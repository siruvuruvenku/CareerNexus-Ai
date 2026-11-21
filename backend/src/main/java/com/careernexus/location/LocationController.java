package com.careernexus.location;

import com.careernexus.common.ApiResponse;
import com.careernexus.job.JobRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/countries")
public class LocationController {
    private final CountryRepository countries;
    private final JobRepository jobs;
    private final LocationRepository locations;
    public LocationController(CountryRepository countries, JobRepository jobs, LocationRepository locations) { this.countries = countries; this.jobs = jobs; this.locations = locations; }
    @GetMapping @Cacheable("countryList")
    public ApiResponse<List<CountryView>> countries() { return ApiResponse.ok(countries.findAllByOrderByNameAsc().stream().map(c -> new CountryView(c.getCode(), c.getName())).toList()); }
    @GetMapping("/{code}/locations") @Cacheable(value = "locationsByCountry", key = "#code.toUpperCase()")
    public ApiResponse<List<String>> locations(@PathVariable String code) {
        String normalized = code.toUpperCase();
        if (countries.findByCodeIgnoreCase(normalized).isEmpty()) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Country not found");
        java.util.Set<String> result = new java.util.TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        locations.findDistinctByCountry_CodeIgnoreCaseOrderByCityAsc(normalized).forEach(location -> result.add(location.getCity()));
        result.addAll(jobs.findLocationsByCountryCode(normalized));
        return ApiResponse.ok(result.stream().toList());
    }
    public record CountryView(String code, String name) {}
}

