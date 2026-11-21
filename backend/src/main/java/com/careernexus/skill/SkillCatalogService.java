package com.careernexus.skill;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class SkillCatalogService {
    private final SkillRepository skills;
    public SkillCatalogService(SkillRepository skills) { this.skills = skills; }

    public Skill findOrCreate(String rawName, boolean custom) {
        String name = rawName.trim();
        return skills.findByNameIgnoreCase(name).orElseGet(() -> {
            Skill skill = new Skill(); skill.setName(name); skill.setCategory("Custom"); skill.setCustom(custom); return skills.save(skill);
        });
    }

    public Set<Skill> extract(String description) {
        if (description == null || description.isBlank()) return Set.of();
        String text = description.toLowerCase(Locale.ROOT);
        Set<Skill> found = new LinkedHashSet<>();
        for (Skill skill : skills.findAll()) {
            if (containsSkill(text, skill.getName())) found.add(skill);
        }
        return found;
    }

    public Set<Skill> extractPreferred(String description, Set<Skill> allSkills) {
        if (description == null || description.isBlank()) return Set.of();
        var matcher = Pattern.compile("(?is)(?:preferred|nice to have|nice-to-have|bonus|desired skills|would be a plus)(.{0,900})").matcher(description);
        List<String> sections = new ArrayList<>();
        while (matcher.find()) sections.add(matcher.group(1));
        if (sections.isEmpty()) return Set.of();
        String preferredText = String.join(" ", sections).toLowerCase(Locale.ROOT);
        return allSkills.stream().filter(skill -> containsSkill(preferredText, skill.getName())).collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    private static boolean containsSkill(String text, String name) {
        String normalized = switch (name.toLowerCase(Locale.ROOT)) {
            case "node.js" -> "node(?:\\.js)?";
            case "express.js" -> "express(?:\\.js)?";
            case "vue.js" -> "vue(?:\\.js)?";
            case "c++" -> "c\\+\\+";
            case "c#" -> "c#";
            case "github actions" -> "github\\s+actions";
            default -> Pattern.quote(name.toLowerCase(Locale.ROOT));
        };
        return Pattern.compile("(?<![a-z0-9])" + normalized + "(?![a-z0-9])", Pattern.CASE_INSENSITIVE).matcher(text).find();
    }
}

