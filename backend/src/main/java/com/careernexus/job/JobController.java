package com.careernexus.job;

import com.careernexus.common.ApiResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobController {
    private final JobSearchService search;
    public JobController(JobSearchService search) { this.search = search; }

    @GetMapping("/search")
    public ApiResponse<SearchResult> search(Authentication auth,
            @RequestParam(required = false) String country, @RequestParam(required = false) String state,
            @RequestParam(required = false) String city, @RequestParam(required = false) List<String> skills,
            @RequestParam(required = false) Integer experienceYears, @RequestParam(required = false) String employmentType,
            @RequestParam(required = false) String workMode, @RequestParam(required = false) BigDecimal minSalary,
            @RequestParam(required = false) BigDecimal maxSalary, @RequestParam(required = false) String salaryCurrency,
            @RequestParam(required = false) String salaryInterval, @RequestParam(required = false) Integer postedWithinDays,
            @RequestParam(required = false) String company, @RequestParam(required = false) String title,
            @RequestParam(defaultValue = "0") @Min(0) @Max(100_000) int page, @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "newest") String sort) {
        if (page < 0 || page > 100_000 || size < 1 || size > 100) throw new IllegalArgumentException("Page must be between 0 and 100000 and size must be between 1 and 100");
        if (country != null && !country.isBlank() && !country.matches("(?i)[a-z]{2}")) throw new IllegalArgumentException("Country must be an ISO alpha-2 code");
        if (tooLong(state, 100) || tooLong(city, 160) || tooLong(title, 240) || tooLong(company, 200)
                || tooLong(employmentType, 40) || tooLong(workMode, 30) || tooLong(salaryCurrency, 3) || tooLong(salaryInterval, 24))
            throw new IllegalArgumentException("A search filter is longer than its supported length");
        if (experienceYears != null && (experienceYears < 0 || experienceYears > 60)) throw new IllegalArgumentException("Experience must be between 0 and 60 years");
        if (postedWithinDays != null && (postedWithinDays < 1 || postedWithinDays > 365)) throw new IllegalArgumentException("Date posted must be between 1 and 365 days");
        if (minSalary != null && minSalary.signum() < 0 || maxSalary != null && maxSalary.signum() < 0)
            throw new IllegalArgumentException("Salary values must be zero or greater");
        if (minSalary != null && maxSalary != null && minSalary.compareTo(maxSalary) > 0)
            throw new IllegalArgumentException("Minimum salary cannot exceed maximum salary");
        boolean salaryComparison = minSalary != null || maxSalary != null || "salary".equalsIgnoreCase(sort);
        if (salaryComparison && (salaryCurrency == null || !salaryCurrency.matches("(?i)[a-z]{3}")))
            throw new IllegalArgumentException("Choose a three-letter salary currency when filtering or sorting by salary");
        if (salaryComparison && (salaryInterval == null || !java.util.Set.of("YEAR", "MONTH", "WEEK", "DAY", "HOUR").contains(salaryInterval.toUpperCase(java.util.Locale.ROOT))))
            throw new IllegalArgumentException("Choose a pay period when filtering or sorting by salary");
        List<String> cleanSkills = skills == null ? List.of() : skills.stream().flatMap(s -> Arrays.stream(s.split(","))).map(String::trim).filter(s -> !s.isBlank()).distinct().limit(30).toList();
        if (cleanSkills.stream().anyMatch(skill -> skill.length() > 100)) throw new IllegalArgumentException("Each skill must be 100 characters or fewer");
        var query = new JobSearchService.SearchQuery(country, state, city, cleanSkills, experienceYears, employmentType, workMode,
                minSalary, maxSalary, salaryCurrency, salaryInterval, postedWithinDays, company, title, page, size, sort);
        return ApiResponse.ok(search.search(query, auth.getName()));
    }

    private static boolean tooLong(String value, int max) { return value != null && value.length() > max; }

    @GetMapping("/recommended")
    public ApiResponse<List<JobView>> recommended(Authentication auth,
            @RequestParam(defaultValue = "0") @Min(0) int page, @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        if (page < 0 || size < 1 || size > 100) throw new IllegalArgumentException("Page must be non-negative and size must be between 1 and 100");
        return ApiResponse.ok(search.recommendations(auth.getName(), page, size));
    }
    @GetMapping("/{id}") public ApiResponse<JobView> detail(Authentication auth, @PathVariable Long id) { return ApiResponse.ok(search.detail(id, auth.getName())); }
}

