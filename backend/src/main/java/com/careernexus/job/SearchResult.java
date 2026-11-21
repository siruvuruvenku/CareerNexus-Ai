package com.careernexus.job;

import java.util.List;

public record SearchResult(List<JobView> items, long total, int page, int size, int totalPages,
                           List<String> sources, List<String> sourceWarnings) {}

