package com.careernexus.job;

import com.careernexus.company.Company;
import com.careernexus.skill.Skill;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "jobs", indexes = {
        @Index(name = "ix_job_location", columnList = "country_code,location"),
        @Index(name = "ix_job_posted", columnList = "posted_at"),
        @Index(name = "ix_job_company", columnList = "company_id"),
        @Index(name = "ix_job_active_verified", columnList = "active,last_verified_at")
})
@Getter @Setter @NoArgsConstructor
public class Job {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;
    @Column(nullable = false, length = 240)
    private String title;
    @Column(name = "country_code", length = 2)
    private String countryCode;
    @Column(length = 240)
    private String location;
    @Column(columnDefinition = "MEDIUMTEXT")
    private String description;
    @Column(name = "canonical_url", nullable = false, unique = true, length = 700)
    private String canonicalUrl;
    @Column(name = "employment_type", length = 40)
    private String employmentType;
    @Column(name = "workplace_type", length = 40)
    private String workplaceType;
    @Column(name = "salary_min", precision = 14, scale = 2)
    private BigDecimal salaryMin;
    @Column(name = "salary_max", precision = 14, scale = 2)
    private BigDecimal salaryMax;
    @Column(name = "salary_currency", length = 3)
    private String salaryCurrency;
    @Column(name = "salary_interval", length = 24)
    private String salaryInterval;
    @Column(name = "required_experience_min")
    private Integer requiredExperienceMin;
    @Column(name = "required_experience_max")
    private Integer requiredExperienceMax;
    @Column(name = "preferred_experience_min")
    private Integer preferredExperienceMin;
    @Column(name = "preferred_experience_max")
    private Integer preferredExperienceMax;
    @ManyToMany
    @JoinTable(name = "job_required_skills", joinColumns = @JoinColumn(name = "job_id"), inverseJoinColumns = @JoinColumn(name = "skill_id"))
    private Set<Skill> requiredSkills = new HashSet<>();
    @ManyToMany
    @JoinTable(name = "job_additional_skills", joinColumns = @JoinColumn(name = "job_id"), inverseJoinColumns = @JoinColumn(name = "skill_id"))
    private Set<Skill> additionalSkills = new HashSet<>();
    @Column(name = "posted_at")
    private Instant postedAt;
    @Column(name = "last_verified_at", nullable = false)
    private Instant lastVerifiedAt = Instant.now();
    @Column(nullable = false)
    private boolean active = true;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<JobSource> sources = new HashSet<>();
}

