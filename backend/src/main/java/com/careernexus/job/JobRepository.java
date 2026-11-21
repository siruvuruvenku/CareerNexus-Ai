package com.careernexus.job;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface JobRepository extends JpaRepository<Job, Long> {
    @EntityGraph(attributePaths = {"company", "requiredSkills", "additionalSkills", "sources"})
    List<Job> findAllByActiveTrueAndLastVerifiedAtAfterOrderByPostedAtDesc(Instant cutoff);
    @EntityGraph(attributePaths = {"company", "requiredSkills", "additionalSkills", "sources"})
    Optional<Job> findDetailedById(Long id);
    Optional<Job> findFirstByCanonicalUrlIgnoreCase(String canonicalUrl);
    @org.springframework.data.jpa.repository.Query("select distinct j.location from Job j where upper(j.countryCode) = upper(:countryCode) and j.active = true and j.location is not null order by j.location")
    List<String> findLocationsByCountryCode(@Param("countryCode") String countryCode);

    @org.springframework.data.jpa.repository.Query(value = """
            select distinct j from SearchHistory sh join sh.jobs j
            left join j.company c
            left join j.requiredSkills rs
            left join j.additionalSkills ads
            where sh.id = :searchHistoryId and j.active = true and j.lastVerifiedAt > :cutoff
              and (:countryCode is null or upper(j.countryCode) = upper(:countryCode)
                   or (j.countryCode is null and (:city is not null and lower(j.location) like lower(concat('%', :city, '%'))))
                   or (j.countryCode is null and :countryName is not null and lower(j.location) like lower(concat('%', :countryName, '%'))))
              and (:city is null or lower(j.location) like lower(concat('%', :city, '%')))
              and (:state is null or lower(j.location) like lower(concat('%', :state, '%')))
              and (:title is null or lower(j.title) like lower(concat('%', :title, '%')))
              and (:company is null or lower(c.name) like lower(concat('%', :company, '%')))
              and (:hasSkills = false or lower(rs.name) in :skills or lower(ads.name) in :skills)
              and (:employmentType is null or lower(j.employmentType) like lower(concat('%', :employmentType, '%')))
              and (:workplaceType is null or upper(j.workplaceType) = upper(:workplaceType))
              and (:minSalary is null or coalesce(j.salaryMax, j.salaryMin) >= :minSalary)
              and (:maxSalary is null or coalesce(j.salaryMin, j.salaryMax) <= :maxSalary)
              and (:salaryCurrency is null or upper(j.salaryCurrency) = upper(:salaryCurrency))
              and (:salaryInterval is null or upper(j.salaryInterval) = upper(:salaryInterval))
              and (:postedAfter is null or j.postedAt >= :postedAfter)
            order by
              case when :sortBy = 'salary' then j.salaryMax end desc,
              case when :sortBy = 'match' and :experienceYears is not null and j.requiredExperienceMin is not null and :experienceYears >= j.requiredExperienceMin and (j.requiredExperienceMax is null or :experienceYears <= j.requiredExperienceMax) then 0
                   when :sortBy = 'match' and j.requiredExperienceMin is null then 1 else 2 end asc,
              case when :sortBy = 'newest' then j.postedAt end desc,
              j.postedAt desc
            """,
            countQuery = """
            select count(distinct j.id) from SearchHistory sh join sh.jobs j
            left join j.company c
            left join j.requiredSkills rs
            left join j.additionalSkills ads
            where sh.id = :searchHistoryId and j.active = true and j.lastVerifiedAt > :cutoff
              and (:countryCode is null or upper(j.countryCode) = upper(:countryCode)
                   or (j.countryCode is null and (:city is not null and lower(j.location) like lower(concat('%', :city, '%'))))
                   or (j.countryCode is null and :countryName is not null and lower(j.location) like lower(concat('%', :countryName, '%'))))
              and (:city is null or lower(j.location) like lower(concat('%', :city, '%')))
              and (:state is null or lower(j.location) like lower(concat('%', :state, '%')))
              and (:title is null or lower(j.title) like lower(concat('%', :title, '%')))
              and (:company is null or lower(c.name) like lower(concat('%', :company, '%')))
              and (:hasSkills = false or lower(rs.name) in :skills or lower(ads.name) in :skills)
              and (:employmentType is null or lower(j.employmentType) like lower(concat('%', :employmentType, '%')))
              and (:workplaceType is null or upper(j.workplaceType) = upper(:workplaceType))
              and (:minSalary is null or coalesce(j.salaryMax, j.salaryMin) >= :minSalary)
              and (:maxSalary is null or coalesce(j.salaryMin, j.salaryMax) <= :maxSalary)
              and (:salaryCurrency is null or upper(j.salaryCurrency) = upper(:salaryCurrency))
              and (:salaryInterval is null or upper(j.salaryInterval) = upper(:salaryInterval))
              and (:postedAfter is null or j.postedAt >= :postedAfter)
            """)
    Page<Job> searchAvailable(@Param("searchHistoryId") Long searchHistoryId, @Param("cutoff") java.time.Instant cutoff,
                              @Param("countryCode") String countryCode, @Param("countryName") String countryName,
                              @Param("city") String city, @Param("state") String state, @Param("title") String title,
                              @Param("company") String company, @Param("hasSkills") boolean hasSkills,
                              @Param("skills") List<String> skills, @Param("employmentType") String employmentType,
                              @Param("workplaceType") String workplaceType, @Param("minSalary") java.math.BigDecimal minSalary,
                              @Param("maxSalary") java.math.BigDecimal maxSalary, @Param("salaryCurrency") String salaryCurrency,
                              @Param("salaryInterval") String salaryInterval, @Param("postedAfter") java.time.Instant postedAfter,
                              @Param("experienceYears") Integer experienceYears, @Param("sortBy") String sortBy, Pageable pageable);
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("update Job j set j.active = false where j.lastVerifiedAt < :cutoff")
    int deactivateUnverifiedBefore(@Param("cutoff") java.time.Instant cutoff);
}

