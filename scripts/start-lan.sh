#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
API_DIR="$ROOT_DIR/production-sales-inventory-api"
WEB_DIR="$ROOT_DIR/production-sales-inventory-web"

API_PORT="${API_PORT:-8080}"
WEB_PORT="${WEB_PORT:-5173}"
LAN_IP="${LAN_IP:-}"

if [ -z "$LAN_IP" ]; then
  LAN_IP="$(ipconfig getifaddr en0 2>/dev/null || true)"
fi

if [ -z "$LAN_IP" ]; then
  LAN_IP="$(ipconfig getifaddr en1 2>/dev/null || true)"
fi

if [ -z "$LAN_IP" ]; then
  LAN_IP="$(ifconfig 2>/dev/null | awk '/inet / && $2 !~ /^127\./ && $2 !~ /^169\.254/ {print $2; exit}')"
fi

if [ -z "$LAN_IP" ]; then
  echo "没有检测到局域网 IP。请确认电脑已连接 Wi-Fi 或手动指定：LAN_IP=192.168.x.x scripts/start-lan.sh"
  exit 1
fi

cleanup() {
  if [ -n "${API_PID:-}" ]; then
    kill "$API_PID" 2>/dev/null || true
  fi
  if [ -n "${WEB_PID:-}" ]; then
    kill "$WEB_PID" 2>/dev/null || true
  fi
}
trap cleanup EXIT INT TERM

echo "HHJS 产销存系统局域网启动"
echo "前端访问地址: http://$LAN_IP:$WEB_PORT"
echo "后端接口地址: http://$LAN_IP:$API_PORT/api/v1"
echo "同一 Wi-Fi 下的电脑或手机打开上面的前端地址即可。按 Ctrl+C 停止。"
echo

(
  cd "$API_DIR"
  SERVER_ADDRESS=0.0.0.0 SERVER_PORT="$API_PORT" mvn spring-boot:run
) &
API_PID=$!

(
  cd "$WEB_DIR"
  VITE_API_PROXY_TARGET="http://127.0.0.1:$API_PORT" npm run dev -- --host 0.0.0.0 --port "$WEB_PORT"
) &
WEB_PID=$!

while kill -0 "$API_PID" 2>/dev/null && kill -0 "$WEB_PID" 2>/dev/null; do
  sleep 1
done

wait "$API_PID" 2>/dev/null || true
wait "$WEB_PID" 2>/dev/null || true
