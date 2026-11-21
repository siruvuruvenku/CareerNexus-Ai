package com.careernexus.skill;

import com.careernexus.common.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/skills")
public class SkillController {
    private final SkillRepository skills;
    private final SkillCatalogService catalog;
    public SkillController(SkillRepository skills, SkillCatalogService catalog) { this.skills = skills; this.catalog = catalog; }
    @GetMapping public ApiResponse<List<SkillView>> list() { return ApiResponse.ok(skills.findAllByOrderByCategoryAscNameAsc().stream().map(s -> new SkillView(s.getId(), s.getName(), s.getCategory(), s.isCustom())).toList()); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SkillView> add(@Valid @RequestBody CustomSkill body) {
        Skill skill = catalog.findOrCreate(body.name(), true); return ApiResponse.ok(new SkillView(skill.getId(), skill.getName(), skill.getCategory(), skill.isCustom()));
    }
    public record CustomSkill(@NotBlank @Size(max = 100) String name) {}
    public record SkillView(Long id, String name, String category, boolean custom) {}
}

