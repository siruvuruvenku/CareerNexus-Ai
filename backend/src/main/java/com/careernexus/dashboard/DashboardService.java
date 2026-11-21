package com.careernexus.dashboard;

import com.careernexus.application.JobApplicationRepository;
import com.careernexus.application.SavedJobRepository;
import com.careernexus.job.Job;
import com.careernexus.job.JobRepository;
import com.careernexus.recommendation.RecommendationService;
import com.careernexus.user.AppUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DashboardService {
    private final AppUserRepository users;
    private final JobRepository jobs;
    private final SavedJobRepository saved;
    private final JobApplicationRepository applications;
    private final RecommendationService recommendations;
    public DashboardService(AppUserRepository users, JobRepository jobs, SavedJobRepository saved,
                           JobApplicationRepository applications, RecommendationService recommendations) {
        this.users = users; this.jobs = jobs; this.saved = saved; this.applications = applications; this.recommendations = recommendations;
    }
    @Transactional(readOnly = true)
    public DashboardDtos.Summary get(String email) {
        var user = users.findByEmailIgnoreCase(email).orElseThrow();
        Instant now = Instant.now();
        List<Job> current = jobs.findAllByActiveTrueAndLastVerifiedAtAfterOrderByPostedAtDesc(now.minus(21, ChronoUnit.DAYS));
        long newJobs = current.stream().filter(j -> j.getPostedAt() != null && j.getPostedAt().isAfter(now.minus(7, ChronoUnit.DAYS))).count();
        long recommended = user.getProfile() == null ? 0 : current.stream().filter(j -> recommendations.evaluate(j, user.getProfile()).scorePercent() >= 70).count();
        long savedCount = saved.countByUser_Id(user.getId());
        long appliedCount = applications.countByUser_IdAndStatusIgnoreCase(user.getId(), "APPLIED");
        Map<String, Long> statusCounts = new LinkedHashMap<>();
        for (String status : List.of("INTERESTED", "SAVED", "APPLIED", "ASSESSMENT", "INTERVIEW", "REJECTED", "OFFER")) {
            long count = applications.countByUser_IdAndStatusIgnoreCase(user.getId(), status);
            if (count > 0) statusCounts.put(status, count);
        }
        List<DashboardDtos.Metric> metrics = List.of(
                new DashboardDtos.Metric("Available jobs", current.size(), "Verified by connected sources in the last 21 days"),
                new DashboardDtos.Metric("New this week", newJobs, "Source posting dates, when provided"),
                new DashboardDtos.Metric("Strong matches", recommended, "Estimated profile match of 70% or higher"),
                new DashboardDtos.Metric("Saved jobs", savedCount, "In your saved list"),
                new DashboardDtos.Metric("Applied", appliedCount, "Marked as applied"));
        return new DashboardDtos.Summary(metrics, statusCounts, top(current.stream().map(Job::getLocation).filter(v -> v != null && !v.isBlank()).toList()),
                top(current.stream().flatMap(j -> j.getRequiredSkills().stream()).map(s -> s.getName()).toList()),
                top(current.stream().map(j -> j.getCompany() == null ? null : j.getCompany().getName()).filter(v -> v != null).toList()),
                false, "Connected sources do not expose consistent job expiry dates.");
    }
    private static List<DashboardDtos.Breakdown> top(List<String> values) {
        return values.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting())).entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()).thenComparing(Map.Entry.comparingByKey(String.CASE_INSENSITIVE_ORDER)))
                .limit(6).map(e -> new DashboardDtos.Breakdown(e.getKey(), e.getValue())).toList();
    }
}

