package com.careernexus.application;

import com.careernexus.job.Job;
import com.careernexus.user.AppUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;

@Entity
@Table(name = "job_applications", uniqueConstraints = @UniqueConstraint(name = "uq_application_user_job", columnNames = {"user_id", "job_id"}), indexes = @Index(name = "ix_application_user_status", columnList = "user_id,status"))
@Getter @Setter @NoArgsConstructor
public class JobApplication {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "job_id", nullable = false)
    private Job job;
    @Column(nullable = false, length = 32)
    private String status = "INTERESTED";
    @Column(name = "interview_at")
    private Instant interviewAt;
    @Column(columnDefinition = "TEXT")
    private String notes;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}

