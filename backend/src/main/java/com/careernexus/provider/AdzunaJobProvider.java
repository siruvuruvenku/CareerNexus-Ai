package com.careernexus.provider;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class AdzunaJobProvider implements JobProvider {
    private final RestClient http;
    private final String appId;
    private final String appKey;
    private final int maxPages;
    public AdzunaJobProvider(RestClient jobProviderRestClient,
                             @Value("${jobs.adzuna.app-id:}") String appId,
                             @Value("${jobs.adzuna.app-key:}") String appKey,
                             @Value("${jobs.adzuna.max-pages:5}") int maxPages) {
        this.http = jobProviderRestClient; this.appId = appId; this.appKey = appKey; this.maxPages = Math.max(1, Math.min(20, maxPages));
    }
    @Override public String name() { return "Adzuna"; }
    @Override public boolean enabled() { return !appId.isBlank() && !appKey.isBlank(); }

    @Override @Cacheable(cacheNames = "providerJobs", key = "'adzuna:' + #criteria.toString()")
    public List<ProviderJob> search(SearchCriteria criteria) {
        if (!enabled() || criteria.countryCode() == null || criteria.countryCode().isBlank()) return List.of();
        String country = criteria.countryCode().toLowerCase();
        List<ProviderJob> jobs = new ArrayList<>();
        for (int page = 1; page <= maxPages; page++) {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString("https://api.adzuna.com/v1/api/jobs/" + country + "/search/" + page)
                    .queryParam("app_id", appId).queryParam("app_key", appKey).queryParam("results_per_page", 50)
                    .queryParam("content-type", "application/json");
            if (!criteria.queryText().isBlank()) builder.queryParam("what", criteria.queryText());
            String where = java.util.stream.Stream.of(criteria.city(), criteria.state()).filter(value -> value != null && !value.isBlank())
                    .map(String::trim).distinct().collect(java.util.stream.Collectors.joining(", "));
            if (!where.isBlank()) builder.queryParam("where", where);
            JsonNode root = http.get().uri(builder.build().encode().toUri()).retrieve().body(JsonNode.class);
            if (root == null || !root.path("results").isArray()) break;
            int pageSize = 0;
            for (JsonNode row : root.path("results")) {
                pageSize++;
                String url = text(row, "redirect_url");
                if (text(row, "id") == null || text(row, "title") == null || url == null) continue;
                String location = text(row.path("location"), "display_name");
                String company = text(row.path("company"), "display_name");
                jobs.add(new ProviderJob(name(), text(row, "id"), text(row, "title"), company, location,
                        criteria.countryCode().toUpperCase(), text(row, "description"), url,
                        attributionDomain(country), text(row, "contract_time"), null,
                        decimal(row, "salary_min"), decimal(row, "salary_max"), currency(country), null, instant(text(row, "created"))));
            }
            if (pageSize < 50) break;
        }
        return jobs;
    }

    private static String text(JsonNode node, String key) {
        JsonNode value = node.path(key);
        return value.isMissingNode() || value.isNull() || value.asText().isBlank() ? null : value.asText();
    }
    private static BigDecimal decimal(JsonNode node, String key) {
        return node.path(key).isNumber() ? node.path(key).decimalValue() : null;
    }
    private static Instant instant(String value) {
        try { return value == null ? null : OffsetDateTime.parse(value).toInstant(); } catch (Exception ignored) { return null; }
    }
    private static String currency(String country) {
        try { return java.util.Currency.getInstance(new java.util.Locale("", country.toUpperCase())).getCurrencyCode(); }
        catch (Exception ignored) { return null; }
    }
    private static String attributionDomain(String country) {
        return switch (country) {
            case "gb" -> "https://www.adzuna.co.uk";
            case "in" -> "https://www.adzuna.in";
            case "au" -> "https://www.adzuna.com.au";
            case "ca" -> "https://www.adzuna.ca";
            case "de" -> "https://www.adzuna.de";
            case "fr" -> "https://www.adzuna.fr";
            default -> "https://www.adzuna.com";
        };
    }
}

