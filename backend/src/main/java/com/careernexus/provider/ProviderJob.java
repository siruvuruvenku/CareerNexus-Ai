package com.careernexus.provider;

import java.math.BigDecimal;
import java.time.Instant;

public record ProviderJob(
        String provider, String providerJobId, String title, String companyName,
        String location, String countryCode, String description, String applicationUrl,
        String attributionUrl, String employmentType, String workplaceType,
        BigDecimal salaryMin, BigDecimal salaryMax, String salaryCurrency, String salaryInterval, Instant postedAt
) implements java.io.Serializable {}

