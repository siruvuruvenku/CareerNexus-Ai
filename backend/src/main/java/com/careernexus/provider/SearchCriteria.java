package com.careernexus.provider;

import java.util.List;

public record SearchCriteria(String countryCode, String state, String city, String title, List<String> skills) implements java.io.Serializable {
    public String queryText() {
        String skillText = skills == null ? "" : String.join(" ", skills);
        return String.join(" ", List.of(title == null ? "" : title, skillText)).trim();
    }
}

