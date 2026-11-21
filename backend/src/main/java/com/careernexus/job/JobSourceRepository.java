package com.careernexus.job;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface JobSourceRepository extends JpaRepository<JobSource, Long> {
    Optional<JobSource> findByProviderAndProviderJobId(String provider, String providerJobId);
}

