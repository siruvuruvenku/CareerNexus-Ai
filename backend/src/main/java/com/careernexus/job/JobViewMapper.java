package com.careernexus.job;

import org.springframework.stereotype.Component;
import java.util.Comparator;

@Component
public class JobViewMapper {
    public JobView map(Job job, RecommendationMatch match) {
        JobSource source = job.getSources().stream().min(Comparator.comparing(JobSource::getProvider)).orElse(null);
        String provider = source == null ? "Unknown" : source.getProvider();
        String attribution = switch (provider) {
            case "Adzuna" -> adzuna(job.getCountryCode());
            case "Greenhouse" -> "https://www.greenhouse.io";
            case "Lever" -> "https://www.lever.co";
            default -> null;
        };
        return new JobView(job.getId(), job.getTitle(), job.getCompany() == null ? "Company not listed" : job.getCompany().getName(),
                job.getLocation(), job.getCountryCode(), job.getDescription(), job.getRequiredSkills().stream().map(s -> s.getName()).sorted().toList(),
                job.getAdditionalSkills().stream().map(s -> s.getName()).sorted().toList(), job.getRequiredExperienceMin(),
                job.getRequiredExperienceMax(), job.getPreferredExperienceMin(), job.getPreferredExperienceMax(), job.getEmploymentType(),
                job.getWorkplaceType(), job.getSalaryMin(), job.getSalaryMax(), job.getSalaryCurrency(), job.getSalaryInterval(), job.getPostedAt(), provider,
                source == null ? job.getCanonicalUrl() : source.getOriginalUrl(), attribution, job.getLastVerifiedAt(), match);
    }
    private static String adzuna(String code) {
        if (code == null) return "https://www.adzuna.com";
        return switch (code.toLowerCase()) {
            case "gb" -> "https://www.adzuna.co.uk"; case "in" -> "https://www.adzuna.in";
            case "au" -> "https://www.adzuna.com.au"; case "ca" -> "https://www.adzuna.ca";
            case "de" -> "https://www.adzuna.de"; case "fr" -> "https://www.adzuna.fr";
            default -> "https://www.adzuna.com";
        };
    }
}

