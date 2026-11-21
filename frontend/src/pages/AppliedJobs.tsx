import { useMemo, useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { FileCheck2, Search } from 'lucide-react'
import { applicationApi } from '../api/jobs'
import { EmptyState, ErrorState, JobCard, LoadingState, PageTitle, inputClass } from '../components/Ui'

export function AppliedJobs() {
  const applications = useQuery({ queryKey: ['applications'], queryFn: applicationApi.list })
  const [location, setLocation] = useState('')
  const applied = useMemo(() => (applications.data || []).filter(item => item.status === 'APPLIED' && (!location || `${item.job.location || ''} ${item.job.countryCode || ''}`.toLowerCase().includes(location.toLowerCase()))), [applications.data, location])
  if (applications.isPending) return <LoadingState label="Loading applied jobs…" />
  if (applications.isError) return <ErrorState error={applications.error} retry={() => void applications.refetch()} />
  return <>
    <PageTitle eyebrow="Your applications" title="Applied jobs" subtitle="Roles you marked as applied, with direct links back to the original posting." />
    <div className="mb-5 max-w-sm"><label className="block"><span className="mb-1.5 block text-xs font-bold uppercase tracking-wide text-slate-600">Filter by location</span><span className="relative"><Search size={15} className="absolute left-3 top-3 text-slate-400" /><input className={`${inputClass} pl-9`} value={location} onChange={event => setLocation(event.target.value)} placeholder="City or country" /></span></label></div>
    {applied.length ? <div className="space-y-4">{applied.map(item => <div key={item.id}><div className="mb-2 inline-flex items-center gap-2 rounded-full border border-line bg-white px-3 py-1.5 text-[11px] font-semibold text-slate-600"><FileCheck2 size={13} className="text-forest" />Applied · updated {new Date(item.updatedAt).toLocaleDateString()}</div><JobCard job={item.job} /></div>)}</div> : <EmptyState icon={<FileCheck2 size={21} />} title="No applied jobs yet" body="When you apply through an original job link, use “Mark as applied” to keep the role here and track your next step." />}
  </>
}
