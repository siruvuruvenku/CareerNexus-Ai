import { createContext, useContext, useEffect, useMemo, useState } from 'react'
import type { PropsWithChildren } from 'react'
import { authApi, profileApi } from '../api/jobs'
import type { AuthUser } from '../types'

interface AuthContextValue {
  user: AuthUser | null
  signedIn: boolean
  login: (email: string, password: string, remember?: boolean) => Promise<void>
  register: (email: string, password: string, name: string, profile?: { countryCode?: string; yearsExperience?: number }) => Promise<void>
  logout: () => void
}
const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: PropsWithChildren) {
  const [user, setUser] = useState<AuthUser | null>(() => {
    const saved = localStorage.getItem('careernexus.user') || sessionStorage.getItem('careernexus.user')
    return saved ? JSON.parse(saved) as AuthUser : null
  })
  const persist = (token: string, nextUser: AuthUser, remember = false) => {
    const storage = remember ? localStorage : sessionStorage
    localStorage.removeItem('careernexus.token')
    localStorage.removeItem('careernexus.user')
    sessionStorage.removeItem('careernexus.token')
    sessionStorage.removeItem('careernexus.user')
    storage.setItem('careernexus.token', token)
    storage.setItem('careernexus.user', JSON.stringify(nextUser))
    setUser(nextUser)
  }
  const logout = () => {
    sessionStorage.removeItem('careernexus.token')
    sessionStorage.removeItem('careernexus.user')
    localStorage.removeItem('careernexus.token')
    localStorage.removeItem('careernexus.user')
    setUser(null)
  }
  useEffect(() => {
    window.addEventListener('careernexus:unauthorized', logout)
    return () => window.removeEventListener('careernexus:unauthorized', logout)
  }, [])
  const value = useMemo<AuthContextValue>(() => ({
    user,
    signedIn: Boolean(user && (localStorage.getItem('careernexus.token') || sessionStorage.getItem('careernexus.token'))),
    login: async (email, password, remember = false) => { const result = await authApi.login(email, password); persist(result.accessToken, result.user, remember) },
    register: async (email, password, name, profile) => {
      const result = await authApi.register(email, password, name)
      persist(result.accessToken, result.user)
      if (profile) {
        try { await profileApi.update({ ...profile, fullName: name }) } catch { /* The profile can be completed after sign in. */ }
      }
    },
    logout,
  }), [user])
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) throw new Error('useAuth must be used inside AuthProvider')
  return context
}

