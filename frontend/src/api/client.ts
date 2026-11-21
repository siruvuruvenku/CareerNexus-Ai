import axios from 'axios'
import type { ApiResponse } from '../types'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
  timeout: 25_000,
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('careernexus.token') || sessionStorage.getItem('careernexus.token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

api.interceptors.response.use((response) => response, (error) => {
  if (error.response?.status === 401 && !String(error.config?.url).includes('/auth/')) {
    localStorage.removeItem('careernexus.token')
    localStorage.removeItem('careernexus.user')
    sessionStorage.removeItem('careernexus.token')
    sessionStorage.removeItem('careernexus.user')
    window.dispatchEvent(new Event('careernexus:unauthorized'))
  }
  return Promise.reject(error)
})

export async function request<T>(promise: Promise<{ data: ApiResponse<T> }>): Promise<T> {
  const response = await promise
  return response.data?.data as T
}

export function errorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) return error.response?.data?.message || error.message || 'Request failed'
  return error instanceof Error ? error.message : 'Something went wrong'
}

export default api

