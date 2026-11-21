package com.careernexus.application;

import com.careernexus.job.Job;
import com.careernexus.job.JobRepository;
import com.careernexus.job.JobSearchService;
import com.careernexus.job.JobView;
import com.careernexus.job.JobViewMapper;
import com.careernexus.user.AppUser;
import com.careernexus.user.AppUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.util.List;
import java.util.Set;

@Service
public class JobManagementService {
    private static final Set<String> STATUSES = Set.of("INTERESTED", "SAVED", "APPLIED", "ASSESSMENT", "INTERVIEW", "REJECTED", "OFFER");
    private final AppUserRepository users;
    private final JobRepository jobs;
    private final SavedJobRepository savedJobs;
    private final JobApplicationRepository applications;
    private final JobSearchService search;
    private final JobViewMapper mapper;
    public JobManagementService(AppUserRepository users, JobRepository jobs, SavedJobRepository savedJobs,
                                JobApplicationRepository applications, JobSearchService search, JobViewMapper mapper) {
        this.users = users; this.jobs = jobs; this.savedJobs = savedJobs; this.applications = applications; this.search = search; this.mapper = mapper;
    }

    @Transactional
    public JobView save(String email, Long jobId) {
        AppUser user = user(email); Job job = job(jobId);
        if (savedJobs.findByUser_IdAndJob_Id(user.getId(), jobId).isEmpty()) {
            SavedJob saved = new SavedJob(); saved.setUser(user); saved.setJob(job); savedJobs.save(saved);
        }
        return search.findView(jobId, email);
    }
    @Transactional(readOnly = true)
    public List<JobView> saved(String email) {
        AppUser user = user(email);
        return savedJobs.findAllByUser_IdOrderByCreatedAtDesc(user.getId()).stream().map(item -> mapper.map(item.getJob(), null)).toList();
    }
    @Transactional
    public void unsave(String email, Long jobId) { savedJobs.deleteByUser_IdAndJob_Id(user(email).getId(), jobId); }

    @Transactional
    public ApplicationDtos.ApplicationView create(String email, ApplicationDtos.CreateApplication body) {
        AppUser user = user(email); Job job = job(body.jobId());
        String status = normalizeStatus(body.status());
        JobApplication application = applications.findByUser_IdAndJob_Id(user.getId(), body.jobId()).orElseGet(JobApplication::new);
        application.setUser(user); application.setJob(job); application.setStatus(status); application.setInterviewAt(body.interviewAt());
        application.setNotes(body.notes()); application.setUpdatedAt(Instant.now());
        application = applications.save(application);
        return view(application, email);
    }
    @Transactional
    public ApplicationDtos.ApplicationView update(String email, Long id, ApplicationDtos.UpdateApplication body) {
        AppUser user = user(email);
        JobApplication application = applications.findById(id).filter(a -> a.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));
        application.setStatus(normalizeStatus(body.status())); application.setInterviewAt(body.interviewAt());
        application.setNotes(body.notes()); application.setUpdatedAt(Instant.now());
        return view(application, email);
    }
    @Transactional(readOnly = true)
    public List<ApplicationDtos.ApplicationView> applications(String email) {
        AppUser user = user(email);
        return applications.findAllByUser_IdOrderByUpdatedAtDesc(user.getId()).stream().map(a -> view(a, email)).toList();
    }
    @Transactional
    public void deleteApplication(String email, Long id) {
        AppUser user = user(email);
        JobApplication application = applications.findById(id).filter(a -> a.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));
        applications.delete(application);
    }
    private ApplicationDtos.ApplicationView view(JobApplication app, String email) {
        JobView jobView = search.findView(app.getJob().getId(), email);
        return new ApplicationDtos.ApplicationView(app.getId(), app.getJob().getId(), jobView, app.getStatus(), app.getInterviewAt(), app.getNotes(), app.getUpdatedAt());
    }
    private AppUser user(String email) { return users.findByEmailIgnoreCase(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")); }
    private Job job(Long id) { return jobs.findDetailedById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found")); }
    private static String normalizeStatus(String status) {
        String value = status.trim().toUpperCase(java.util.Locale.ROOT).replace(' ', '_');
        if (!STATUSES.contains(value)) throw new IllegalArgumentException("Status must be Interested, Saved, Applied, Assessment, Interview, Rejected, or Offer");
        return value;
    }
}

