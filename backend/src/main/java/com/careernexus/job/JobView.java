package com.careernexus.job;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record JobView(Long id, String title, String companyName, String location, String countryCode,
                      String description, List<String> requiredSkills, List<String> additionalSkills,
                      Integer requiredExperienceMin, Integer requiredExperienceMax,
                      Integer preferredExperienceMin, Integer preferredExperienceMax,
                      String employmentType, String workplaceType, BigDecimal salaryMin, BigDecimal salaryMax,
                      String salaryCurrency, String salaryInterval, Instant postedAt, String source, String sourceUrl,
                      String sourceAttributionUrl, Instant lastVerifiedAt, RecommendationMatch match) {}

