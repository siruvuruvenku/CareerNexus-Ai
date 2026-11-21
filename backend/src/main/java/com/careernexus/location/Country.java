package com.careernexus.location;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "countries")
@Getter @Setter @NoArgsConstructor
public class Country {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 2)
    private String code;
    @Column(nullable = false, length = 120)
    private String name;
}

