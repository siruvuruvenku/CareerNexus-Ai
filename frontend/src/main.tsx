import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import App from './App'
import { AuthProvider } from './auth/AuthProvider'
import './styles.css'

if (localStorage.getItem('careernexus.theme') === 'dark') document.documentElement.classList.add('dark')

const queryClient = new QueryClient({
  defaultOptions: {
    queries: { staleTime: 45_000, retry: 1, refetchOnWindowFocus: false },
    mutations: { retry: 0 },
  },
})

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <QueryClientProvider client={queryClient}>
      <AuthProvider><BrowserRouter><App /></BrowserRouter></AuthProvider>
    </QueryClientProvider>
  </React.StrictMode>,
)

