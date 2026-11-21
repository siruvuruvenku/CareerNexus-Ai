package com.careernexus.location;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LocationRepository extends JpaRepository<Location, Long> {
    List<Location> findDistinctByCountry_CodeIgnoreCaseOrderByCityAsc(String code);
}

