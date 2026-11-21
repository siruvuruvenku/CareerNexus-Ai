import api, { request } from './client'
import type { Application, AuthResponse, Country, DashboardSummary, Job, Profile, SearchResult, Skill } from '../types'

export const authApi = {
  login: (email: string, password: string) => request<AuthResponse>(api.post('/auth/login', { email, password })),
  register: (email: string, password: string, fullName: string) => request<AuthResponse>(api.post('/auth/register', { email, password, fullName })),
  forgot: (email: string) => request<void>(api.post('/auth/forgot-password', { email })),
  reset: (token: string, newPassword: string) => request<void>(api.post('/auth/reset-password', { token, newPassword })),
}

export const countryApi = {
  list: () => request<Country[]>(api.get('/countries')),
  locations: (country: string) => request<string[]>(api.get(`/countries/${country}/locations`)),
}

export const skillApi = {
  list: () => request<Skill[]>(api.get('/skills')),
  add: (name: string) => request<Skill>(api.post('/skills', { name })),
}

export const profileApi = {
  get: () => request<Profile>(api.get('/users/profile')),
  update: (profile: Partial<Profile>) => request<Profile>(api.put('/users/profile', profile)),
  uploadResume: async (file: File) => {
    const form = new FormData(); form.append('file', file)
    return request<{ fileName: string; contentType: string }>(api.post('/users/profile/resume', form))
  },
  downloadResume: () => api.get('/users/profile/resume', { responseType: 'blob' }),
}

export const jobApi = {
  search: (params: URLSearchParams) => request<SearchResult>(api.get(`/jobs/search?${params.toString()}`)),
  get: (id: number) => request<Job>(api.get(`/jobs/${id}`)),
  recommended: (page = 0, size = 20) => request<Job[]>(api.get('/jobs/recommended', { params: { page, size } })),
  saved: () => request<Job[]>(api.get('/jobs/saved')),
  save: (id: number) => request<Job>(api.post(`/jobs/${id}/save`)),
  unsave: (id: number) => request<void>(api.delete(`/jobs/${id}/save`)),
}

export const applicationApi = {
  list: () => request<Application[]>(api.get('/applications')),
  create: (jobId: number, status = 'INTERESTED') => request<Application>(api.post('/applications', { jobId, status })),
  update: (id: number, body: Pick<Application, 'status' | 'interviewAt' | 'notes'>) => request<Application>(api.put(`/applications/${id}`, body)),
  remove: (id: number) => request<void>(api.delete(`/applications/${id}`)),
}

export const dashboardApi = { summary: () => request<DashboardSummary>(api.get('/dashboard/summary')) }

