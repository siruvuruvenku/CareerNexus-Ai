package com.careernexus.job;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;

@Entity
@Table(name = "job_sources", uniqueConstraints = @UniqueConstraint(name = "uq_provider_job", columnNames = {"provider", "provider_job_id"}), indexes = @Index(name = "ix_job_source_job", columnList = "job_id"))
@Getter @Setter @NoArgsConstructor
public class JobSource {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;
    @Column(nullable = false, length = 40)
    private String provider;
    @Column(name = "provider_job_id", nullable = false, length = 240)
    private String providerJobId;
    @Column(name = "original_url", nullable = false, length = 1000)
    private String originalUrl;
    @Column(name = "last_verified_at", nullable = false)
    private Instant lastVerifiedAt = Instant.now();
}

