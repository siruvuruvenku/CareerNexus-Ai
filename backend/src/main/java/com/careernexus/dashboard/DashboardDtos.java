package com.careernexus.dashboard;

import java.util.List;
import java.util.Map;

public final class DashboardDtos {
    private DashboardDtos() {}
    public record Metric(String label, long value, String hint) {}
    public record Breakdown(String label, long count) {}
    public record Summary(List<Metric> metrics, Map<String, Long> applicationStatuses, List<Breakdown> jobsByCity,
                          List<Breakdown> jobsByTechnology, List<Breakdown> jobsByCompany,
                          boolean expiryDataAvailable, String availabilityNote) {}
}

