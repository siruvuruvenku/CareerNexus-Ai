package com.careernexus.location;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CountryRepository extends JpaRepository<Country, Long> {
    List<Country> findAllByOrderByNameAsc();
    Optional<Country> findByCodeIgnoreCase(String code);
}

