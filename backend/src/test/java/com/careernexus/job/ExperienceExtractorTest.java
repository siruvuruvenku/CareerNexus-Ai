package com.careernexus.job;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ExperienceExtractorTest {
    private final ExperienceExtractor extractor = new ExperienceExtractor();

    @Test
    void separatesRequiredAndPreferredRanges() {
        var result = extractor.parse("Requires 1-2 years of experience. Preferred: 3-5 years in cloud.");

        assertEquals(1, result.requiredMin());
        assertEquals(2, result.requiredMax());
        assertEquals(3, result.preferredMin());
        assertEquals(5, result.preferredMax());
    }

    @Test
    void doesNotTreatOnlyPreferredExperienceAsRequired() {
        var result = extractor.parse("Preferred: 3+ years of experience.");

        assertNull(result.requiredMin());
        assertNull(result.requiredMax());
        assertEquals(3, result.preferredMin());
    }

    @Test
    void supportsEnDashRanges() {
        var result = extractor.parse("At least 0–1 years of experience.");

        assertEquals(0, result.requiredMin());
        assertEquals(1, result.requiredMax());
    }
}
