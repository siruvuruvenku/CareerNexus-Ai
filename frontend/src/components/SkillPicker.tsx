import { useMemo, useState } from 'react'
import { X } from 'lucide-react'

export function SkillPicker({ values, onChange, catalog }: { values: string[]; onChange: (values: string[]) => void; catalog: string[] }) {
  const [text, setText] = useState('')
  const options = useMemo(() => catalog.filter(skill => skill.toLowerCase().includes(text.toLowerCase()) && !values.some(value => value.toLowerCase() === skill.toLowerCase())).slice(0, 8), [catalog, text, values])
  const add = (value: string) => {
    const clean = value.trim()
    if (clean && !values.some(item => item.toLowerCase() === clean.toLowerCase())) onChange([...values, clean])
    setText('')
  }
  return <div>
    <div className="flex flex-wrap gap-2 rounded-xl border border-line bg-white p-2 focus-within:border-forest/60 focus-within:ring-4 focus-within:ring-forest/10">
      {values.map(skill => <span key={skill} className="inline-flex items-center gap-1 rounded-lg bg-mint px-2.5 py-1 text-xs font-semibold text-forest">{skill}<button type="button" onClick={() => onChange(values.filter(item => item !== skill))} aria-label={`Remove ${skill}`}><X size={13} /></button></span>)}
      <input aria-label="Search or add skills" className="min-w-[140px] flex-1 border-0 bg-transparent px-2 py-1 text-sm outline-none focus:ring-0" value={text} onChange={event => setText(event.target.value)} onKeyDown={event => { if (event.key === 'Enter' && text.trim()) { event.preventDefault(); add(text) } }} placeholder="Search or add a skill" />
    </div>
    {text && <div className="mt-1 flex flex-wrap gap-1">{options.map(skill => <button key={skill} type="button" onClick={() => add(skill)} className="rounded-lg border border-line bg-white px-2.5 py-1.5 text-xs text-slate-600 hover:border-forest/40 hover:text-forest">{skill}</button>)}<button type="button" onClick={() => add(text)} className="rounded-lg border border-dashed border-forest/30 px-2.5 py-1.5 text-xs font-semibold text-forest">Add “{text.trim()}”</button></div>}
  </div>
}
