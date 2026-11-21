package com.careernexus.job;

import com.careernexus.application.SearchHistory;
import com.careernexus.application.SearchHistoryRepository;
import com.careernexus.company.Company;
import com.careernexus.company.CompanyRepository;
import com.careernexus.location.CountryRepository;
import com.careernexus.profile.UserProfile;
import com.careernexus.provider.JobProvider;
import com.careernexus.provider.ProviderJob;
import com.careernexus.provider.ProviderCallException;
import com.careernexus.provider.SearchCriteria;
import com.careernexus.recommendation.RecommendationService;
import com.careernexus.recommendation.JobRecommendation;
import com.careernexus.recommendation.JobRecommendationRepository;
import com.careernexus.skill.Skill;
import com.careernexus.skill.SkillCatalogService;
import com.careernexus.user.AppUserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class JobSearchService {
    private final List<JobProvider> providers;
    private final JobRepository jobs;
    private final JobSourceRepository sources;
    private final CompanyRepository companies;
    private final SkillCatalogService skillCatalog;
    private final ExperienceExtractor experienceExtractor;
    private final JobViewMapper mapper;
    private final RecommendationService recommendations;
    private final AppUserRepository users;
    private final SearchHistoryRepository history;
    private final CountryRepository countries;
    private final JobRecommendationRepository recommendationRows;
    private final ObjectMapper objectMapper;

    public JobSearchService(List<JobProvider> providers, JobRepository jobs, JobSourceRepository sources,
                            CompanyRepository companies, SkillCatalogService skillCatalog, ExperienceExtractor experienceExtractor,
                            JobViewMapper mapper, RecommendationService recommendations, AppUserRepository users,
                            SearchHistoryRepository history, CountryRepository countries,
                            JobRecommendationRepository recommendationRows, ObjectMapper objectMapper) {
        this.providers = providers; this.jobs = jobs; this.sources = sources; this.companies = companies; this.skillCatalog = skillCatalog;
        this.experienceExtractor = experienceExtractor; this.mapper = mapper; this.recommendations = recommendations;
        this.users = users; this.history = history; this.countries = countries;
        this.recommendationRows = recommendationRows; this.objectMapper = objectMapper;
    }

    @Transactional
    public SearchResult search(SearchQuery query, String email) {
        List<JobProvider> enabled = providers.stream().filter(JobProvider::enabled).toList();
        if (enabled.isEmpty()) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "No job sources are configured. Add Adzuna credentials or Greenhouse/Lever public board identifiers to the backend environment.");
        var searchUser = users.findByEmailIgnoreCase(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (query.skills() != null) query.skills().stream().filter(skill -> skill != null && !skill.isBlank())
                .forEach(skill -> skillCatalog.findOrCreate(skill, true));
        SearchHistory searchRun = new SearchHistory(); searchRun.setUser(searchUser); searchRun.setQueryText(query.summary());
        searchRun = history.save(searchRun);
        SearchCriteria criteria = new SearchCriteria(query.countryCode(), query.state(), query.city(), query.title(), query.skills());
        List<String> successfulSources = new ArrayList<>(), warnings = new ArrayList<>();
        Map<Long, Job> returnedJobs = new LinkedHashMap<>();
        int failed = 0;
        for (JobProvider provider : enabled) {
            try {
                List<ProviderJob> fresh = provider.search(criteria);
                for (ProviderJob providerJob : fresh) {
                    Job saved = persist(providerJob);
                    if (saved != null && saved.getId() != null) returnedJobs.put(saved.getId(), saved);
                }
                successfulSources.add(provider.name());
            } catch (Exception e) {
                failed++;
                warnings.add(provider.name() + " could not be reached for this search.");
            }
        }
        if (failed == enabled.size()) throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Configured job sources could not be reached. Try again shortly.");
        searchRun.setJobs(new HashSet<>(returnedJobs.values()));
        history.save(searchRun);

        Instant cutoff = Instant.now().minus(21, ChronoUnit.DAYS);
        String countryCode = clean(query.countryCode());
        String countryName = countryCode == null ? null : countries.findByCodeIgnoreCase(countryCode).map(country -> country.getName()).orElse(null);
        boolean hasSkills = query.skills() != null && !query.skills().isEmpty();
        List<String> skillNames = hasSkills ? query.skills().stream().map(s -> s.toLowerCase(Locale.ROOT)).distinct().toList() : List.of("__no_skill__");
        Instant postedAfter = query.postedWithinDays() == null ? null : Instant.now().minus(query.postedWithinDays(), ChronoUnit.DAYS);
        String sort = Set.of("match", "newest", "salary").contains(query.sort() == null ? "" : query.sort().toLowerCase(Locale.ROOT)) ? query.sort().toLowerCase(Locale.ROOT) : "newest";
        var pageRequest = org.springframework.data.domain.PageRequest.of(query.page(), query.size());
        var resultPage = jobs.searchAvailable(searchRun.getId(), cutoff, countryCode, countryName, clean(query.city()), clean(query.state()), clean(query.title()),
                clean(query.company()), hasSkills, skillNames, normalizeEmployment(query.employmentType()), normalizeWorkplace(query.workplaceType()),
                query.minSalary(), query.maxSalary(), clean(query.salaryCurrency()), clean(query.salaryInterval()), postedAfter, query.experienceYears(), sort, pageRequest);
        UserProfile profile = users.findByEmailIgnoreCase(email).map(u -> u.getProfile()).orElse(null);
        UserProfile scoringProfile = hasSearchProfile(query) ? searchProfile(profile, query) : profile;
        UserProfile candidateProfile = scoringProfile;
        List<JobView> page = resultPage.getContent().stream().map(job -> mapper.map(job, candidateProfile == null ? null : recommendations.evaluate(job, candidateProfile, query.experienceYears()))).toList();
        return new SearchResult(page, resultPage.getTotalElements(), query.page(), query.size(), resultPage.getTotalPages(), successfulSources, warnings);
    }

    @Transactional
    public Job upsert(ProviderJob external) { return persist(external); }

    private static boolean hasSearchProfile(SearchQuery query) {
        return query.experienceYears() != null || query.countryCode() != null || query.city() != null || query.title() != null || query.skills() != null && !query.skills().isEmpty();
    }

    private UserProfile searchProfile(UserProfile current, SearchQuery query) {
        UserProfile candidate = new UserProfile();
        if (current != null) {
            candidate.setYearsExperience(current.getYearsExperience()); candidate.setCountryCode(current.getCountryCode());
            candidate.setCity(current.getCity()); candidate.setTargetTitle(current.getTargetTitle()); candidate.setSkills(new HashSet<>(current.getSkills()));
        } else {
            candidate.setYearsExperience(0); candidate.setSkills(new HashSet<>());
        }
        if (query.experienceYears() != null) candidate.setYearsExperience(query.experienceYears());
        if (query.countryCode() != null) candidate.setCountryCode(query.countryCode());
        if (query.city() != null) candidate.setCity(query.city());
        if (query.title() != null) candidate.setTargetTitle(query.title());
        if (query.skills() != null && !query.skills().isEmpty()) candidate.setSkills(query.skills().stream().map(name -> skillCatalog.findOrCreate(name, true)).collect(java.util.stream.Collectors.toSet()));
        return candidate;
    }

    private Job persist(ProviderJob external) {
        if (external.providerJobId() == null || external.title() == null || external.applicationUrl() == null) return null;
        String url = canonicalize(external.applicationUrl());
        if (url == null || url.length() > 700) return null;
        var sourceOpt = sources.findByProviderAndProviderJobId(external.provider(), external.providerJobId());
        Job job = sourceOpt.map(JobSource::getJob).orElseGet(() -> jobs.findFirstByCanonicalUrlIgnoreCase(url).orElseGet(Job::new));
        boolean created = job.getId() == null;
        if (created) job.setCanonicalUrl(url);
        job.setTitle(external.title().trim());
        job.setCountryCode(validCountryCode(external.countryCode()));
        job.setLocation(external.location());
        job.setDescription(external.description());
        job.setEmploymentType(normalizeEmployment(external.employmentType()));
        job.setWorkplaceType(normalizeWorkplace(external.workplaceType()));
        job.setSalaryMin(external.salaryMin()); job.setSalaryMax(external.salaryMax());
        job.setSalaryCurrency(external.salaryCurrency() != null && external.salaryCurrency().matches("(?i)[a-z]{3}") ? external.salaryCurrency().toUpperCase(Locale.ROOT) : null);
        job.setSalaryInterval(normalizeSalaryInterval(external.salaryInterval()));
        job.setPostedAt(external.postedAt()); job.setLastVerifiedAt(Instant.now()); job.setActive(true);
        var experience = experienceExtractor.parse(external.description());
        job.setRequiredExperienceMin(experience.requiredMin()); job.setRequiredExperienceMax(experience.requiredMax());
        job.setPreferredExperienceMin(experience.preferredMin()); job.setPreferredExperienceMax(experience.preferredMax());
        if (external.companyName() != null && !external.companyName().isBlank()) {
            Company company = companies.findFirstByNameIgnoreCase(external.companyName().trim()).orElseGet(() -> {
                Company createdCompany = new Company(); createdCompany.setName(external.companyName().trim()); return companies.save(createdCompany);
            });
            job.setCompany(company);
        }
        Set<Skill> all = skillCatalog.extract(external.description());
        Set<Skill> preferred = skillCatalog.extractPreferred(external.description(), all);
        Set<Skill> required = new HashSet<>(all); required.removeAll(preferred);
        job.setRequiredSkills(required); job.setAdditionalSkills(preferred);
        jobs.save(job);
        JobSource source = sourceOpt.orElseGet(JobSource::new);
        source.setJob(job); source.setProvider(external.provider()); source.setProviderJobId(external.providerJobId());
        source.setOriginalUrl(external.applicationUrl()); source.setLastVerifiedAt(Instant.now());
        sources.save(source);
        job.getSources().add(source);
        return job;
    }

    @Transactional(readOnly = true)
    public JobView detail(Long id, String email) {
        Job job = jobs.findDetailedById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));
        var profile = users.findByEmailIgnoreCase(email).map(u -> u.getProfile()).orElse(null);
        return mapper.map(job, profile == null ? null : recommendations.evaluate(job, profile));
    }

    @Transactional
    public List<JobView> recommendations(String email, int page, int size) {
        var user = users.findByEmailIgnoreCase(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User profile not found"));
        UserProfile profile = user.getProfile();
        if (profile == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User profile not found");
        List<Job> candidates = jobs.findAllByActiveTrueAndLastVerifiedAtAfterOrderByPostedAtDesc(Instant.now().minus(21, ChronoUnit.DAYS));
        List<Job> selected = candidates.stream().sorted(Comparator.comparingDouble((Job job) -> recommendations.evaluate(job, profile).scorePercent()).reversed())
                .skip((long) page * size).limit(size).toList();
        return selected.stream().map(job -> {
            var match = recommendations.evaluate(job, profile);
            JobRecommendation row = recommendationRows.findByUser_IdAndJob_Id(user.getId(), job.getId()).orElseGet(JobRecommendation::new);
            row.setUser(user); row.setJob(job); row.setMatchScore(java.math.BigDecimal.valueOf(match.scorePercent()).setScale(2, java.math.RoundingMode.HALF_UP));
            row.setExplanationJson(objectMapper.valueToTree(match));
            row.setGeneratedAt(Instant.now()); recommendationRows.save(row);
            return mapper.map(job, match);
        }).toList();
    }

    @Transactional(readOnly = true)
    public JobView findView(Long id, String email) { return detail(id, email); }

    private String validCountryCode(String code) { return code != null && code.matches("(?i)[a-z]{2}") ? code.toUpperCase(Locale.ROOT) : null; }
    private static String clean(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private static boolean contains(String text, String query) { return text != null && query != null && text.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT)); }
    private static String normalizeEmployment(String value) {
        if (value == null || value.isBlank()) return null;
        String lower = value.toLowerCase(Locale.ROOT);
        if (lower.contains("full")) return "FULL_TIME"; if (lower.contains("part")) return "PART_TIME";
        if (lower.contains("contract") || lower.contains("temporary")) return "CONTRACT";
        if (lower.contains("intern")) return "INTERNSHIP"; return value.toUpperCase(Locale.ROOT).replace(' ', '_');
    }
    private static String normalizeWorkplace(String value) {
        if (value == null || value.isBlank()) return null;
        String lower = value.toLowerCase(Locale.ROOT);
        if (lower.contains("remote")) return "REMOTE"; if (lower.contains("hybrid")) return "HYBRID";
        if (lower.contains("on-site") || lower.contains("onsite")) return "ONSITE"; return value.toUpperCase(Locale.ROOT);
    }
    private static String normalizeSalaryInterval(String value) {
        if (value == null || value.isBlank()) return null;
        String lower = value.toLowerCase(Locale.ROOT);
        if (lower.contains("year")) return "YEAR"; if (lower.contains("month")) return "MONTH";
        if (lower.contains("hour")) return "HOUR"; if (lower.contains("week")) return "WEEK";
        if (lower.contains("day")) return "DAY"; return value.toUpperCase(Locale.ROOT);
    }
    private static String canonicalize(String value) {
        try { URI uri = URI.create(value.trim()); if (!("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme())) || uri.getHost() == null) return null;
            return new URI(uri.getScheme().toLowerCase(Locale.ROOT), uri.getUserInfo(), uri.getHost().toLowerCase(Locale.ROOT), uri.getPort(), uri.getPath(), uri.getQuery(), null).toASCIIString();
        } catch (Exception e) { return null; }
    }

    public record SearchQuery(String countryCode, String state, String city, List<String> skills, Integer experienceYears,
                              String employmentType, String workplaceType, java.math.BigDecimal minSalary, java.math.BigDecimal maxSalary,
                              String salaryCurrency, String salaryInterval,
                              Integer postedWithinDays, String company, String title, int page, int size, String sort) {
        public String summary() {
            String value = String.join(" ", java.util.stream.Stream.of(title, company, countryCode, state, city, skills == null ? null : String.join(",", skills))
                    .filter(s -> s != null && !s.isBlank()).toList());
            return value.substring(0, Math.min(500, value.length()));
        }
    }
}

