package com.careernexus.application;

import com.careernexus.job.JobView;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public final class ApplicationDtos {
    private ApplicationDtos() {}
    public record CreateApplication(@NotNull Long jobId, @NotBlank String status, Instant interviewAt, @Size(max = 5000) String notes) {}
    public record UpdateApplication(@NotBlank String status, Instant interviewAt, @Size(max = 5000) String notes) {}
    public record ApplicationView(Long id, Long jobId, JobView job, String status, Instant interviewAt, String notes, Instant updatedAt) {}
}

