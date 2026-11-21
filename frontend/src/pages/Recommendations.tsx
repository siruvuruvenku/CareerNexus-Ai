import { useMemo, useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { ArrowUpRight, MapPin, Sparkles } from 'lucide-react'
import { Link } from 'react-router-dom'
import { jobApi, profileApi } from '../api/jobs'
import { EmptyState, ErrorState, JobCard, LoadingState, PageTitle, inputClass } from '../components/Ui'

export function Recommendations() {
  const jobs = useQuery({ queryKey: ['recommendations'], queryFn: () => jobApi.recommended(0, 50) })
  const profile = useQuery({ queryKey: ['profile'], queryFn: profileApi.get })
  const [filter, setFilter] = useState('highest')
  const [location, setLocation] = useState('')
  const [minSalary, setMinSalary] = useState('')
  const [salaryCurrency, setSalaryCurrency] = useState('')
  const [salaryInterval, setSalaryInterval] = useState('')
  const visible = useMemo(() => {
    const rows = [...(jobs.data || [])].filter(job => {
      if (location && !`${job.location || ''} ${job.countryCode || ''}`.toLowerCase().includes(location.toLowerCase())) return false
      const salaryUpperBound = job.salaryMax ?? job.salaryMin
      if (minSalary && (!salaryCurrency || !salaryInterval || job.salaryCurrency !== salaryCurrency || job.salaryInterval !== salaryInterval || salaryUpperBound == null || salaryUpperBound < Number(minSalary))) return false
      if (filter === 'experience' && job.match?.experienceCompatibility.toLowerCase().includes('outside')) return false
      if (filter === 'location' && !job.match?.locationCompatible) return false
      return true
    })
    if (filter === 'recent') rows.sort((a, b) => new Date(b.postedAt || 0).getTime() - new Date(a.postedAt || 0).getTime())
    else rows.sort((a, b) => (b.match?.scorePercent || 0) - (a.match?.scorePercent || 0))
    return rows
  }, [filter, jobs.data, location, minSalary, salaryCurrency, salaryInterval])
  if (jobs.isPending) return <LoadingState label="Comparing your profile with current listings…" />
  if (jobs.isError) return <ErrorState error={jobs.error} retry={() => void jobs.refetch()} />
  return <>
    <PageTitle eyebrow="AI recommendations" title="Roles that align with you" subtitle="A transparent comparison of your profile with live job requirements. Match scores are estimates of profile fit, not hiring probabilities." action={<Link to="/profile" className="rounded-xl border border-line bg-white px-4 py-2.5 text-sm font-bold text-ink hover:border-forest/40">Update profile</Link>} />
    {profile.data && <div className="mb-5 flex flex-wrap items-center gap-3 rounded-2xl border border-forest/10 bg-mint/55 p-4"><span className="rounded-xl bg-white p-2.5 text-forest"><Sparkles size={18} /></span><div className="min-w-0 flex-1"><p className="text-sm font-bold text-ink">Profile match context</p><p className="mt-1 text-xs text-slate-600">{profile.data.skills.length} skills · {profile.data.yearsExperience ?? 0} years experience · {profile.data.city || profile.data.countryCode || 'No preferred location set'}</p></div><Link to="/profile" className="text-xs font-bold text-forest">Edit profile <ArrowUpRight size={13} className="inline" /></Link></div>}
    <div className="mb-5 grid gap-3 rounded-2xl border border-line bg-white p-4 sm:grid-cols-2 xl:grid-cols-5"><label className="block"><span className="mb-1.5 block text-xs font-bold uppercase tracking-wide text-slate-600">Sort / fit</span><select className={inputClass} value={filter} onChange={event => setFilter(event.target.value)}><option value="highest">Highest match</option><option value="recent">Recently posted</option><option value="experience">Experience compatible</option><option value="location">Location compatible</option></select></label><label className="block"><span className="mb-1.5 block text-xs font-bold uppercase tracking-wide text-slate-600">Location</span><span className="relative"><MapPin size={15} className="absolute left-3 top-3 text-slate-400" /><input className={`${inputClass} pl-9`} value={location} onChange={event => setLocation(event.target.value)} placeholder="City or country" /></span></label><label className="block"><span className="mb-1.5 block text-xs font-bold uppercase tracking-wide text-slate-600">Minimum salary</span><input type="number" min="0" className={inputClass} value={minSalary} onChange={event => setMinSalary(event.target.value)} placeholder="Any salary" /></label><label className="block"><span className="mb-1.5 block text-xs font-bold uppercase tracking-wide text-slate-600">Currency</span><select className={inputClass} value={salaryCurrency} onChange={event => setSalaryCurrency(event.target.value)}><option value="">Choose a currency</option>{['USD', 'INR', 'GBP', 'EUR', 'CAD', 'AUD', 'NZD', 'SGD', 'JPY', 'CNY', 'CHF', 'AED', 'SAR', 'ZAR', 'BRL', 'MXN', 'HKD', 'KRW', 'SEK', 'NOK', 'DKK', 'PLN', 'PHP', 'IDR', 'PKR'].map(code => <option value={code} key={code}>{code}</option>)}</select></label><label className="block"><span className="mb-1.5 block text-xs font-bold uppercase tracking-wide text-slate-600">Pay period</span><select className={inputClass} value={salaryInterval} onChange={event => setSalaryInterval(event.target.value)}><option value="">Choose a period</option><option value="YEAR">Year</option><option value="MONTH">Month</option><option value="WEEK">Week</option><option value="DAY">Day</option><option value="HOUR">Hour</option></select></label></div>
    {minSalary && (!salaryCurrency || !salaryInterval) && <p className="-mt-2 mb-4 text-right text-xs text-slate-500">Choose both a currency and pay period to filter recommendations by salary.</p>}
    {visible.length ? <div className="space-y-4">{visible.map(job => <div key={job.id}><div className="mb-2 grid gap-2 sm:grid-cols-3">{job.match && <div className="rounded-xl border border-line bg-white px-3 py-2 text-xs"><span className="font-bold text-forest">{Math.round(job.match.scorePercent)}% estimated fit</span><span className="ml-2 text-muted">{job.match.matchingSkills.length} skills matched</span></div>}<div className="rounded-xl border border-line bg-white px-3 py-2 text-xs text-slate-600">Experience: {job.match?.experienceCompatibility || 'Not enough profile data'}</div><div className="rounded-xl border border-line bg-white px-3 py-2 text-xs text-slate-600">Location: {job.match?.locationCompatible ? 'Compatible' : 'May need relocation'}</div></div><JobCard job={job} />{job.match && <div className="mt-2 grid gap-3 rounded-xl border border-line bg-white p-4 text-xs sm:grid-cols-3"><div><p className="mb-1 font-bold text-ink">Matching skills</p><p className="leading-5 text-muted">{job.match.matchingSkills.length ? job.match.matchingSkills.join(', ') : 'No matching skills detected yet.'}</p></div><div><p className="mb-1 font-bold text-ink">Skills to build</p><p className="leading-5 text-muted">{job.match.missingSkills.length ? job.match.missingSkills.join(', ') : 'No required skill gaps detected.'}</p></div><div><p className="mb-1 font-bold text-ink">Why this match</p><p className="leading-5 text-muted">{job.match.explanation}</p></div></div>}</div>)}</div> : <EmptyState icon={<Sparkles size={21} />} title="No recommendations match these filters" body="Try a broader location or salary range. Recommendations update as your profile and live job data change." />}
    <p className="mt-5 text-center text-[11px] text-muted">Match percentages are estimated from listed skills, experience, title, and location. They are not hiring probabilities.</p>
  </>
}
