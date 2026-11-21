package com.careernexus.application;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SavedJobRepository extends JpaRepository<SavedJob, Long> {
    List<SavedJob> findAllByUser_IdOrderByCreatedAtDesc(Long userId);
    Optional<SavedJob> findByUser_IdAndJob_Id(Long userId, Long jobId);
    long countByUser_Id(Long userId);
    void deleteByUser_IdAndJob_Id(Long userId, Long jobId);
}

