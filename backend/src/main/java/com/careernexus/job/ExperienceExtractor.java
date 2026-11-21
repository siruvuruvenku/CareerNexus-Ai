package com.careernexus.job;

import org.springframework.stereotype.Component;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ExperienceExtractor {
    private static final Pattern RANGE = Pattern.compile("(?i)(\\d{1,2})\\s*(?:-|–|to)\\s*(\\d{1,2})\\s*\\+?\\s*years?");
    private static final Pattern MINIMUM = Pattern.compile("(?i)(?:at least|minimum of|min\\.)\\s*(\\d{1,2})\\s*\\+?\\s*years?|\\b(\\d{1,2})\\s*\\+\\s*years?");

    public ExperienceRequirements parse(String description) {
        if (description == null) return new ExperienceRequirements(null, null, null, null);
        String preferredSection = preferredSection(description);
        Matcher range = RANGE.matcher(description);
        Integer min = null, max = null;
        if (range.find()) { min = Integer.valueOf(range.group(1)); max = Integer.valueOf(range.group(2)); }
        else {
            Matcher minimum = MINIMUM.matcher(description);
            if (minimum.find()) min = Integer.valueOf(minimum.group(1) != null ? minimum.group(1) : minimum.group(2));
        }
        Integer preferredMin = null, preferredMax = null;
        if (!preferredSection.isBlank()) {
            Matcher preferredRange = RANGE.matcher(preferredSection);
            if (preferredRange.find()) { preferredMin = Integer.valueOf(preferredRange.group(1)); preferredMax = Integer.valueOf(preferredRange.group(2)); }
            else {
                Matcher preferredMinimum = MINIMUM.matcher(preferredSection);
                if (preferredMinimum.find()) preferredMin = Integer.valueOf(preferredMinimum.group(1) != null ? preferredMinimum.group(1) : preferredMinimum.group(2));
            }
            if (preferredMin != null && min != null && preferredMin.equals(min)) { min = null; max = null; }
        }
        return new ExperienceRequirements(min, max, preferredMin, preferredMax);
    }

    private static String preferredSection(String description) {
        Matcher matcher = Pattern.compile("(?is)(?:preferred|nice to have|nice-to-have|bonus|desired)(.{0,1000})").matcher(description);
        return matcher.find() ? matcher.group(1) : "";
    }
    public record ExperienceRequirements(Integer requiredMin, Integer requiredMax, Integer preferredMin, Integer preferredMax) {}
}

