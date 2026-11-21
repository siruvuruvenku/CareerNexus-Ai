package com.careernexus.profile;

import com.careernexus.skill.Skill;
import com.careernexus.user.AppUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "user_profiles")
@Getter @Setter @NoArgsConstructor
public class UserProfile {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private AppUser user;
    @Column(name = "full_name", nullable = false, length = 160)
    private String fullName;
    @Column(name = "target_title", length = 160)
    private String targetTitle;
    @Column(name = "country_code", length = 2)
    private String countryCode;
    @Column(length = 160)
    private String city;
    @Column(name = "years_experience", nullable = false)
    private Integer yearsExperience = 0;
    @Column(columnDefinition = "TEXT")
    private String summary;
    @Column(name = "resume_path", length = 500)
    private String resumePath;
    @ManyToMany
    @JoinTable(name = "user_skills", joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "user_id"), inverseJoinColumns = @JoinColumn(name = "skill_id"))
    private Set<Skill> skills = new HashSet<>();
}

