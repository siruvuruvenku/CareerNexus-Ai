import { lazy, Suspense } from 'react'
import { Navigate, Route, Routes } from 'react-router-dom'
import { useAuth } from './auth/AuthProvider'

const AppShell = lazy(() => import('./components/AppShell').then(module => ({ default: module.AppShell })))
const ApplicationTracker = lazy(() => import('./pages/ApplicationTracker').then(module => ({ default: module.ApplicationTracker })))
const AppliedJobs = lazy(() => import('./pages/AppliedJobs').then(module => ({ default: module.AppliedJobs })))
const Dashboard = lazy(() => import('./pages/Dashboard').then(module => ({ default: module.Dashboard })))
const JobDetail = lazy(() => import('./pages/JobDetail').then(module => ({ default: module.JobDetail })))
const JobSearch = lazy(() => import('./pages/JobSearch').then(module => ({ default: module.JobSearch })))
const Landing = lazy(() => import('./pages/Landing').then(module => ({ default: module.Landing })))
const ForgotPassword = lazy(() => import('./pages/AuthPages').then(module => ({ default: module.ForgotPassword })))
const Login = lazy(() => import('./pages/AuthPages').then(module => ({ default: module.Login })))
const Register = lazy(() => import('./pages/AuthPages').then(module => ({ default: module.Register })))
const ResetPassword = lazy(() => import('./pages/AuthPages').then(module => ({ default: module.ResetPassword })))
const ProfilePage = lazy(() => import('./pages/ProfilePage').then(module => ({ default: module.ProfilePage })))
const Recommendations = lazy(() => import('./pages/Recommendations').then(module => ({ default: module.Recommendations })))
const SavedJobs = lazy(() => import('./pages/SavedJobs').then(module => ({ default: module.SavedJobs })))
const SettingsPage = lazy(() => import('./pages/SettingsPage').then(module => ({ default: module.SettingsPage })))

function Protected({ children }: { children: React.ReactNode }) {
  const { signedIn } = useAuth()
  return signedIn ? <>{children}</> : <Navigate to="/login" replace />
}

export default function App() {
  const { signedIn } = useAuth()
  return <Suspense fallback={<div role="status" className="flex min-h-screen items-center justify-center text-sm text-slate-500">Loading CareerNexus…</div>}><Routes>
    <Route path="/" element={signedIn ? <Navigate to="/dashboard" replace /> : <Landing />} />
    <Route path="/login" element={signedIn ? <Navigate to="/dashboard" replace /> : <Login />} />
    <Route path="/register" element={signedIn ? <Navigate to="/dashboard" replace /> : <Register />} />
    <Route path="/forgot-password" element={<ForgotPassword />} />
    <Route path="/reset-password" element={<ResetPassword />} />
    <Route path="/" element={<Protected><AppShell /></Protected>}>
      <Route path="dashboard" element={<Dashboard />} />
      <Route path="search" element={<JobSearch />} />
      <Route path="recommendations" element={<Recommendations />} />
      <Route path="jobs/:id" element={<JobDetail />} />
      <Route path="saved" element={<SavedJobs />} />
      <Route path="applications" element={<ApplicationTracker />} />
      <Route path="applied" element={<AppliedJobs />} />
      <Route path="profile" element={<ProfilePage />} />
      <Route path="settings" element={<SettingsPage />} />
    </Route>
    <Route path="*" element={<Navigate to={signedIn ? '/dashboard' : '/'} replace />} />
  </Routes></Suspense>
}

