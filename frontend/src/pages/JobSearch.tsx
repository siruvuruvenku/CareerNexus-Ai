import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Search, SlidersHorizontal } from 'lucide-react'
import { useSearchParams } from 'react-router-dom'
import { countryApi, jobApi, skillApi } from '../api/jobs'
import { SkillPicker } from '../components/SkillPicker'
import { EmptyState, ErrorState, Field, JobCard, LoadingState, PageTitle, inputClass } from '../components/Ui'

const employmentTypes = ['FULL_TIME', 'PART_TIME', 'CONTRACT', 'INTERNSHIP']
const experienceYearsByBand: Record<string, number> = { fresher: 0, '0-1': 1, '1-2': 1, '2-3': 2, '3-5': 3, '5+': 5 }
const experienceBandFromYears = (value: string) => value === '0' ? 'fresher' : value === '1' ? '0-1' : value === '2' ? '2-3' : value === '3' ? '3-5' : value === '5' ? '5+' : ''
const experienceBandFromParams = (params: URLSearchParams) => params.get('experienceBand') || experienceBandFromYears(params.get('experienceYears') || '')

export function JobSearch() {
  const [params, setParams] = useSearchParams()
  const [country, setCountry] = useState(params.get('country') || '')
  const [state, setState] = useState(params.get('state') || '')
  const [city, setCity] = useState(params.get('city') || '')
  const [title, setTitle] = useState(params.get('title') || '')
  const [company, setCompany] = useState(params.get('company') || '')
  const [skills, setSkills] = useState<string[]>(params.getAll('skills'))
  const [experience, setExperience] = useState(experienceBandFromParams(params))
  const [employment, setEmployment] = useState(params.get('employmentType') || '')
  const [workMode, setWorkMode] = useState(params.get('workMode') || '')
  const [minSalary, setMinSalary] = useState(params.get('minSalary') || '')
  const [maxSalary, setMaxSalary] = useState(params.get('maxSalary') || '')
  const [salaryCurrency, setSalaryCurrency] = useState(params.get('salaryCurrency') || '')
  const [salaryInterval, setSalaryInterval] = useState(params.get('salaryInterval') || '')
  const [posted, setPosted] = useState(params.get('postedWithinDays') || '')
  const [sort, setSort] = useState(params.get('sort') || 'newest')
  const countries = useQuery({ queryKey: ['countries'], queryFn: countryApi.list, staleTime: 10 * 60_000 })
  const locations = useQuery({ queryKey: ['locations', country], queryFn: () => countryApi.locations(country), enabled: Boolean(country), staleTime: 10 * 60_000 })
  const skillCatalog = useQuery({ queryKey: ['skills'], queryFn: skillApi.list, staleTime: 10 * 60_000 })
  const query = useQuery({ queryKey: ['job-search', params.toString()], queryFn: () => { const filters = new URLSearchParams(params); filters.delete('submitted'); filters.delete('experienceBand'); return jobApi.search(filters) }, enabled: params.has('submitted') })
  const salaryComparison = Boolean(minSalary || maxSalary || sort === 'salary')
  const salaryRangeInvalid = Boolean(minSalary && maxSalary && Number(minSalary) > Number(maxSalary))
  const salarySettingsMissing = salaryComparison && (!salaryCurrency || !salaryInterval)

  const submit = (event: React.FormEvent) => {
    event.preventDefault()
    const next = new URLSearchParams()
    next.set('submitted', '1')
    next.set('page', '0')
    if (country) next.set('country', country)
    if (state.trim()) next.set('state', state.trim())
    if (city) next.set('city', city)
    if (title.trim()) next.set('title', title.trim())
    if (company.trim()) next.set('company', company.trim())
    skills.forEach(skill => next.append('skills', skill))
    if (experience) { next.set('experienceBand', experience); next.set('experienceYears', String(experienceYearsByBand[experience])) }
    if (employment) next.set('employmentType', employment)
    if (workMode) next.set('workMode', workMode)
    if (minSalary) next.set('minSalary', minSalary)
    if (maxSalary) next.set('maxSalary', maxSalary)
    if (salaryCurrency) next.set('salaryCurrency', salaryCurrency)
    if (salaryInterval) next.set('salaryInterval', salaryInterval)
    if (posted) next.set('postedWithinDays', posted)
    next.set('sort', sort)
    setParams(next)
  }

  const reset = () => {
    setCountry(''); setState(''); setCity(''); setTitle(''); setCompany(''); setSkills([]); setExperience(''); setEmployment(''); setWorkMode(''); setMinSalary(''); setMaxSalary(''); setSalaryCurrency(''); setSalaryInterval(''); setPosted(''); setSort('newest'); setParams({})
  }

  return <>
    <PageTitle eyebrow="Global job search" title="Find a role that fits" subtitle="Search live, verified listings from connected sources. Every result links back to its original application page." />
    <form onSubmit={submit} className="rounded-2xl border border-line bg-white p-5 shadow-sm sm:p-6">
      <div className="mb-5 flex items-center justify-between"><div className="flex items-center gap-2"><span className="rounded-xl bg-mint p-2 text-forest"><SlidersHorizontal size={17} /></span><div><h2 className="font-display text-base font-extrabold text-ink">Search filters</h2><p className="text-xs text-muted">Combine as many filters as you need.</p></div></div><button type="button" onClick={reset} className="text-xs font-semibold text-muted hover:text-forest">Clear all</button></div>
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <Field label="Country">{countries.isError ? <button type="button" onClick={() => void countries.refetch()} className={inputClass + ' text-left'}>Could not load countries · retry</button> : <select className={inputClass} value={country} onChange={event => { setCountry(event.target.value); setState(''); setCity('') }}><option value="">All countries</option>{countries.data?.map(item => <option key={item.code} value={item.code}>{item.name}</option>)}</select>}</Field>
        <Field label="State / province" hint="State options are not available from the current location API."><input className={inputClass} value={state} onChange={event => setState(event.target.value)} placeholder="e.g. Telangana" /></Field>
        <Field label="City">{country ? <><input className={inputClass} list="search-cities" value={city} onChange={event => setCity(event.target.value)} placeholder={locations.isPending ? 'Loading available cities…' : 'Type or choose a city'} /><datalist id="search-cities">{locations.data?.map(item => <option key={item} value={item} />)}</datalist></> : <input className={inputClass} value="" disabled placeholder="Choose a country first" />}</Field>
        <Field label="Job title"><input className={inputClass} value={title} onChange={event => setTitle(event.target.value)} placeholder="e.g. Product designer" /></Field>
        <Field label="Company"><input className={inputClass} value={company} onChange={event => setCompany(event.target.value)} placeholder="Company name" /></Field>
        <Field label="Technical skills"><SkillPicker values={skills} onChange={setSkills} catalog={skillCatalog.data?.map(skill => skill.name) || []} /></Field>
        <Field label="Experience"><select className={inputClass} value={experience} onChange={event => setExperience(event.target.value)}><option value="">Any experience</option><option value="fresher">Fresher</option><option value="0-1">0–1 year</option><option value="1-2">1–2 years</option><option value="2-3">2–3 years</option><option value="3-5">3–5 years</option><option value="5+">5+ years</option></select></Field>
        <Field label="Employment type"><select className={inputClass} value={employment} onChange={event => setEmployment(event.target.value)}><option value="">Any type</option>{employmentTypes.map(item => <option key={item} value={item}>{item.replaceAll('_', ' ')}</option>)}</select></Field>
        <Field label="Work mode"><select className={inputClass} value={workMode} onChange={event => setWorkMode(event.target.value)}><option value="">Any work mode</option><option value="REMOTE">Remote</option><option value="HYBRID">Hybrid</option><option value="ONSITE">On-site</option></select></Field>
        <Field label="Minimum salary"><input className={inputClass} type="number" min="0" value={minSalary} onChange={event => setMinSalary(event.target.value)} placeholder="No minimum" /></Field>
        <Field label="Maximum salary"><input className={inputClass} type="number" min="0" value={maxSalary} onChange={event => setMaxSalary(event.target.value)} placeholder="No maximum" /></Field>
        <Field label="Salary currency" hint="No exchange-rate conversion is applied."><select className={inputClass} value={salaryCurrency} onChange={event => setSalaryCurrency(event.target.value)}><option value="">Choose if filtering salary</option>{['USD', 'INR', 'GBP', 'EUR', 'CAD', 'AUD', 'NZD', 'SGD', 'JPY', 'CNY', 'CHF', 'AED', 'SAR', 'ZAR', 'BRL', 'MXN', 'HKD', 'KRW', 'SEK', 'NOK', 'DKK', 'PLN', 'PHP', 'IDR', 'PKR'].map(currency => <option key={currency} value={currency}>{currency}</option>)}</select></Field>
        <Field label="Pay period"><select className={inputClass} value={salaryInterval} onChange={event => setSalaryInterval(event.target.value)}><option value="">Any / not specified</option><option value="YEAR">Year</option><option value="MONTH">Month</option><option value="WEEK">Week</option><option value="DAY">Day</option><option value="HOUR">Hour</option></select></Field>
        <Field label="Date posted"><select className={inputClass} value={posted} onChange={event => setPosted(event.target.value)}><option value="">Any time</option><option value="1">Past 24 hours</option><option value="7">Past week</option><option value="30">Past month</option></select></Field>
        <Field label="Sort by"><select className={inputClass} value={sort} onChange={event => setSort(event.target.value)}><option value="newest">Most recent</option><option value="match">Experience fit</option><option value="salary">Salary</option></select></Field>
      </div>
      {salarySettingsMissing && <p className="mt-3 text-right text-xs text-rose-700">Choose a salary currency and pay period to compare or sort salary amounts.</p>}
      {salaryRangeInvalid && <p className="mt-3 text-right text-xs text-rose-700">Minimum salary must not exceed maximum salary.</p>}
      <div className="mt-5 flex justify-end"><button type="submit" disabled={salarySettingsMissing || salaryRangeInvalid} className="inline-flex items-center gap-2 rounded-xl bg-forest px-5 py-3 text-sm font-bold text-white shadow-sm transition hover:bg-[#294dca] disabled:cursor-not-allowed disabled:opacity-45"><Search size={16} />Search jobs</button></div>
    </form>

    <section className="mt-7">
      {!params.has('submitted') ? <EmptyState icon={<Search size={21} />} title="Your search starts here" body="Choose a location, title, or skill and search live listings from connected job sources." /> : query.isPending ? <LoadingState label="Searching connected job sources…" /> : query.isError ? <ErrorState error={query.error} retry={() => void query.refetch()} /> : <>
        <div className="mb-4 flex flex-wrap items-end justify-between gap-2"><div><p className="text-xs font-bold uppercase tracking-[.15em] text-forest">Search results</p><h2 className="mt-1 font-display text-xl font-extrabold text-ink">{query.data.total.toLocaleString()} {query.data.total === 1 ? 'role' : 'roles'} found</h2></div><p className="text-xs text-muted">Listings verified by their connected source</p></div>
        {query.data.sourceWarnings.length > 0 && <div className="mb-4 rounded-xl border border-amber-200 bg-amber-50 p-3 text-xs leading-5 text-amber-900">Some sources could not be reached: {query.data.sourceWarnings.join(' ')}</div>}
        {query.data.items.length ? <><div className="space-y-4">{query.data.items.map(job => <JobCard key={job.id} job={job} />)}</div>{query.data.totalPages > 1 && <div className="mt-5 flex items-center justify-between rounded-xl border border-line bg-white px-4 py-3"><span className="text-xs text-muted">Page {query.data.page + 1} of {query.data.totalPages}</span><div className="flex gap-2"><button type="button" disabled={query.data.page <= 0} onClick={() => { const next = new URLSearchParams(params); next.set('page', String(query.data.page - 1)); setParams(next) }} className="rounded-lg border border-line px-3 py-2 text-xs font-bold text-ink disabled:opacity-40">Previous</button><button type="button" disabled={query.data.page + 1 >= query.data.totalPages} onClick={() => { const next = new URLSearchParams(params); next.set('page', String(query.data.page + 1)); setParams(next) }} className="rounded-lg border border-line px-3 py-2 text-xs font-bold text-ink disabled:opacity-40">Next</button></div></div>}</> : <EmptyState title="No roles match these filters" body="Try a broader location, remove a skill, or adjust the posted date and experience filters." action={<button onClick={reset} className="text-sm font-bold text-forest">Clear filters</button>} />}
      </>}
    </section>
  </>
}
