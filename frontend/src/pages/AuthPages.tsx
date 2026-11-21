import { useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { useMutation, useQuery } from '@tanstack/react-query'
import { zodResolver } from '@hookform/resolvers/zod'
import { ArrowLeft, Compass, LockKeyhole, Mail, UserRound } from 'lucide-react'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { authApi, countryApi } from '../api/jobs'
import { errorMessage } from '../api/client'
import { useAuth } from '../auth/AuthProvider'
import { Button, Field, inputClass } from '../components/Ui'

function AuthFrame({ children, heading, copy, alt, altLink, back = '/' }: { children: React.ReactNode; heading: string; copy: string; alt: string; altLink: string; back?: string }) {
  return <main className="grid min-h-screen bg-white lg:grid-cols-[.92fr_1.08fr]">
    <section className="relative hidden overflow-hidden bg-[#17264a] p-12 text-white lg:flex lg:flex-col lg:justify-between xl:p-16">
      <div className="absolute -right-32 top-20 h-80 w-80 rounded-full border-[55px] border-white/5" /><div className="absolute -bottom-40 -left-24 h-[28rem] w-[28rem] rounded-full bg-forest/50 blur-2xl" />
      <Link to="/" className="relative flex items-center gap-3"><span className="flex h-10 w-10 items-center justify-center rounded-xl bg-white text-forest"><Compass size={21} /></span><span className="font-display text-lg font-extrabold">CareerNexus</span></Link>
      <div className="relative max-w-xl"><p className="text-xs font-bold uppercase tracking-[.2em] text-[#a7dfc3]">A search that knows what matters</p><h1 className="mt-5 font-display text-4xl font-extrabold leading-tight xl:text-5xl">Your skills have a next chapter.</h1><p className="mt-5 max-w-md text-sm leading-7 text-slate-300">Discover live opportunities from connected providers, understand your fit, and keep every application in reach.</p><div className="mt-8 flex items-center gap-3 rounded-2xl border border-white/10 bg-white/5 p-4"><div className="flex h-10 w-10 items-center justify-center rounded-xl bg-white/10 text-[#a7dfc3]"><LockKeyhole size={18} /></div><p className="text-xs leading-5 text-slate-300">Your profile, saved roles, and application notes stay tied to your account.</p></div></div>
      <p className="relative text-xs text-slate-400">© CareerNexus · Make your next move count.</p>
    </section>
    <section className="flex min-h-screen items-center justify-center px-5 py-10 sm:px-10"><div className="w-full max-w-[440px]">
      <Link to={back} className="inline-flex items-center gap-2 text-xs font-bold text-muted hover:text-forest"><ArrowLeft size={15} />Back</Link>
      <p className="mt-8 text-xs font-bold uppercase tracking-[.18em] text-forest">Welcome to CareerNexus</p><h2 className="mt-2 font-display text-3xl font-extrabold tracking-tight text-ink">{heading}</h2><p className="mt-2 text-sm leading-6 text-muted">{copy}</p>
      <div className="mt-7">{children}</div>
      <p className="mt-7 text-center text-sm text-slate-500">{alt} <Link to={altLink} className="font-bold text-forest hover:underline">{altLink === '/login' ? 'Log in' : 'Create an account'}</Link></p>
    </div></section>
  </main>
}

export function Login() {
  const { login } = useAuth(); const navigate = useNavigate()
  const schema = z.object({ email: z.string().email('Enter a valid email address.'), password: z.string().min(1, 'Enter your password.'), remember: z.boolean() })
  const form = useForm<z.infer<typeof schema>>({ resolver: zodResolver(schema), defaultValues: { email: '', password: '', remember: false } })
  const mutation = useMutation({ mutationFn: (values: z.infer<typeof schema>) => login(values.email, values.password, values.remember), onSuccess: () => navigate('/dashboard') })
  return <AuthFrame heading="Welcome back" copy="Sign in to pick up where your search left off." alt="New to CareerNexus?" altLink="/register">
    <form onSubmit={form.handleSubmit(values => mutation.mutate(values))} className="space-y-4">
      <Field label="Email address"><div className="relative"><Mail size={16} className="absolute left-3.5 top-3 text-slate-400" /><input className={`${inputClass} pl-10`} type="email" autoComplete="email" {...form.register('email')} placeholder="you@example.com" /></div>{form.formState.errors.email && <span className="mt-1 block text-xs text-rose-700">{form.formState.errors.email.message}</span>}</Field>
      <Field label="Password"><div className="relative"><LockKeyhole size={16} className="absolute left-3.5 top-3 text-slate-400" /><input className={`${inputClass} pl-10`} type="password" autoComplete="current-password" {...form.register('password')} placeholder="Your password" /></div>{form.formState.errors.password && <span className="mt-1 block text-xs text-rose-700">{form.formState.errors.password.message}</span>}</Field>
      <div className="flex items-center justify-between"><label className="inline-flex items-center gap-2 text-xs font-medium text-slate-600"><input type="checkbox" className="rounded border-line text-forest focus:ring-forest" {...form.register('remember')} />Remember me</label><Link to="/forgot-password" className="text-xs font-semibold text-forest hover:underline">Forgot password?</Link></div>
      {mutation.isError && <p className="rounded-xl bg-rose-50 px-3 py-2 text-sm text-rose-700">{errorMessage(mutation.error)}</p>}
      <Button type="submit" className="w-full py-3" disabled={mutation.isPending}>{mutation.isPending ? 'Signing in…' : 'Sign in'}</Button>
    </form>
  </AuthFrame>
}

export function Register() {
  const { register } = useAuth(); const navigate = useNavigate()
  const countries = useQuery({ queryKey: ['countries'], queryFn: countryApi.list, staleTime: 10 * 60_000 })
  const schema = z.object({ fullName: z.string().trim().min(1, 'Enter your full name.').max(160), email: z.string().email('Enter a valid email address.'), password: z.string().min(10, 'Use at least 10 characters.').max(72), confirmPassword: z.string(), countryCode: z.string().min(1, 'Choose a country.'), yearsExperience: z.coerce.number().int().min(0).max(60) }).refine(value => value.password === value.confirmPassword, { path: ['confirmPassword'], message: 'Passwords do not match.' })
  type RegisterInput = z.input<typeof schema>
  type RegisterValues = z.output<typeof schema>
  const form = useForm<RegisterInput, unknown, RegisterValues>({ resolver: zodResolver(schema), defaultValues: { fullName: '', email: '', password: '', confirmPassword: '', countryCode: '', yearsExperience: 0 } })
  const mutation = useMutation({ mutationFn: (values: z.infer<typeof schema>) => register(values.email, values.password, values.fullName, { countryCode: values.countryCode, yearsExperience: values.yearsExperience }), onSuccess: () => navigate('/profile') })
  const field = form.register
  return <AuthFrame heading="Create your account" copy="Set up your profile and make the next job search yours." alt="Already have an account?" altLink="/login">
    <form onSubmit={form.handleSubmit(values => mutation.mutate(values))} className="space-y-4">
      <Field label="Full name"><div className="relative"><UserRound size={16} className="absolute left-3.5 top-3 text-slate-400" /><input className={`${inputClass} pl-10`} autoComplete="name" maxLength={160} {...field('fullName')} placeholder="Your name" /></div>{form.formState.errors.fullName && <span className="mt-1 block text-xs text-rose-700">{form.formState.errors.fullName.message}</span>}</Field>
      <Field label="Email address"><div className="relative"><Mail size={16} className="absolute left-3.5 top-3 text-slate-400" /><input className={`${inputClass} pl-10`} type="email" autoComplete="email" {...field('email')} placeholder="you@example.com" /></div>{form.formState.errors.email && <span className="mt-1 block text-xs text-rose-700">{form.formState.errors.email.message}</span>}</Field>
      <div className="grid gap-4 sm:grid-cols-2"><Field label="Country">{countries.isError ? <p className="text-xs text-rose-700">Country list could not be loaded. Refresh to retry.</p> : <select className={inputClass} {...field('countryCode')}><option value="">Choose country</option>{countries.data?.map(item => <option key={item.code} value={item.code}>{item.name}</option>)}</select>}{form.formState.errors.countryCode && <span className="mt-1 block text-xs text-rose-700">{form.formState.errors.countryCode.message}</span>}</Field><Field label="Experience level"><select className={inputClass} {...field('yearsExperience')}><option value="0">Entry level · 0 years</option>{[1, 2, 3, 5, 8, 10].map(year => <option key={year} value={year}>{year === 1 ? '1 year' : `${year}+ years`}</option>)}</select></Field></div>
      <Field label="Password" hint="Use 10 or more characters."><div className="relative"><LockKeyhole size={16} className="absolute left-3.5 top-3 text-slate-400" /><input className={`${inputClass} pl-10`} type="password" autoComplete="new-password" maxLength={72} {...field('password')} placeholder="Create a secure password" /></div>{form.formState.errors.password && <span className="mt-1 block text-xs text-rose-700">{form.formState.errors.password.message}</span>}</Field>
      <Field label="Confirm password"><input className={inputClass} type="password" autoComplete="new-password" {...field('confirmPassword')} placeholder="Type your password again" />{form.formState.errors.confirmPassword && <span className="mt-1 block text-xs text-rose-700">{form.formState.errors.confirmPassword.message}</span>}</Field>
      {mutation.isError && <p className="rounded-xl bg-rose-50 px-3 py-2 text-sm text-rose-700">{errorMessage(mutation.error)}</p>}
      <Button type="submit" className="w-full py-3" disabled={mutation.isPending}>{mutation.isPending ? 'Creating account…' : 'Create account'}</Button>
      <p className="text-center text-[11px] leading-5 text-muted">By continuing, you agree to use provider listings through their original application links.</p>
    </form>
  </AuthFrame>
}

export function ForgotPassword() {
  const [email, setEmail] = useState(''); const [sent, setSent] = useState(false)
  const mutation = useMutation({ mutationFn: () => authApi.forgot(email), onSuccess: () => setSent(true) })
  return <AuthFrame heading="Reset your password" copy="Enter the email on your account. If email delivery is configured, we’ll send a time-limited reset link." alt="Remember your password?" altLink="/login">
    {sent ? <div className="rounded-2xl border border-forest/15 bg-mint/50 p-4 text-sm leading-6 text-forest">If the account exists and email delivery is configured, reset instructions will be sent.</div> : <form onSubmit={e => { e.preventDefault(); mutation.mutate() }} className="space-y-4"><Field label="Email address"><input className={inputClass} type="email" required value={email} onChange={e => setEmail(e.target.value)} placeholder="you@example.com" /></Field>{mutation.isError && <p className="text-sm text-rose-700">{errorMessage(mutation.error)}</p>}<Button type="submit" className="w-full" disabled={mutation.isPending}>{mutation.isPending ? 'Sending…' : 'Send reset link'}</Button></form>}
  </AuthFrame>
}

export function ResetPassword() {
  const [searchParams] = useSearchParams(); const navigate = useNavigate(); const token = searchParams.get('token') || ''
  const [password, setPassword] = useState('')
  const mutation = useMutation({ mutationFn: () => authApi.reset(token, password), onSuccess: () => navigate('/login') })
  return <AuthFrame heading="Choose a new password" copy="Reset links expire after 30 minutes and can only be used once." alt="Back to sign in?" altLink="/login">
    <form onSubmit={e => { e.preventDefault(); mutation.mutate() }} className="space-y-4"><Field label="New password" hint="Use 10 or more characters."><input className={inputClass} type="password" minLength={10} maxLength={72} required value={password} onChange={e => setPassword(e.target.value)} placeholder="New password" /></Field>{!token && <p className="text-sm text-rose-700">This reset link is missing its token. Request a new link.</p>}{mutation.isError && <p className="text-sm text-rose-700">{errorMessage(mutation.error)}</p>}<Button type="submit" disabled={!token || mutation.isPending} className="w-full">{mutation.isPending ? 'Updating…' : 'Update password'}</Button></form>
  </AuthFrame>
}

