export interface ApiResponse<T> {
  success: boolean
  data: T
  message?: string | null
}

export interface AuthUser { id: number; email: string; fullName: string }
export interface AuthResponse { accessToken: string; tokenType: string; expiresInSeconds: number; user: AuthUser }
export interface Country { code: string; name: string }
export interface Skill { id: number; name: string; category: string; custom: boolean }

export interface MatchDetails {
  scorePercent: number
  matchingSkills: string[]
  missingSkills: string[]
  experienceCompatibility: string
  locationCompatible: boolean
  explanation: string
}

export interface Job {
  id: number
  title: string
  companyName: string
  location: string | null
  countryCode: string | null
  description: string | null
  requiredSkills: string[]
  additionalSkills: string[]
  requiredExperienceMin: number | null
  requiredExperienceMax: number | null
  preferredExperienceMin: number | null
  preferredExperienceMax: number | null
  employmentType: string | null
  workplaceType: string | null
  salaryMin: number | null
  salaryMax: number | null
  salaryCurrency: string | null
  salaryInterval: string | null
  postedAt: string | null
  source: string
  sourceUrl: string
  sourceAttributionUrl: string | null
  lastVerifiedAt: string
  match: MatchDetails | null
}

export interface SearchResult {
  items: Job[]
  total: number
  page: number
  size: number
  totalPages: number
  sources: string[]
  sourceWarnings: string[]
}

export interface Profile {
  id: number
  email: string
  fullName: string
  targetTitle: string | null
  countryCode: string | null
  city: string | null
  yearsExperience: number | null
  summary: string | null
  skills: string[]
  resumeUploaded: boolean
}

export interface Application {
  id: number
  jobId: number
  job: Job
  status: string
  interviewAt: string | null
  notes: string | null
  updatedAt: string
}

export interface Breakdown { label: string; count: number }
export interface DashboardMetric { label: string; value: number; hint: string }
export interface DashboardSummary {
  metrics: DashboardMetric[]
  applicationStatuses: Record<string, number>
  jobsByCity: Breakdown[]
  jobsByTechnology: Breakdown[]
  jobsByCompany: Breakdown[]
  expiryDataAvailable: boolean
  availabilityNote: string
}

