import { useEffect, useState } from 'react'
import { Moon, Sun, UserRound } from 'lucide-react'
import { useAuth } from '../auth/AuthProvider'
import { PageTitle } from '../components/Ui'

export function SettingsPage() {
  const { user, logout } = useAuth()
  const [dark, setDark] = useState(() => localStorage.getItem('careernexus.theme') === 'dark')
  useEffect(() => { document.documentElement.classList.toggle('dark', dark); localStorage.setItem('careernexus.theme', dark ? 'dark' : 'light') }, [dark])
  return <>
    <PageTitle eyebrow="Workspace preferences" title="Settings" subtitle="Adjust the appearance of CareerNexus on this device and manage your account session." />
    <section className="max-w-3xl space-y-4">
      <article className="flex flex-wrap items-center justify-between gap-4 rounded-2xl border border-line bg-white p-5 sm:p-6"><div className="flex items-center gap-3"><span className="rounded-xl bg-mint p-2.5 text-forest">{dark ? <Moon size={18} /> : <Sun size={18} />}</span><div><h2 className="font-display text-base font-extrabold text-ink">Appearance</h2><p className="mt-1 text-xs text-muted">Choose a light or dark workspace.</p></div></div><button type="button" role="switch" aria-label="Toggle dark mode" aria-checked={dark} onClick={() => setDark(value => !value)} className={`relative h-7 w-12 rounded-full transition ${dark ? 'bg-forest' : 'bg-slate-300'}`}><span className={`absolute top-1 h-5 w-5 rounded-full bg-white shadow-sm transition ${dark ? 'left-6' : 'left-1'}`} /></button></article>
      <article className="flex flex-wrap items-center justify-between gap-4 rounded-2xl border border-line bg-white p-5 sm:p-6"><div className="flex items-center gap-3"><span className="rounded-xl bg-slate-50 p-2.5 text-slate-500"><UserRound size={18} /></span><div><h2 className="font-display text-base font-extrabold text-ink">Account</h2><p className="mt-1 text-xs text-muted">{user?.fullName} · {user?.email}</p></div></div><button type="button" onClick={logout} className="rounded-xl border border-line px-4 py-2.5 text-xs font-bold text-ink hover:border-rose-200 hover:text-rose-700">Sign out</button></article>
      <p className="px-1 text-[11px] text-muted">Appearance is stored in this browser. Your profile and job data are stored in your CareerNexus account.</p>
    </section>
  </>
}
