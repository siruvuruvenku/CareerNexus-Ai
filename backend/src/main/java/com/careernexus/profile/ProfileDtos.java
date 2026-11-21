package com.careernexus.profile;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.util.List;

public final class ProfileDtos {
    private ProfileDtos() {}
    public record ProfileView(Long id, String email, String fullName, String targetTitle, String countryCode, String city,
                              Integer yearsExperience, String summary, List<String> skills, boolean resumeUploaded) {}
    public record ProfileUpdate(@Size(max = 160) String fullName, @Size(max = 160) String targetTitle,
                                @Size(max = 2) String countryCode, @Size(max = 160) String city,
                                @Min(0) @Max(60) Integer yearsExperience, @Size(max = 5000) String summary,
                                @Size(max = 80) List<@Size(max = 100) String> skills) {}
    public record ResumeView(String fileName, String contentType) {}
}

