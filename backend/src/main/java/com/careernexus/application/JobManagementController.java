package com.careernexus.application;

import com.careernexus.common.ApiResponse;
import com.careernexus.job.JobView;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
public class JobManagementController {
    private final JobManagementService management;
    public JobManagementController(JobManagementService management) { this.management = management; }
    @PostMapping("/api/jobs/{id}/save") @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<JobView> save(Authentication auth, @PathVariable Long id) { return ApiResponse.ok(management.save(auth.getName(), id)); }
    @DeleteMapping("/api/jobs/{id}/save") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unsave(Authentication auth, @PathVariable Long id) { management.unsave(auth.getName(), id); }
    @GetMapping("/api/jobs/saved")
    public ApiResponse<List<JobView>> saved(Authentication auth) { return ApiResponse.ok(management.saved(auth.getName())); }
    @PostMapping("/api/applications") @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ApplicationDtos.ApplicationView> create(Authentication auth, @Valid @RequestBody ApplicationDtos.CreateApplication body) { return ApiResponse.ok(management.create(auth.getName(), body)); }
    @GetMapping("/api/applications")
    public ApiResponse<List<ApplicationDtos.ApplicationView>> applications(Authentication auth) { return ApiResponse.ok(management.applications(auth.getName())); }
    @PutMapping("/api/applications/{id}")
    public ApiResponse<ApplicationDtos.ApplicationView> update(Authentication auth, @PathVariable Long id, @Valid @RequestBody ApplicationDtos.UpdateApplication body) { return ApiResponse.ok(management.update(auth.getName(), id, body)); }
    @DeleteMapping("/api/applications/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Authentication auth, @PathVariable Long id) { management.deleteApplication(auth.getName(), id); }
}

