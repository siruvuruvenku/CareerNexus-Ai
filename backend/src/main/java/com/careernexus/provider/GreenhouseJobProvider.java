package com.careernexus.provider;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class GreenhouseJobProvider implements JobProvider {
    private final RestClient http;
    private final List<String> boards;
    public GreenhouseJobProvider(RestClient jobProviderRestClient, @Value("${jobs.greenhouse.boards:}") String configuredBoards) {
        this.http = jobProviderRestClient; this.boards = split(configuredBoards);
    }
    @Override public String name() { return "Greenhouse"; }
    @Override public boolean enabled() { return !boards.isEmpty(); }

    @Override @Cacheable(cacheNames = "providerJobs", key = "'greenhouse:' + #criteria.toString()")
    public List<ProviderJob> search(SearchCriteria criteria) {
        List<ProviderJob> result = new ArrayList<>();
        for (String board : boards) {
            String company = board;
            try {
                JsonNode boardInfo = http.get().uri("https://boards-api.greenhouse.io/v1/boards/{board}", board).retrieve().body(JsonNode.class);
                if (boardInfo != null && boardInfo.hasNonNull("name")) company = boardInfo.path("name").asText();
                java.net.URI jobsUri = org.springframework.web.util.UriComponentsBuilder
                        .fromUriString("https://boards-api.greenhouse.io/v1/boards/" + board + "/jobs")
                        .queryParam("content", "true").build().encode().toUri();
                JsonNode root = http.get().uri(jobsUri).retrieve().body(JsonNode.class);
                if (root == null || !root.path("jobs").isArray()) continue;
                final String boardCompany = company;
                for (JsonNode row : root.path("jobs")) {
                    String url = text(row, "absolute_url");
                    String title = text(row, "title");
                    if (url == null || title == null) continue;
                    String location = text(row.path("location"), "name");
                    String description = text(row, "content");
                    result.add(new ProviderJob(name(), text(row, "id"), title, boardCompany, location,
                            countryFromLocation(location), stripHtml(description),
                            url, "https://www.greenhouse.io", null, null, null, null, null, null, instant(text(row, "updated_at"))));
                }
            } catch (Exception e) {
                throw new ProviderCallException(name() + " board " + board + " could not be fetched", e);
            }
        }
        return result;
    }

    private static List<String> split(String value) {
        if (value == null || value.isBlank()) return List.of();
        return java.util.Arrays.stream(value.split(",")).map(String::trim).filter(s -> s.matches("[A-Za-z0-9_-]{2,100}")).distinct().toList();
    }
    private static String text(JsonNode node, String key) {
        JsonNode value = node.path(key); return value.isMissingNode() || value.isNull() || value.asText().isBlank() ? null : value.asText();
    }
    private static Instant instant(String value) {
        try { return value == null ? null : OffsetDateTime.parse(value).toInstant(); } catch (Exception ignored) { return null; }
    }
    private static String countryFromLocation(String location) {
        if (location == null) return null;
        String upper = location.toUpperCase(java.util.Locale.ROOT);
        for (String code : java.util.Locale.getISOCountries()) {
            String name = new java.util.Locale("", code).getDisplayCountry(java.util.Locale.ENGLISH).toUpperCase(java.util.Locale.ROOT);
            if (upper.endsWith(", " + name) || upper.equals(name)) return code;
        }
        return null;
    }
    private static String stripHtml(String html) {
        if (html == null) return null;
        return html.replaceAll("(?is)<(script|style)[^>]*>.*?</\\1>", " ").replaceAll("(?s)<[^>]+>", " ")
                .replace("&nbsp;", " ").replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">")
                .replaceAll("\\s+", " ").trim();
    }

}

