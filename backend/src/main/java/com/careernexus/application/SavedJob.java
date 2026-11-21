package com.careernexus.application;

import com.careernexus.job.Job;
import com.careernexus.user.AppUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;

@Entity
@Table(name = "saved_jobs", uniqueConstraints = @UniqueConstraint(name = "uq_saved_user_job", columnNames = {"user_id", "job_id"}), indexes = @Index(name = "ix_saved_user_created", columnList = "user_id,created_at"))
@Getter @Setter @NoArgsConstructor
public class SavedJob {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "job_id", nullable = false)
    private Job job;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}

