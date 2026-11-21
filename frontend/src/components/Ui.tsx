import type { PropsWithChildren, ReactNode } from 'react'
import { AlertCircle, ArrowUpRight, Bookmark, BriefcaseBusiness, Clock3, Globe2, MapPin, Sparkles, ExternalLink } from 'lucide-react'
import { Link } from 'react-router-dom'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { applicationApi, jobApi } from '../api/jobs'
import { errorMessage } from '../api/client'
import type { Job } from '../types'

export function Button({ children, className = '', variant = 'primary', ...props }: PropsWithChildren<React.ButtonHTMLAttributes<HTMLButtonElement> & { variant?: 'primary' | 'secondary' | 'ghost' }>) {
  const styles = variant === 'primary' ? 'bg-forest text-white hover:bg-[#294dca] shadow-sm' : variant === 'secondary' ? 'border border-line bg-white text-ink hover:border-forest/40 hover:bg-mint/40' : 'text-muted hover:bg-slate-100 hover:text-ink'
  return <button className={`inline-flex items-center justify-center gap-2 rounded-xl px-4 py-2.5 text-sm font-semibold transition disabled:cursor-not-allowed disabled:opacity-55 ${styles} ${className}`} {...props}>{children}</button>
}

export function PageTitle({ eyebrow, title, subtitle, action }: { eyebrow?: string; title: string; subtitle?: string; action?: ReactNode }) {
  return <div className="mb-7 flex flex-wrap items-end justify-between gap-4">
    <div>{eyebrow && <p className="mb-2 text-xs font-bold uppercase tracking-[.16em] text-forest">{eyebrow}</p>}<h1 className="font-display text-3xl font-extrabold tracking-tight text-ink sm:text-[2.1rem]">{title}</h1>{subtitle && <p className="mt-2 max-w-2xl text-sm leading-6 text-muted">{subtitle}</p>}</div>
    {action}
  </div>
}

export function LoadingState({ label = 'Loading your workspace…' }: { label?: string }) {
  return <div className="flex min-h-48 items-center justify-center gap-3 text-sm text-muted"><span className="h-5 w-5 animate-spin rounded-full border-2 border-forest/20 border-t-forest" />{label}</div>
}

export function ErrorState({ error, retry }: { error: unknown; retry?: () => void }) {
  return <div className="rounded-2xl border border-rose-200 bg-rose-50 p-5 text-sm text-rose-800"><div className="flex items-start gap-3"><AlertCircle size={19} className="mt-0.5 shrink-0" /><div><p className="font-semibold">We couldn’t load this data</p><p className="mt-1 text-rose-700">{errorMessage(error)}</p>{retry && <button onClick={retry} className="mt-3 font-bold underline underline-offset-2">Try again</button>}</div></div></div>
}

export function EmptyState({ icon, title, body, action }: { icon?: ReactNode; title: string; body: string; action?: ReactNode }) {
  return <div className="rounded-2xl border border-dashed border-slate-300 bg-white px-6 py-12 text-center"><div className="mx-auto mb-4 flex h-12 w-12 items-center justify-center rounded-2xl bg-mint text-forest">{icon || <BriefcaseBusiness size={22} />}</div><h3 className="font-display text-lg font-bold text-ink">{title}</h3><p className="mx-auto mt-2 max-w-md text-sm leading-6 text-muted">{body}</p>{action && <div className="mt-5">{action}</div>}</div>
}

