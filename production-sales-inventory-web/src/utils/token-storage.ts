const AUTH_TOKEN_KEY = 'production_sales_inventory_access_token'
const AUTH_REMEMBER_KEY = 'production_sales_inventory_remember_login'

export function readAuthToken(): string | null {
  return sessionStorage.getItem(AUTH_TOKEN_KEY) || localStorage.getItem(AUTH_TOKEN_KEY)
}

export function readRememberLogin(): boolean {
  return localStorage.getItem(AUTH_REMEMBER_KEY) === 'true'
}

export function saveAuthToken(token: string, remember: boolean) {
  clearAuthToken()
  if (remember) {
    localStorage.setItem(AUTH_TOKEN_KEY, token)
    localStorage.setItem(AUTH_REMEMBER_KEY, 'true')
    return
  }

  sessionStorage.setItem(AUTH_TOKEN_KEY, token)
  localStorage.setItem(AUTH_REMEMBER_KEY, 'false')
}

export function clearAuthToken() {
  sessionStorage.removeItem(AUTH_TOKEN_KEY)
  localStorage.removeItem(AUTH_TOKEN_KEY)
}
