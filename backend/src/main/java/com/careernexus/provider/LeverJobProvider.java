package com.careernexus.provider;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
public class LeverJobProvider implements JobProvider {
    private final RestClient http;
    private final List<Board> boards;
    public LeverJobProvider(RestClient jobProviderRestClient, @Value("${jobs.lever.boards:}") String configuredBoards) {
        this.http = jobProviderRestClient; this.boards = parseBoards(configuredBoards);
    }
    @Override public String name() { return "Lever"; }
    @Override public boolean enabled() { return !boards.isEmpty(); }

    @Override @Cacheable(cacheNames = "providerJobs", key = "'lever:' + #criteria.toString()")
    public List<ProviderJob> search(SearchCriteria criteria) {
        List<ProviderJob> result = new ArrayList<>();
        for (Board board : boards) {
            try {
                String host = board.region().equals("eu") ? "api.eu.lever.co" : "api.lever.co";
                for (int skip = 0; skip < 10_000; skip += 100) {
                    java.net.URI uri = org.springframework.web.util.UriComponentsBuilder.fromUriString("https://" + host + "/v0/postings/" + board.site())
                            .queryParam("mode", "json").queryParam("skip", skip).queryParam("limit", 100).build().encode().toUri();
                    JsonNode root = http.get().uri(uri).retrieve().body(JsonNode.class);
                    if (root == null || !root.isArray()) break;
                    int pageSize = 0;
                    for (JsonNode row : root) {
                        pageSize++;
                        String id = text(row, "id"), title = text(row, "text"), url = text(row, "hostedUrl");
                        if (url == null) url = text(row, "applyUrl");
                        if (id == null || title == null || url == null) continue;
                        JsonNode categories = row.path("categories");
                        JsonNode salary = row.path("salaryRange");
                        String location = text(categories, "location");
                        result.add(new ProviderJob(name(), board.site() + ":" + id, title, board.site(), location,
                                countryCode(text(row, "country"), location), text(row, "descriptionPlain"), url, "https://www.lever.co",
                                text(categories, "commitment"), text(row, "workplaceType"), decimal(salary, "min"), decimal(salary, "max"),
                                text(salary, "currency"), text(salary, "interval"), epoch(row.path("createdAt"))));
                    }
                    if (pageSize < 100) break;
                }
            } catch (Exception e) {
                throw new ProviderCallException(name() + " board " + board.site() + " could not be fetched", e);
            }
        }
        return result;
    }

    private static List<Board> parseBoards(String configured) {
        if (configured == null || configured.isBlank()) return List.of();
        return java.util.Arrays.stream(configured.split(",")).map(String::trim).map(entry -> {
            String[] parts = entry.split(":", 2);
            String region = parts.length == 2 ? parts[0].toLowerCase() : "global";
            String site = parts.length == 2 ? parts[1] : parts[0];
            return new Board(region, site);
        }).filter(b -> (b.region().equals("global") || b.region().equals("eu")) && b.site().matches("[A-Za-z0-9_-]{2,100}")).distinct().toList();
    }
    private static String text(JsonNode node, String key) {
        JsonNode value = node.path(key); return value.isMissingNode() || value.isNull() || value.asText().isBlank() ? null : value.asText();
    }
    private static java.math.BigDecimal decimal(JsonNode node, String key) { return node.path(key).isNumber() ? node.path(key).decimalValue() : null; }
    private static String countryCode(String country, String location) {
        String value = country == null || country.isBlank() ? location : country;
        if (value == null) return null;
        String normalized = value.trim();
        if (normalized.matches("(?i)[a-z]{2}") && java.util.Arrays.asList(java.util.Locale.getISOCountries()).contains(normalized.toUpperCase(java.util.Locale.ROOT)))
            return normalized.toUpperCase(java.util.Locale.ROOT);
        for (String code : java.util.Locale.getISOCountries()) {
            String name = new java.util.Locale("", code).getDisplayCountry(java.util.Locale.ENGLISH);
            if (normalized.equalsIgnoreCase(name) || normalized.toLowerCase(java.util.Locale.ROOT).endsWith(", " + name.toLowerCase(java.util.Locale.ROOT))) return code;
        }
        return null;
    }
    private static Instant epoch(JsonNode value) {
        if (!value.isNumber()) return null;
        long raw = value.asLong(); return Instant.ofEpochMilli(raw < 100000000000L ? raw * 1000 : raw);
    }
    private record Board(String region, String site) {}
}

