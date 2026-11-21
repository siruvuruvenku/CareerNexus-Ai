package com.careernexus.application;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    List<JobApplication> findAllByUser_IdOrderByUpdatedAtDesc(Long userId);
    Optional<JobApplication> findByUser_IdAndJob_Id(Long userId, Long jobId);
    long countByUser_Id(Long userId);
    long countByUser_IdAndStatusIgnoreCase(Long userId, String status);
}

