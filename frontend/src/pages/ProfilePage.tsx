import { useEffect, useState } from 'react'
import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Download, FileUp, Save, UserRound } from 'lucide-react'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { countryApi, profileApi, skillApi } from '../api/jobs'
import { errorMessage } from '../api/client'
import { SkillPicker } from '../components/SkillPicker'
import { Button, ErrorState, Field, LoadingState, PageTitle, inputClass } from '../components/Ui'

const profileSchema = z.object({
  fullName: z.string().trim().min(1, 'Enter your name.').max(160),
  targetTitle: z.string().max(160),
  countryCode: z.string().max(2),
  city: z.string().max(160),
  yearsExperience: z.coerce.number().int().min(0).max(60),
  summary: z.string().max(5000),
})
type ProfileForm = z.infer<typeof profileSchema>

export function ProfilePage() {
  const client = useQueryClient()
  const profile = useQuery({ queryKey: ['profile'], queryFn: profileApi.get })
  const countries = useQuery({ queryKey: ['countries'], queryFn: countryApi.list, staleTime: 10 * 60_000 })
  const skills = useQuery({ queryKey: ['skills'], queryFn: skillApi.list, staleTime: 10 * 60_000 })
  const [selectedSkills, setSelectedSkills] = useState<string[]>([])
  const [resumeMessage, setResumeMessage] = useState('')
  const form = useForm<z.input<typeof profileSchema>, unknown, ProfileForm>({ resolver: zodResolver(profileSchema), defaultValues: { fullName: '', targetTitle: '', countryCode: '', city: '', yearsExperience: 0, summary: '' } })
  const country = form.watch('countryCode')
  const locations = useQuery({ queryKey: ['locations', country], queryFn: () => countryApi.locations(country), enabled: Boolean(country), staleTime: 10 * 60_000 })
  useEffect(() => {
    if (!profile.data) return
    form.reset({ fullName: profile.data.fullName || '', targetTitle: profile.data.targetTitle || '', countryCode: profile.data.countryCode || '', city: profile.data.city || '', yearsExperience: profile.data.yearsExperience ?? 0, summary: profile.data.summary || '' })
    setSelectedSkills(profile.data.skills || [])
  }, [profile.data, form])
  const save = useMutation({ mutationFn: (values: ProfileForm) => profileApi.update({ ...values, skills: selectedSkills }), onSuccess: result => { client.setQueryData(['profile'], result); client.invalidateQueries({ queryKey: ['recommendations'] }); client.invalidateQueries({ queryKey: ['dashboard'] }) } })
  const upload = useMutation({ mutationFn: profileApi.uploadResume, onSuccess: result => { setResumeMessage(`${result.fileName} uploaded successfully.`); client.invalidateQueries({ queryKey: ['profile'] }) }, onError: error => setResumeMessage(errorMessage(error)) })
  if (profile.isPending) return <LoadingState label="Loading your profile…" />
  if (profile.isError) return <ErrorState error={profile.error} retry={() => void profile.refetch()} />
  const errors = form.formState.errors
  return <>
    <PageTitle eyebrow="Your professional story" title="My profile" subtitle="Your profile helps CareerNexus compare your experience with skills and locations in live listings." />
    <form onSubmit={form.handleSubmit(values => save.mutate(values))} className="space-y-5">
      <section className="rounded-2xl border border-line bg-white p-5 sm:p-7"><div className="mb-5 flex items-center gap-3"><span className="rounded-xl bg-mint p-2.5 text-forest"><UserRound size={18} /></span><div><h2 className="font-display text-lg font-extrabold text-ink">Personal details</h2><p className="text-xs text-muted">Basic information for your account.</p></div></div><div className="grid gap-4 sm:grid-cols-2"><Field label="Full name"><input className={inputClass} maxLength={160} {...form.register('fullName')} />{errors.fullName && <span className="mt-1 block text-xs text-rose-700">{errors.fullName.message}</span>}</Field><Field label="Email address"><input className={`${inputClass} bg-slate-50`} value={profile.data.email} readOnly /></Field><Field label="Target role"><input className={inputClass} maxLength={160} placeholder="e.g. Full stack developer" {...form.register('targetTitle')} /></Field><Field label="Years of experience"><input className={inputClass} type="number" min="0" max="60" {...form.register('yearsExperience')} />{errors.yearsExperience && <span className="mt-1 block text-xs text-rose-700">{errors.yearsExperience.message}</span>}</Field><Field label="Country">{countries.isError ? <p className="text-sm text-rose-700">{errorMessage(countries.error)}</p> : <select className={inputClass} {...form.register('countryCode')}><option value="">Choose a country</option>{countries.data?.map(item => <option key={item.code} value={item.code}>{item.name}</option>)}</select>}</Field><Field label="City">{country ? <><input className={inputClass} list="profile-cities" placeholder={locations.isPending ? 'Loading available cities…' : 'Type or choose a city'} {...form.register('city')} /><datalist id="profile-cities">{locations.data?.map(cityName => <option key={cityName} value={cityName} />)}</datalist></> : <input className={inputClass} placeholder="Choose a country first" disabled />}</Field></div></section>
      <section className="rounded-2xl border border-line bg-white p-5 sm:p-7"><h2 className="font-display text-lg font-extrabold text-ink">Professional summary</h2><p className="mt-1 text-xs text-muted">A short overview to give recommendations useful context.</p><textarea className={`${inputClass} mt-4 min-h-32 resize-y`} maxLength={5000} placeholder="Describe your strengths, focus, or career direction" {...form.register('summary')} />{errors.summary && <span className="mt-1 block text-xs text-rose-700">{errors.summary.message}</span>}</section>
      <section className="rounded-2xl border border-line bg-white p-5 sm:p-7"><h2 className="font-display text-lg font-extrabold text-ink">Skills</h2><p className="mt-1 text-xs text-muted">Select several skills or add a custom skill. They are used in estimated match scores.</p><div className="mt-4"><SkillPicker values={selectedSkills} onChange={setSelectedSkills} catalog={skills.data?.map(item => item.name) || []} /></div>{skills.isError && <p className="mt-2 text-xs text-rose-700">The suggested skills could not be loaded. You can still add skills above.</p>}</section>
      <section className="rounded-2xl border border-line bg-white p-5 sm:p-7"><div className="flex flex-wrap items-center justify-between gap-4"><div><h2 className="font-display text-lg font-extrabold text-ink">Resume</h2><p className="mt-1 text-xs text-muted">Upload a PDF, DOC, or DOCX file up to 5 MB.</p><p className="mt-2 text-xs font-semibold text-ink">{profile.data.resumeUploaded ? 'A resume is attached to your profile.' : 'No resume uploaded yet.'}</p></div><div className="flex flex-wrap gap-2"><label className="inline-flex cursor-pointer items-center gap-2 rounded-xl border border-line bg-white px-4 py-2.5 text-xs font-bold text-ink hover:border-forest/40"><FileUp size={15} />Choose resume<input type="file" className="sr-only" accept=".pdf,.doc,.docx" onChange={event => { const file = event.target.files?.[0]; if (file) { setResumeMessage(''); upload.mutate(file) } event.currentTarget.value = '' }} /></label>{profile.data.resumeUploaded && <button type="button" onClick={async () => { try { const response = await profileApi.downloadResume(); const href = URL.createObjectURL(response.data); const anchor = document.createElement('a'); anchor.href = href; anchor.download = 'resume'; anchor.click(); URL.revokeObjectURL(href) } catch (error) { setResumeMessage(errorMessage(error)) } }} className="inline-flex items-center gap-2 rounded-xl bg-mint px-4 py-2.5 text-xs font-bold text-forest"><Download size={15} />Download</button>}</div></div>{resumeMessage && <p role="status" className={`mt-3 text-xs ${upload.isError ? 'text-rose-700' : 'text-forest'}`}>{resumeMessage}</p>}</section>
      {save.isError && <p role="alert" className="rounded-xl bg-rose-50 p-3 text-sm text-rose-700">{errorMessage(save.error)}</p>}{save.isSuccess && <p role="status" className="rounded-xl bg-mint/70 p-3 text-sm font-medium text-forest">Profile saved. Recommendations will use these updates.</p>}
      <div className="flex justify-end"><Button type="submit" disabled={save.isPending}><Save size={15} />{save.isPending ? 'Saving profile…' : 'Save profile'}</Button></div>
    </form>
  </>
}
