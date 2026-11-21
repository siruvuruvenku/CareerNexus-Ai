package com.careernexus.job;

import java.util.List;

public record RecommendationMatch(double scorePercent, List<String> matchingSkills, List<String> missingSkills,
                                  String experienceCompatibility, boolean locationCompatible, String explanation) {}

