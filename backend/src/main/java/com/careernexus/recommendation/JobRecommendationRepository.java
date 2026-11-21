package com.careernexus.recommendation;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface JobRecommendationRepository extends JpaRepository<JobRecommendation, Long> {
    List<JobRecommendation> findAllByUser_IdOrderByMatchScoreDesc(Long userId);
    java.util.Optional<JobRecommendation> findByUser_IdAndJob_Id(Long userId, Long jobId);
}