export function JobCard({ job, saved = false }: { job: Job; saved?: boolean }) {
  const queryClient = useQueryClient()
  const apply = useMutation({
    mutationFn: () => applicationApi.create(job.id, 'APPLIED'),
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['applications'] }); queryClient.invalidateQueries({ queryKey: ['dashboard'] }) },
  })
  const save = useMutation({
    mutationFn: async () => {
      if (saved) await jobApi.unsave(job.id)
      else await jobApi.save(job.id)
    },
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['saved-jobs'] }); queryClient.invalidateQueries({ queryKey: ['dashboard'] }) },
  })
  const experience = job.requiredExperienceMin == null ? 'Experience not listed' : job.requiredExperienceMax == null ? `${job.requiredExperienceMin}+ yrs` : `${job.requiredExperienceMin}–${job.requiredExperienceMax} yrs`
  const salary = job.salaryMin != null || job.salaryMax != null
    ? `${job.salaryCurrency ? `${job.salaryCurrency} ` : ''}${job.salaryMin?.toLocaleString() ?? '—'} – ${job.salaryMax?.toLocaleString() ?? '—'} / ${job.salaryInterval?.toLowerCase() || 'period not listed'}` : null
  return <article className="group rounded-2xl border border-line bg-white p-5 transition hover:-translate-y-0.5 hover:border-forest/25 hover:shadow-soft sm:p-6">
    <div className="flex items-start gap-4">
      <div className="flex h-12 w-12 shrink-0 items-center justify-center rounded-2xl bg-[#eaf4ef] font-display text-sm font-extrabold uppercase text-forest">{job.companyName.slice(0, 2)}</div>
      <div className="min-w-0 flex-1">
        <div className="flex flex-wrap items-center gap-2"><Link to={`/jobs/${job.id}`} className="font-display text-lg font-bold leading-snug text-ink hover:text-forest">{job.title}</Link>{job.match && <span title="Estimated skill and experience fit, not hiring probability" className="inline-flex items-center gap-1 rounded-full bg-mint px-2.5 py-1 text-xs font-bold text-forest"><Sparkles size={12} />{Math.round(job.match.scorePercent)}% estimated fit</span>}</div>
        <p className="mt-1 text-sm font-medium text-slate-600">{job.companyName}</p>
        <div className="mt-3 flex flex-wrap gap-x-4 gap-y-2 text-xs text-muted"><span className="inline-flex items-center gap-1.5"><MapPin size={14} />{[job.location, job.countryCode].filter(Boolean).join(', ') || 'Location not listed'}</span><span className="inline-flex items-center gap-1.5"><BriefcaseBusiness size={14} />{job.employmentType?.replaceAll('_', ' ') || 'Employment type not listed'}</span><span className="inline-flex items-center gap-1.5"><Globe2 size={14} />{job.workplaceType?.replaceAll('_', ' ') || 'Work mode not listed'}</span><span className="inline-flex items-center gap-1.5"><Clock3 size={14} />{experience}</span></div>
      </div>
      <button aria-label={saved ? 'Remove saved job' : 'Save job'} title={saved ? 'Remove saved job' : 'Save job'} onClick={() => save.mutate()} disabled={save.isPending} className={`rounded-xl p-2.5 transition ${saved ? 'bg-mint text-forest' : 'bg-slate-50 text-slate-500 hover:bg-mint hover:text-forest'}`}><Bookmark size={18} fill={saved ? 'currentColor' : 'none'} /></button>
    </div>
    {job.description && <p className="mt-4 line-clamp-2 text-sm leading-6 text-slate-600">{job.description}</p>}
    <div className="mt-4 flex flex-wrap items-center justify-between gap-3 border-t border-slate-100 pt-4">
      <div className="flex flex-wrap gap-2">{job.requiredSkills.slice(0, 4).map(skill => <span key={skill} className="rounded-lg bg-slate-50 px-2.5 py-1 text-xs font-medium text-slate-600">{skill}</span>)}{job.requiredSkills.length > 4 && <span className="rounded-lg bg-slate-50 px-2.5 py-1 text-xs text-muted">+{job.requiredSkills.length - 4}</span>}</div>
      <div className="flex flex-wrap items-center gap-3 text-xs text-muted">{salary && <span className="font-semibold text-ink">{salary}</span>}{job.postedAt && <span>{new Date(job.postedAt).toLocaleDateString()}</span>}</div>
    </div>
    {job.source === 'Adzuna' && <a href={job.sourceAttributionUrl || 'https://www.adzuna.com'} target="_blank" rel="noreferrer" className="mt-3 inline-flex h-[23px] w-[116px] items-center justify-center rounded bg-[#edf5f1] text-[12px] font-extrabold tracking-wide text-forest" aria-label="Jobs from Adzuna">Adzuna</a>}
    <div className="mt-4 flex flex-wrap justify-end gap-2">
      <Link to={`/jobs/${job.id}`} className="rounded-xl border border-line px-3.5 py-2 text-xs font-bold text-ink transition hover:border-forest/40 hover:text-forest">View details</Link>
      <button onClick={() => apply.mutate()} disabled={apply.isPending} className="inline-flex items-center gap-1.5 rounded-xl bg-forest px-3.5 py-2 text-xs font-bold text-white transition hover:bg-[#294dca] disabled:opacity-60">{apply.isPending ? 'Saving…' : 'Track as applied'} <ExternalLink size={13} /></button>
      <a href={job.sourceUrl} target="_blank" rel="noreferrer" className="rounded-xl border border-line px-3.5 py-2 text-xs font-bold text-forest transition hover:bg-mint">Apply now <ArrowUpRight size={13} className="inline" /></a>
    </div>
    {save.isError && <p role="alert" className="mt-2 text-xs text-rose-600">{errorMessage(save.error)}</p>}{apply.isError && <p role="alert" className="mt-2 text-xs text-rose-600">{errorMessage(apply.error)}</p>}
  </article>
}

export function Field({ label, hint, children }: PropsWithChildren<{ label: string; hint?: string }>) {
  return <label className="block"><span className="mb-1.5 block text-xs font-bold uppercase tracking-wide text-slate-600">{label}</span>{children}{hint && <span className="mt-1 block text-xs text-muted">{hint}</span>}</label>
}

export const inputClass = 'w-full rounded-xl border border-line bg-white px-3.5 py-2.5 text-sm text-ink outline-none transition placeholder:text-slate-400 focus:border-forest/60 focus:ring-4 focus:ring-forest/10'

