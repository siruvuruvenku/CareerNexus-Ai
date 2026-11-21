package com.careernexus.config;

import com.careernexus.location.Country;
import com.careernexus.location.CountryRepository;
import com.careernexus.skill.Skill;
import com.careernexus.skill.SkillRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;
import java.util.Locale;
import java.util.Map;
import java.util.List;
import com.careernexus.location.Location;
import com.careernexus.location.LocationRepository;

@Configuration
public class ReferenceDataSeeder {
    @Bean ApplicationRunner seedReferenceData(CountryRepository countries, LocationRepository locations, SkillRepository skills) {
        return args -> seed(countries, locations, skills);
    }

    @Transactional
    void seed(CountryRepository countries, LocationRepository locations, SkillRepository skills) {
        if (countries.count() == 0) {
            for (String code : Locale.getISOCountries()) {
                Country country = new Country();
                country.setCode(code);
                country.setName(new Locale("", code).getDisplayCountry(Locale.ENGLISH));
                countries.save(country);
            }
        }
        Country india = countries.findByCodeIgnoreCase("IN").orElse(null);
        if (india != null && locations.findDistinctByCountry_CodeIgnoreCaseOrderByCityAsc("IN").isEmpty()) {
            for (String city : List.of("Bengaluru", "Chennai", "Delhi", "Gurgaon", "Hyderabad", "Mumbai", "Noida", "Pune", "Visakhapatnam")) {
                Location location = new Location(); location.setCountry(india); location.setCity(city); locations.save(location);
            }
        }
        Map<String, String> catalog = Map.ofEntries(
                Map.entry("Java", "Programming"), Map.entry("Python", "Programming"), Map.entry("JavaScript", "Programming"),
                Map.entry("TypeScript", "Programming"), Map.entry("C++", "Programming"), Map.entry("C#", "Programming"),
                Map.entry("Go", "Programming"), Map.entry("PHP", "Programming"), Map.entry("Spring Boot", "Backend"),
                Map.entry("Spring MVC", "Backend"), Map.entry("Hibernate", "Backend"), Map.entry("Node.js", "Backend"),
                Map.entry("Express.js", "Backend"), Map.entry("Django", "Backend"), Map.entry("FastAPI", "Backend"),
                Map.entry("React", "Frontend"), Map.entry("Angular", "Frontend"), Map.entry("Vue.js", "Frontend"),
                Map.entry("Next.js", "Frontend"), Map.entry("MySQL", "Database"), Map.entry("PostgreSQL", "Database"),
                Map.entry("MongoDB", "Database"), Map.entry("Redis", "Database"), Map.entry("Oracle", "Database"),
                Map.entry("AWS", "Cloud & DevOps"), Map.entry("Azure", "Cloud & DevOps"), Map.entry("Docker", "Cloud & DevOps"),
                Map.entry("Kubernetes", "Cloud & DevOps"), Map.entry("Jenkins", "Cloud & DevOps"), Map.entry("GitHub Actions", "Cloud & DevOps"));
        catalog.forEach((name, category) -> skills.findByNameIgnoreCase(name).orElseGet(() -> {
            Skill skill = new Skill(); skill.setName(name); skill.setCategory(category); skill.setCustom(false); return skills.save(skill);
        }));
    }
}

