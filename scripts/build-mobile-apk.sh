#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
MOBILE_DIR="$ROOT_DIR/production-sales-inventory-mobile"
ANDROID_DIR="$ROOT_DIR/production-sales-inventory-android"
APP_ID="__UNI__B05BB5F"
MOBILE_APP_DIST="$MOBILE_DIR/dist/build/app"
ANDROID_APP_WWW="$ANDROID_DIR/simpleDemo/src/main/assets/apps/$APP_ID/www"
APK_SOURCE="$ANDROID_DIR/simpleDemo/build/outputs/apk/release/simpleDemo-release.apk"
APK_OUTPUT_DIR="$ANDROID_DIR/simpleDemo/build/outputs/apk/release"

if [[ ! -d "$MOBILE_DIR" ]]; then
  echo "Mobile project not found: $MOBILE_DIR" >&2
  exit 1
fi

if [[ ! -d "$ANDROID_DIR" ]]; then
  echo "Android packaging project not found: $ANDROID_DIR" >&2
  echo "Please create production-sales-inventory-android from DCloud Android offline SDK first." >&2
  exit 1
fi

echo "==> Building uniapp app resources"
(
  cd "$MOBILE_DIR"
  npm run build:app
)

if [[ ! -d "$MOBILE_APP_DIST" ]]; then
  echo "uniapp app dist not found: $MOBILE_APP_DIST" >&2
  exit 1
fi

echo "==> Syncing app resources to Android packaging project"
rm -rf "$ANDROID_APP_WWW"
mkdir -p "$ANDROID_APP_WWW"
cp -R "$MOBILE_APP_DIST/." "$ANDROID_APP_WWW/"

echo "==> Building Android release APK"
(
  cd "$ANDROID_DIR"
  ./gradlew :simpleDemo:assembleRelease
)

if [[ ! -f "$APK_SOURCE" ]]; then
  echo "APK not found after build: $APK_SOURCE" >&2
  exit 1
fi

API_BASE_URL="$(grep -E '^VITE_API_BASE_URL=' "$MOBILE_DIR/.env.production" | tail -1 | cut -d '=' -f 2- || true)"
API_HOST="$(printf '%s' "$API_BASE_URL" | sed -E 's#^https?://([^:/]+).*#\1#')"
if [[ -z "$API_HOST" || "$API_HOST" == "$API_BASE_URL" ]]; then
  API_HOST="release"
fi

APK_TARGET="$APK_OUTPUT_DIR/production-sales-inventory-mobile-$API_HOST.apk"
cp "$APK_SOURCE" "$APK_TARGET"

echo "==> APK ready"
echo "$APK_TARGET"
