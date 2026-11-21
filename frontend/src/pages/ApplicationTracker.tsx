import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { CalendarDays, FileText, Save } from 'lucide-react'
import { Link } from 'react-router-dom'
import { applicationApi } from '../api/jobs'
import { errorMessage } from '../api/client'
import { EmptyState, ErrorState, LoadingState, PageTitle } from '../components/Ui'
import type { Application } from '../types'

const statuses = ['INTERESTED', 'SAVED', 'APPLIED', 'ASSESSMENT', 'INTERVIEW', 'REJECTED', 'OFFER']

function ApplicationRow({ item }: { item: Application }) {
  const client = useQueryClient()
  const [notes, setNotes] = useState(item.notes || '')
  const [interviewAt, setInterviewAt] = useState(item.interviewAt ? new Date(item.interviewAt).toISOString().slice(0, 16) : '')
  const [status, setStatus] = useState(item.status)
  const update = useMutation({ mutationFn: () => applicationApi.update(item.id, { status, notes, interviewAt: interviewAt ? new Date(interviewAt).toISOString() : null }), onSuccess: () => { client.invalidateQueries({ queryKey: ['applications'] }); client.invalidateQueries({ queryKey: ['dashboard'] }) } })
  return <tr className="border-t border-line align-top">
    <td className="min-w-[210px] px-4 py-4"><Link to={`/jobs/${item.jobId}`} className="font-semibold text-ink hover:text-forest">{item.job.title}</Link><p className="mt-1 text-xs text-muted">{item.job.companyName} · {item.job.location || 'Location not listed'}</p></td>
    <td className="min-w-[150px] px-4 py-4"><select className="w-full rounded-lg border border-line bg-white px-2.5 py-2 text-xs font-semibold text-ink" value={status} onChange={event => setStatus(event.target.value)}>{statuses.map(value => <option key={value} value={value}>{value.replaceAll('_', ' ')}</option>)}</select></td>
    <td className="min-w-[190px] px-4 py-4"><label className="relative block"><CalendarDays size={14} className="absolute left-2.5 top-2.5 text-slate-400" /><input aria-label={`Interview date for ${item.job.title}`} type="datetime-local" className="w-full rounded-lg border border-line bg-white py-2 pl-8 pr-2 text-xs text-ink" value={interviewAt} onChange={event => setInterviewAt(event.target.value)} /></label></td>
    <td className="min-w-[240px] px-4 py-4"><textarea aria-label={`Notes for ${item.job.title}`} className="w-full resize-y rounded-lg border border-line bg-white px-3 py-2 text-xs text-ink" rows={2} maxLength={5000} value={notes} onChange={event => setNotes(event.target.value)} placeholder="Add a note or next step" /></td>
    <td className="px-4 py-4"><button onClick={() => update.mutate()} disabled={update.isPending} title="Save changes" aria-label={`Save application changes for ${item.job.title}`} className="rounded-lg bg-mint p-2 text-forest hover:bg-[#dbe5ff] disabled:opacity-50"><Save size={15} /></button>{update.isError && <p className="mt-1 max-w-28 text-[10px] text-rose-700">{errorMessage(update.error)}</p>}</td>
  </tr>
}

export function ApplicationTracker() {
  const applications = useQuery({ queryKey: ['applications'], queryFn: applicationApi.list })
  if (applications.isPending) return <LoadingState label="Loading your application tracker…" />
  if (applications.isError) return <ErrorState error={applications.error} retry={() => void applications.refetch()} />
  return <>
    <PageTitle eyebrow="Stay on track" title="Application tracker" subtitle="Update a status, add a note, and keep interview dates alongside the original job listing." action={<Link to="/search" className="rounded-xl bg-forest px-4 py-2.5 text-sm font-bold text-white hover:bg-[#294dca]">Find a job</Link>} />
    {applications.data.length ? <div className="overflow-hidden rounded-2xl border border-line bg-white"><div className="overflow-x-auto"><table className="w-full border-collapse text-left"><thead className="bg-slate-50"><tr>{['Role', 'Status', 'Interview date', 'Notes', ''].map(label => <th key={label} className="px-4 py-3 text-[10px] font-bold uppercase tracking-wide text-muted">{label}</th>)}</tr></thead><tbody>{applications.data.map(item => <ApplicationRow key={item.id} item={item} />)}</tbody></table></div></div> : <EmptyState icon={<FileText size={21} />} title="Your tracker is ready" body="Mark a live role as applied or save it to start tracking status, interview dates, and notes." action={<Link to="/search" className="text-sm font-bold text-forest">Explore live jobs</Link>} />}
    <p className="mt-4 text-xs text-muted">Status changes and notes are saved to your CareerNexus account.</p>
  </>
}
