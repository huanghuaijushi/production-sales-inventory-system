import { readAuthToken } from '@/utils/token-storage'

interface ApiEnvelope<T> {
  code: number
  message: string
  data: T
  timestamp: string
  traceId: string | undefined
}

interface RequestOptions extends Omit<RequestInit, 'body'> {
  auth?: boolean
  body?: BodyInit | unknown
}

export class ApiError extends Error {
  readonly status: number
  readonly code: number
  readonly traceId: string | undefined
  readonly data: unknown

  constructor(message: string, status: number, code: number, traceId: string | undefined, data: unknown) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.code = code
    this.traceId = traceId
    this.data = data
  }
}

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api/v1'

export async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const { auth, body, ...fetchOptions } = options
  const headers = new Headers(options.headers)

  if (!headers.has('Content-Type') && isJsonBody(body)) {
    headers.set('Content-Type', 'application/json')
  }

  headers.set('Accept', 'application/json')
  headers.set('X-Trace-Id', createTraceId())

  if (auth !== false) {
    const token = readAuthToken()
    if (token) {
      headers.set('Authorization', `Bearer ${token}`)
    }
  }

  const requestInit: RequestInit = {
    ...fetchOptions,
    headers
  }

  const serializedBody = serializeBody(body)
  if (serializedBody !== undefined) {
    requestInit.body = serializedBody
  }

  const response = await fetch(`${API_BASE_URL}${path}`, requestInit)

  const payload = await parseResponse<T>(response)

  if (isApiEnvelope<T>(payload)) {
    if (!response.ok || payload.code < 200 || payload.code >= 300) {
      throw new ApiError(payload.message || 'Request failed', response.status, payload.code, payload.traceId, payload.data)
    }
    return payload.data
  }

  if (!response.ok) {
    throw new ApiError(response.statusText || 'Request failed', response.status, response.status, undefined, payload)
  }

  return payload
}

function isJsonBody(body: RequestOptions['body']): body is Record<string, unknown> {
  return Boolean(body)
    && typeof body === 'object'
    && !(body instanceof FormData)
    && !(body instanceof Blob)
    && !(body instanceof ArrayBuffer)
    && !(body instanceof URLSearchParams)
}

function serializeBody(body: RequestOptions['body']): BodyInit | undefined {
  if (body === undefined || body === null) {
    return undefined
  }
  return isJsonBody(body) ? JSON.stringify(body) : (body as BodyInit)
}

async function parseResponse<T>(response: Response): Promise<ApiEnvelope<T> | T> {
  const text = await response.text()
  if (!text) {
    return {
      code: response.status,
      message: response.ok ? 'success' : response.statusText,
      data: undefined as T,
      timestamp: new Date().toISOString(),
      traceId: response.headers.get('X-Trace-Id') ?? undefined
    }
  }

  try {
    return JSON.parse(text) as ApiEnvelope<T> | T
  } catch {
    return {
      code: response.status,
      message: text,
      data: undefined as T,
      timestamp: new Date().toISOString(),
      traceId: response.headers.get('X-Trace-Id') ?? undefined
    }
  }
}

function isApiEnvelope<T>(value: ApiEnvelope<T> | T): value is ApiEnvelope<T> {
  if (value === null || typeof value !== 'object') {
    return false
  }

  return 'code' in value
    && 'message' in value
    && 'timestamp' in value
}

function createTraceId(): string {
  if (crypto.randomUUID) {
    return crypto.randomUUID()
  }
  return `${Date.now()}-${Math.random().toString(16).slice(2)}`
}
