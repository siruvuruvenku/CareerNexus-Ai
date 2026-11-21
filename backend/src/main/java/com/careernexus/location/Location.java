package com.careernexus.location;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "locations", uniqueConstraints = @UniqueConstraint(columnNames = {"country_id", "city"}), indexes = @Index(name = "ix_location_city", columnList = "city"))
@Getter @Setter @NoArgsConstructor
public class Location {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;
    @Column(nullable = false, length = 160)
    private String city;
}

