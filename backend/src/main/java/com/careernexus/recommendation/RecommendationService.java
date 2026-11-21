package com.careernexus.recommendation;

import com.careernexus.job.Job;
import com.careernexus.job.JobViewMapper;
import com.careernexus.job.RecommendationMatch;
import com.careernexus.profile.UserProfile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class RecommendationService {
    private final double requiredSkillsWeight, experienceWeight, locationWeight, titleWeight, additionalSkillsWeight;
    public RecommendationService(@Value("${recommendation.weights.required-skills:40}") double requiredSkillsWeight,
                                 @Value("${recommendation.weights.experience:25}") double experienceWeight,
                                 @Value("${recommendation.weights.location:15}") double locationWeight,
                                 @Value("${recommendation.weights.title:10}") double titleWeight,
                                 @Value("${recommendation.weights.additional-skills:10}") double additionalSkillsWeight) {
        this.requiredSkillsWeight = requiredSkillsWeight; this.experienceWeight = experienceWeight;
        this.locationWeight = locationWeight; this.titleWeight = titleWeight; this.additionalSkillsWeight = additionalSkillsWeight;
        if (requiredSkillsWeight < 0 || experienceWeight < 0 || locationWeight < 0 || titleWeight < 0 || additionalSkillsWeight < 0
                || requiredSkillsWeight + experienceWeight + locationWeight + titleWeight + additionalSkillsWeight <= 0)
            throw new IllegalArgumentException("Recommendation weights must be non-negative and have a positive total");
    }

    public RecommendationMatch evaluate(Job job, UserProfile profile) {
        return evaluate(job, profile, null);
    }

    public RecommendationMatch evaluate(Job job, UserProfile profile, Integer experienceOverride) {
        Set<String> profileSkills = profile.getSkills().stream().map(s -> norm(s.getName())).collect(java.util.stream.Collectors.toSet());
        java.util.Map<String, String> requiredNames = job.getRequiredSkills().stream()
                .collect(java.util.stream.Collectors.toMap(s -> norm(s.getName()), s -> s.getName(), (first, ignored) -> first));
        Set<String> required = requiredNames.keySet();
        Set<String> additional = job.getAdditionalSkills().stream().map(s -> norm(s.getName())).collect(java.util.stream.Collectors.toSet());
        List<String> matched = required.stream().filter(profileSkills::contains).map(requiredNames::get).sorted(String.CASE_INSENSITIVE_ORDER).toList();
        List<String> missing = required.stream().filter(skill -> !profileSkills.contains(skill)).map(requiredNames::get).sorted(String.CASE_INSENSITIVE_ORDER).toList();
        double skillFit = required.isEmpty() ? 0.55 : (double) matched.size() / required.size();
        double extraFit = additional.isEmpty() ? 0.55 : (double) additional.stream().filter(profileSkills::contains).count() / additional.size();

        int years = experienceOverride != null ? experienceOverride : profile.getYearsExperience() == null ? 0 : profile.getYearsExperience();
        Integer min = job.getRequiredExperienceMin(), max = job.getRequiredExperienceMax();
        boolean meetsRequired = min == null || years >= min && (max == null || years <= max);
        double experienceFit = min == null ? (job.getPreferredExperienceMin() == null ? 0.7 : years >= job.getPreferredExperienceMin() ? 0.9 : 0.6)
                : meetsRequired ? 1.0 : years < min ? Math.max(0.1, 1.0 - ((double) (min - years) / Math.max(min, 1))) : 0.6;
        String experienceText = min == null ? (job.getPreferredExperienceMin() == null ? "No explicit requirement found" : "Preferred: " + range(job.getPreferredExperienceMin(), job.getPreferredExperienceMax()))
                : meetsRequired ? "Within required range" : "Outside listed range; review the full posting";

        String profileCountry = norm(profile.getCountryCode()), jobCountry = norm(job.getCountryCode());
        String profileCity = norm(profile.getCity()), jobLocation = norm(job.getLocation());
        boolean locationCompatible = profileCountry.isBlank() || jobCountry.isBlank() || profileCountry.equals(jobCountry);
        double locationFit = profileCountry.isBlank() ? 0.65 : profileCountry.equals(jobCountry) ? 1.0 : jobCountry.isBlank() ? 0.55 : 0.0;
        if (!profileCity.isBlank()) {
            boolean cityMatch = jobLocation.contains(profileCity);
            locationCompatible = locationCompatible && (jobLocation.isBlank() || cityMatch);
            if (cityMatch) locationFit = Math.min(1.0, locationFit + 0.1);
            else if (!jobLocation.isBlank()) locationFit *= 0.55;
        }
        double titleFit = titleSimilarity(profile.getTargetTitle(), job.getTitle());
        double totalWeight = requiredSkillsWeight + experienceWeight + locationWeight + titleWeight + additionalSkillsWeight;
        double weighted = (skillFit * requiredSkillsWeight + experienceFit * experienceWeight + locationFit * locationWeight + titleFit * titleWeight + extraFit * additionalSkillsWeight) / totalWeight;
        double score = Math.round(weighted * 1000.0) / 10.0;
        String explanation = !missing.isEmpty() ? "Relevant skills found; build or demonstrate " + String.join(", ", missing) + "."
                : !required.isEmpty() ? "Your profile includes the skills detected in this posting."
                : "This posting does not list detectable skills; score relies more on experience, title, and location.";
        return new RecommendationMatch(score, matched, missing, experienceText, locationCompatible, explanation);
    }

    private static double titleSimilarity(String target, String job) {
        if (target == null || target.isBlank()) return 0.55;
        String left = norm(target), right = norm(job);
        if (right.contains(left) || left.contains(right)) return 1.0;
        Set<String> tokens = new HashSet<>(List.of(left.split("\\s+")));
        tokens.removeIf(t -> t.length() < 3);
        if (tokens.isEmpty()) return 0.3;
        long hits = tokens.stream().filter(right::contains).count();
        return (double) hits / tokens.size();
    }
    private static String range(Integer min, Integer max) { return max == null ? min + "+ years" : min + "–" + max + " years"; }
    private static String norm(String value) { return value == null ? "" : value.trim().toLowerCase(Locale.ROOT); }
}

