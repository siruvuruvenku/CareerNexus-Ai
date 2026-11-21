package com.careernexus.recommendation;

import com.careernexus.job.Job;
import com.careernexus.profile.UserProfile;
import com.careernexus.skill.Skill;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecommendationServiceTest {
    private final RecommendationService recommendations = new RecommendationService(40, 25, 15, 10, 10);

    @Test
    void reportsSkillGapsAndCompatibleExperience() {
        UserProfile profile = new UserProfile();
        profile.setSkills(Set.of(skill("Java"), skill("Spring Boot"), skill("React"), skill("MySQL")));
        profile.setYearsExperience(1);
        profile.setCountryCode("IN");
        profile.setCity("Bengaluru");
        profile.setTargetTitle("Java Full Stack Developer");

        Job job = new Job();
        job.setTitle("Java Full Stack Developer");
        job.setCountryCode("IN");
        job.setLocation("Bengaluru, India");
        job.setRequiredExperienceMin(0);
        job.setRequiredExperienceMax(2);
        job.setRequiredSkills(Set.of(skill("Java"), skill("Spring Boot"), skill("React"), skill("MySQL"), skill("AWS")));

        var match = recommendations.evaluate(job, profile);

        assertEquals(4, match.matchingSkills().size());
        assertEquals(java.util.List.of("AWS"), match.missingSkills());
        assertEquals("Within required range", match.experienceCompatibility());
        assertTrue(match.locationCompatible());
        assertTrue(match.scorePercent() > 70);
        assertFalse(match.explanation().contains("selection probability"));
    }

    private static Skill skill(String name) {
        Skill skill = new Skill();
        skill.setName(name);
        return skill;
    }
}
