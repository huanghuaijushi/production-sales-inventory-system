export const TOKEN_STORAGE_KEY = 'psi_mobile_access_token'
export const TOKEN_TYPE_STORAGE_KEY = 'psi_mobile_token_type'
export const USER_STORAGE_KEY = 'psi_mobile_user'

export function getStorage<T>(key: string, fallback: T): T {
  try {
    const value = uni.getStorageSync(key)

    if (value === '' || value === null || value === undefined) {
      return fallback
    }

    return value as T
  } catch {
    return fallback
  }
}

export function setStorage<T>(key: string, value: T) {
  uni.setStorageSync(key, value)
}

export function removeStorage(key: string) {
  uni.removeStorageSync(key)
}
