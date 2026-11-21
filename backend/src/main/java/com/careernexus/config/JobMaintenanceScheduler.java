package com.careernexus.config;

import com.careernexus.job.JobRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
public class JobMaintenanceScheduler {
    private final JobRepository jobs;
    public JobMaintenanceScheduler(JobRepository jobs) { this.jobs = jobs; }
    @Scheduled(cron = "0 15 */6 * * *")
    @Transactional
    public void deactivateStaleJobs() { jobs.deactivateUnverifiedBefore(Instant.now().minus(21, ChronoUnit.DAYS)); }
}

