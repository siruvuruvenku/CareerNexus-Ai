import { useMemo, useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { ArrowRight, Bookmark, BriefcaseBusiness, Clock3, FileText, Sparkles, TrendingUp } from 'lucide-react'
import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import { Link } from 'react-router-dom'
import { applicationApi, dashboardApi, jobApi } from '../api/jobs'
import { EmptyState, ErrorState, LoadingState, PageTitle } from '../components/Ui'
import type { Application } from '../types'

const icons = [BriefcaseBusiness, TrendingUp, Sparkles, Bookmark, FileText]
const chartTabs = [
  { id: 'city', label: 'By location', key: 'jobsByCity' as const },
  { id: 'technology', label: 'By technology', key: 'jobsByTechnology' as const },
  { id: 'company', label: 'By company', key: 'jobsByCompany' as const },
]

export function Dashboard() {
  const summary = useQuery({ queryKey: ['dashboard'], queryFn: dashboardApi.summary })
  const recentJobs = useQuery({ queryKey: ['dashboard-recent-jobs'], queryFn: () => jobApi.search(new URLSearchParams('size=3&sort=newest')) })
  const recommendations = useQuery({ queryKey: ['recommendations', 'dashboard'], queryFn: () => jobApi.recommended(0, 3) })
  const applications = useQuery({ queryKey: ['applications'], queryFn: applicationApi.list })
  const [activeChart, setActiveChart] = useState('city')
  const activity = useMemo(() => {
    const rows: { label: string; applications: number }[] = []
    const items: Application[] = applications.data || []
    for (let offset = 6; offset >= 0; offset -= 1) {
      const date = new Date(); date.setHours(0, 0, 0, 0); date.setDate(date.getDate() - offset)
      const key = date.toDateString()
      rows.push({ label: date.toLocaleDateString(undefined, { weekday: 'short' }), applications: items.filter(item => new Date(item.updatedAt).toDateString() === key).length })
    }
    return rows
  }, [applications.data])
  if (summary.isPending) return <LoadingState />
  if (summary.isError) return <ErrorState error={summary.error} retry={() => void summary.refetch()} />
  const data = summary.data
  const chart = chartTabs.find(tab => tab.id === activeChart)!
  const breakdown = data[chart.key]
  return <>
    <PageTitle eyebrow="Your workspace" title="Good morning. Let’s find your next move." subtitle="A quick view of live opportunities and the applications you’re moving forward." action={<Link to="/search" className="inline-flex items-center gap-2 rounded-xl bg-forest px-4 py-2.5 text-sm font-bold text-white hover:bg-[#115744]">Explore jobs <ArrowRight size={16} /></Link>} />
    <section className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3 2xl:grid-cols-6">{data.metrics.map((metric, index) => { const Icon = icons[index] || BriefcaseBusiness; return <article key={metric.label} className="rounded-2xl border border-line bg-white p-5 shadow-sm"><div className="flex items-start justify-between"><span className="text-xs font-semibold text-muted">{metric.label}</span><span className={`rounded-xl p-2 ${index === 2 ? 'bg-mint text-forest' : 'bg-slate-50 text-slate-500'}`}><Icon size={17} /></span></div><p className="mt-3 font-display text-3xl font-extrabold text-ink">{metric.value.toLocaleString()}</p><p className="mt-2 text-[11px] leading-5 text-muted">{metric.hint}</p></article> })}<article className="rounded-2xl border border-line bg-white p-5 shadow-sm"><div className="flex items-start justify-between"><span className="text-xs font-semibold text-muted">Jobs expiring soon</span><span className="rounded-xl bg-slate-50 p-2 text-slate-500"><Clock3 size={17} /></span></div><p className="mt-3 font-display text-xl font-extrabold text-ink">Unavailable</p><p className="mt-2 text-[11px] leading-5 text-muted">Connected sources do not provide consistent expiry dates.</p></article></section>
    <section className="mt-6 grid gap-5 xl:grid-cols-[1.45fr_.8fr]">
      <article className="rounded-2xl border border-line bg-white p-5 sm:p-6"><div className="flex flex-wrap items-start justify-between gap-3"><div><p className="text-xs font-bold uppercase tracking-[.15em] text-forest">Opportunity landscape</p><h2 className="mt-1 font-display text-lg font-extrabold text-ink">Where the listings are</h2></div><div className="flex rounded-xl bg-slate-50 p-1">{chartTabs.map(tab => <button key={tab.id} onClick={() => setActiveChart(tab.id)} className={`rounded-lg px-3 py-2 text-[11px] font-bold transition ${activeChart === tab.id ? 'bg-white text-forest shadow-sm' : 'text-muted hover:text-ink'}`}>{tab.label}</button>)}</div></div>
        {breakdown.length ? <div className="mt-6 h-[270px] w-full"><ResponsiveContainer width="100%" height="100%"><BarChart data={breakdown} margin={{ top: 5, right: 8, left: -20, bottom: 0 }}><CartesianGrid stroke="#edf1ef" vertical={false} /><XAxis dataKey="label" tick={{ fontSize: 11, fill: '#71817c' }} axisLine={false} tickLine={false} interval={0} /><YAxis allowDecimals={false} tick={{ fontSize: 11, fill: '#71817c' }} axisLine={false} tickLine={false} /><Tooltip cursor={{ fill: '#f4f8f6' }} contentStyle={{ borderRadius: 12, border: '1px solid #e4eae7', fontSize: 12 }} /><Bar dataKey="count" name="Openings" fill="#176b58" radius={[7, 7, 0, 0]} maxBarSize={40} /></BarChart></ResponsiveContainer></div> : <div className="flex h-[270px] items-center justify-center"><EmptyState icon={<BriefcaseBusiness size={20} />} title="No live listing data yet" body="Connect a job provider and run a search. This chart uses listings returned by those sources." action={<Link to="/search" className="text-xs font-bold text-forest">Go to job search →</Link>} /></div>}
      </article>
      <article className="rounded-2xl border border-line bg-white p-5 sm:p-6"><p className="text-xs font-bold uppercase tracking-[.15em] text-forest">Application flow</p><h2 className="mt-1 font-display text-lg font-extrabold text-ink">Your pipeline</h2>{Object.keys(data.applicationStatuses).length ? <div className="mt-6 space-y-4">{Object.entries(data.applicationStatuses).map(([status, value]) => { const max = Math.max(...Object.values(data.applicationStatuses), 1); return <div key={status}><div className="mb-1.5 flex justify-between text-xs"><span className="font-semibold capitalize text-slate-600">{status.toLowerCase()}</span><span className="font-bold text-ink">{value}</span></div><div className="h-2 overflow-hidden rounded-full bg-slate-100"><div style={{ width: `${(value / max) * 100}%` }} className="h-full rounded-full bg-forest" /></div></div> })}<Link to="/applications" className="mt-5 inline-flex items-center gap-1 text-xs font-bold text-forest">Open tracker <ArrowRight size={13} /></Link></div> : <div className="mt-5 rounded-2xl bg-paper p-5"><p className="text-sm font-bold text-ink">Your tracker is ready</p><p className="mt-2 text-xs leading-5 text-muted">Add a role when you’re ready to apply. We’ll keep its status, notes, and interview date here.</p><Link to="/applications" className="mt-4 inline-flex items-center gap-1 text-xs font-bold text-forest">View tracker <ArrowRight size={13} /></Link></div>}
        <div className="mt-6 rounded-2xl border border-amber-100 bg-amber-50 p-4"><p className="text-xs font-bold text-amber-900">Expiry information</p><p className="mt-1 text-[11px] leading-5 text-amber-800">{data.availabilityNote}</p></div>
      </article>
    </section>
    <section className="mt-6 grid gap-5 xl:grid-cols-[1.05fr_.95fr]">
      <article className="rounded-2xl border border-line bg-white p-5 sm:p-6">
        <div className="flex items-start justify-between gap-3"><div><p className="text-xs font-bold uppercase tracking-[.15em] text-forest">Your activity</p><h2 className="mt-1 font-display text-lg font-extrabold text-ink">Application updates</h2></div><Link to="/applications" className="text-xs font-bold text-forest">Open tracker</Link></div>
        {applications.isError ? <p className="mt-5 text-sm text-rose-700">Application activity is temporarily unavailable.</p> : <><div className="mt-6 h-[190px] w-full"><ResponsiveContainer width="100%" height="100%"><BarChart data={activity} margin={{ top: 5, right: 6, left: -24, bottom: 0 }}><CartesianGrid stroke="#edf1ef" vertical={false} /><XAxis dataKey="label" tick={{ fontSize: 11, fill: '#75809a' }} axisLine={false} tickLine={false} /><YAxis allowDecimals={false} tick={{ fontSize: 11, fill: '#75809a' }} axisLine={false} tickLine={false} /><Tooltip cursor={{ fill: '#f1f4fc' }} contentStyle={{ borderRadius: 12, border: '1px solid #e5e9f2', fontSize: 12 }} /><Bar dataKey="applications" name="Updated applications" fill="#365ee8" radius={[7, 7, 0, 0]} maxBarSize={34} /></BarChart></ResponsiveContainer></div><p className="text-[11px] text-muted">Updates to your application statuses over the past seven days.</p></>}
      </article>
      <article className="rounded-2xl border border-line bg-white p-5 sm:p-6">
        <div className="flex items-start justify-between gap-3"><div><p className="text-xs font-bold uppercase tracking-[.15em] text-forest">Recently posted</p><h2 className="mt-1 font-display text-lg font-extrabold text-ink">Fresh opportunities</h2></div><Link to="/search" className="text-xs font-bold text-forest">Search all</Link></div>
        {recentJobs.isPending ? <div className="py-8"><LoadingState label="Loading recent listings…" /></div> : recentJobs.isError ? <p className="mt-5 text-sm text-muted">Live listings could not be loaded right now.</p> : recentJobs.data.items.length ? <div className="mt-4 space-y-3">{recentJobs.data.items.slice(0, 2).map(job => <div key={job.id} className="rounded-xl border border-line p-4"><Link to={`/jobs/${job.id}`} className="font-semibold text-ink hover:text-forest">{job.title}</Link><p className="mt-1 text-xs text-muted">{job.companyName} · {job.location || 'Location not listed'}</p></div>)}</div> : <p className="mt-5 text-sm text-muted">No current listings were returned by connected sources.</p>}
      </article>
    </section>
    <section className="mt-6 rounded-2xl border border-line bg-white p-5 sm:p-6">
      <div className="mb-4 flex items-start justify-between gap-3"><div><p className="text-xs font-bold uppercase tracking-[.15em] text-forest">AI recommendations</p><h2 className="mt-1 font-display text-lg font-extrabold text-ink">Roles aligned with your profile</h2></div><Link to="/recommendations" className="text-xs font-bold text-forest">View all</Link></div>
      {recommendations.isPending ? <LoadingState label="Finding profile matches…" /> : recommendations.isError ? <p className="text-sm text-muted">Add your skills to your profile to see recommendations.</p> : recommendations.data.length ? <div className="grid gap-3 md:grid-cols-3">{recommendations.data.map(job => <div key={job.id} className="rounded-xl border border-line p-4"><div className="flex items-start justify-between gap-2"><Link to={`/jobs/${job.id}`} className="font-semibold text-ink hover:text-forest">{job.title}</Link>{job.match && <span className="shrink-0 rounded-full bg-mint px-2 py-1 text-[10px] font-bold text-forest">{Math.round(job.match.scorePercent)}%</span>}</div><p className="mt-1 text-xs text-muted">{job.companyName} · {job.location || 'Location not listed'}</p><p className="mt-3 text-xs text-slate-500">{job.match?.matchingSkills.length || 0} matching skills</p></div>)}</div> : <EmptyState title="No recommendations yet" body="Add skills and a preferred location to your profile, then check back for estimated matches." action={<Link to="/profile" className="text-xs font-bold text-forest">Complete profile</Link>} />}
    </section>
    <section className="mt-6 flex flex-wrap items-center justify-between gap-4 rounded-2xl bg-[#e7f2ec] p-5 sm:px-7"><div><p className="font-display text-base font-extrabold text-ink">Your profile makes matches more useful.</p><p className="mt-1 text-sm text-slate-600">Add skills, preferred location, and experience to personalize recommendations.</p></div><Link to="/profile" className="rounded-xl border border-forest/15 bg-white px-4 py-2.5 text-xs font-bold text-forest hover:bg-mint">Update profile</Link></section>
  </>
}

