import { useState } from 'react'
import { NavLink, Outlet, useLocation } from 'react-router-dom'
import { BarChart3, Bell, BriefcaseBusiness, ChevronDown, Compass, FileText, LayoutDashboard, Menu, Search, Settings, UserRound, X } from 'lucide-react'
import { useAuth } from '../auth/AuthProvider'

const links = [
  { to: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { to: '/search', label: 'Find jobs', icon: Search },
  { to: '/recommendations', label: 'For you', icon: Compass },
  { to: '/saved', label: 'Saved jobs', icon: BriefcaseBusiness },
  { to: '/applied', label: 'Applied jobs', icon: FileText },
  { to: '/applications', label: 'Application tracker', icon: FileText },
  { to: '/profile', label: 'My profile', icon: UserRound },
  { to: '/settings', label: 'Settings', icon: Settings },
]

function Sidebar({ close }: { close?: () => void }) {
  const { user, logout } = useAuth()
  return <div className="flex h-full flex-col bg-[#17264a] px-4 pb-4 pt-6 text-white">
    <NavLink to="/dashboard" onClick={close} className="mb-10 flex items-center gap-3 px-2">
      <span className="flex h-10 w-10 items-center justify-center rounded-2xl bg-[#e6ecff] text-forest"><Compass size={21} strokeWidth={2.5} /></span>
      <span className="font-display text-lg font-extrabold tracking-tight">Career<span className="text-[#c1ceff]">Nexus</span></span>
    </NavLink>
    <p className="mb-3 px-3 text-[10px] font-bold uppercase tracking-[.2em] text-slate-400">Workspace</p>
    <nav className="space-y-1">
      {links.map(({ to, label, icon: Icon }) => <NavLink key={to} to={to} onClick={close} className={({ isActive }) => `flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium transition ${isActive ? 'bg-white/12 text-white shadow-inner' : 'text-slate-300 hover:bg-white/7 hover:text-white'}`}><Icon size={18} />{label}</NavLink>)}
    </nav>
    <div className="mt-auto">
      <div className="mb-4 rounded-2xl border border-white/10 bg-white/5 p-4"><div className="mb-2 flex items-center gap-2 text-[#a7dfc3]"><BarChart3 size={15} /><span className="text-xs font-bold">Your next move</span></div><p className="text-xs leading-5 text-slate-300">Keep your profile current to get more useful role matches.</p><NavLink to="/profile" onClick={close} className="mt-3 inline-flex text-xs font-bold text-white hover:text-[#a7dfc3]">Update profile <span className="ml-1">↗</span></NavLink></div>
      <NavLink to="/profile" onClick={close} className="flex items-center gap-3 rounded-xl p-2.5 hover:bg-white/8"><div className="flex h-9 w-9 items-center justify-center rounded-full bg-[#d8ede3] text-xs font-extrabold text-forest">{user?.fullName?.slice(0, 1).toUpperCase() || 'U'}</div><div className="min-w-0 flex-1"><p className="truncate text-xs font-bold">{user?.fullName || 'Your profile'}</p><p className="truncate text-[10px] text-slate-400">{user?.email}</p></div><ChevronDown size={14} className="text-slate-400" /></NavLink>
      <button onClick={logout} className="mt-1 w-full rounded-lg px-3 py-2 text-left text-xs font-semibold text-slate-400 hover:bg-white/8 hover:text-white">Sign out</button>
    </div>
  </div>
}

function Header({ onMenu }: { onMenu: () => void }) {
  const location = useLocation()
  const title = links.find(link => location.pathname.startsWith(link.to))?.label || (location.pathname.startsWith('/jobs/') ? 'Job details' : 'Settings')
  return <header className="sticky top-0 z-20 flex h-[72px] items-center justify-between border-b border-line/80 bg-paper/90 px-4 backdrop-blur sm:px-8">
    <div className="flex items-center gap-3"><button onClick={onMenu} className="rounded-lg p-2 text-ink hover:bg-white lg:hidden" aria-label="Open navigation"><Menu size={20} /></button><div><p className="text-[10px] font-bold uppercase tracking-[.18em] text-muted">CareerNexus / {title}</p><p className="mt-0.5 font-display text-sm font-bold text-ink sm:text-base">Build your next chapter</p></div></div>
    <div className="flex items-center gap-2"><NavLink to="/profile" aria-label="My profile" className="rounded-xl p-2.5 text-slate-500 hover:bg-white hover:text-ink"><UserRound size={18} /></NavLink><NavLink to="/settings" aria-label="Settings" className="rounded-xl p-2.5 text-slate-500 hover:bg-white hover:text-ink"><Settings size={18} /></NavLink><button title="Notifications" className="relative rounded-xl p-2.5 text-slate-500 hover:bg-white hover:text-ink"><Bell size={18} /><span className="absolute right-2 top-2 h-1.5 w-1.5 rounded-full bg-forest" /></button></div>
  </header>
}

export function AppShell() {
  const [menuOpen, setMenuOpen] = useState(false)
  return <div className="min-h-screen bg-paper text-ink">
    <aside className="fixed inset-y-0 left-0 z-30 hidden w-[248px] lg:block"><Sidebar /></aside>
    {menuOpen && <div className="fixed inset-0 z-40 lg:hidden"><button onClick={() => setMenuOpen(false)} aria-label="Close navigation" className="absolute inset-0 bg-ink/40" /><aside className="absolute inset-y-0 left-0 w-[280px] shadow-2xl"><button onClick={() => setMenuOpen(false)} className="absolute right-3 top-4 z-10 rounded-lg p-2 text-white" aria-label="Close menu"><X size={19} /></button><Sidebar close={() => setMenuOpen(false)} /></aside></div>}
    <div className="lg:pl-[248px]"><Header onMenu={() => setMenuOpen(true)} /><main className="mx-auto max-w-[1500px] px-4 py-7 sm:px-8 sm:py-9"><Outlet /></main><footer className="border-t border-line px-4 py-5 text-center text-xs text-muted sm:px-8">CareerNexus · Job information comes from connected providers · Match scores are estimates</footer></div>
  </div>
}

