package com.careernexus.application;

import com.careernexus.user.AppUser;
import com.careernexus.job.Job;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "search_history", indexes = @Index(name = "ix_search_user_date", columnList = "user_id,searched_at"))
@Getter @Setter @NoArgsConstructor
public class SearchHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;
    @Column(name = "query_text", length = 500)
    private String queryText;
    @Column(name = "searched_at", nullable = false)
    private Instant searchedAt = Instant.now();
    @ManyToMany
    @JoinTable(name = "search_history_jobs", joinColumns = @JoinColumn(name = "search_id"), inverseJoinColumns = @JoinColumn(name = "job_id"))
    private Set<Job> jobs = new HashSet<>();
}

