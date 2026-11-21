package com.careernexus.recommendation;

import com.careernexus.job.Job;
import com.careernexus.user.AppUser;
import jakarta.persistence.*;
import com.fasterxml.jackson.databind.JsonNode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "job_recommendations", uniqueConstraints = @UniqueConstraint(name = "uq_recommendation_user_job", columnNames = {"user_id", "job_id"}), indexes = @Index(name = "ix_recommendation_user_score", columnList = "user_id,match_score"))
@Getter @Setter @NoArgsConstructor
public class JobRecommendation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "job_id", nullable = false)
    private Job job;
    @Column(name = "match_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal matchScore;
    @Column(name = "explanation_json", nullable = false, columnDefinition = "json")
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode explanationJson;
    @Column(name = "generated_at", nullable = false)
    private Instant generatedAt = Instant.now();
}

