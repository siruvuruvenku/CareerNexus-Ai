import { useMemo, useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Bookmark, Search } from 'lucide-react'
import { applicationApi, jobApi } from '../api/jobs'
import { EmptyState, ErrorState, JobCard, LoadingState, PageTitle, inputClass } from '../components/Ui'

export function SavedJobs() {
  const jobs = useQuery({ queryKey: ['saved-jobs'], queryFn: jobApi.saved })
  const applications = useQuery({ queryKey: ['applications'], queryFn: applicationApi.list })
  const [location, setLocation] = useState('')
  const [status, setStatus] = useState('')
  const [sort, setSort] = useState('newest')
  const rows = useMemo(() => [...(jobs.data || [])].filter(job => {
    const app = applications.data?.find(item => item.jobId === job.id)
    return (!location || `${job.location || ''} ${job.countryCode || ''}`.toLowerCase().includes(location.toLowerCase())) && (!status || app?.status === status)
  }).sort((a, b) => sort === 'oldest' ? new Date(a.postedAt || 0).getTime() - new Date(b.postedAt || 0).getTime() : new Date(b.postedAt || 0).getTime() - new Date(a.postedAt || 0).getTime()), [applications.data, jobs.data, location, sort, status])
  if (jobs.isPending) return <LoadingState label="Loading saved jobs…" />
  if (jobs.isError) return <ErrorState error={jobs.error} retry={() => void jobs.refetch()} />
  return <>
    <PageTitle eyebrow="Your shortlist" title="Saved jobs" subtitle="Keep promising roles close, then follow each one through your application tracker." />
    <section className="mb-5 grid gap-3 rounded-2xl border border-line bg-white p-4 sm:grid-cols-3"><label className="block"><span className="mb-1.5 block text-xs font-bold uppercase tracking-wide text-slate-600">Filter location</span><span className="relative"><Search size={15} className="absolute left-3 top-3 text-slate-400" /><input className={`${inputClass} pl-9`} value={location} onChange={event => setLocation(event.target.value)} placeholder="City or country" /></span></label><label className="block"><span className="mb-1.5 block text-xs font-bold uppercase tracking-wide text-slate-600">Application status</span><select className={inputClass} value={status} onChange={event => setStatus(event.target.value)}><option value="">All statuses</option>{['INTERESTED', 'SAVED', 'APPLIED', 'ASSESSMENT', 'INTERVIEW', 'REJECTED', 'OFFER'].map(value => <option key={value} value={value}>{value.replaceAll('_', ' ')}</option>)}</select></label><label className="block"><span className="mb-1.5 block text-xs font-bold uppercase tracking-wide text-slate-600">Sort by posted date</span><select className={inputClass} value={sort} onChange={event => setSort(event.target.value)}><option value="newest">Newest first</option><option value="oldest">Oldest first</option></select></label></section>
    {rows.length ? <div className="space-y-4">{rows.map(job => { const app = applications.data?.find(item => item.jobId === job.id); return <div key={job.id}><div className="mb-2 inline-flex items-center gap-2 rounded-full border border-line bg-white px-3 py-1.5 text-[11px] font-semibold text-slate-600"><Bookmark size={12} className="text-forest" />Tracker status: <span className="text-ink">{app?.status.replaceAll('_', ' ') || 'Saved'}</span></div><JobCard job={job} saved /></div>})}</div> : <EmptyState icon={<Bookmark size={21} />} title={jobs.data.length ? 'No saved roles match these filters' : 'Your shortlist is empty'} body={jobs.data.length ? 'Adjust the location or status filter to see more saved jobs.' : 'Save a live job listing to keep it here. Your saved roles stay linked to their original source.'} />}
  </>
}
