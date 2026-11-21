package com.careernexus.profile;

import com.careernexus.skill.Skill;
import com.careernexus.skill.SkillCatalogService;
import com.careernexus.user.AppUser;
import com.careernexus.user.AppUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class ProfileService {
    private final AppUserRepository users;
    private final SkillCatalogService skillCatalog;
    private final Path uploadDirectory;
    public ProfileService(AppUserRepository users, SkillCatalogService skillCatalog, @Value("${app.uploads.directory}") String uploadDirectory) {
        this.users = users; this.skillCatalog = skillCatalog; this.uploadDirectory = Path.of(uploadDirectory).toAbsolutePath().normalize();
    }

    @Transactional(readOnly = true)
    public ProfileDtos.ProfileView get(String email) {
        UserProfile p = profile(email);
        return view(p);
    }
    @Transactional
    public ProfileDtos.ProfileView update(String email, ProfileDtos.ProfileUpdate body) {
        UserProfile p = profile(email);
        if (body.fullName() != null) p.setFullName(body.fullName().trim());
        if (body.targetTitle() != null) p.setTargetTitle(body.targetTitle().trim());
        if (body.countryCode() != null) p.setCountryCode(body.countryCode().isBlank() ? null : body.countryCode().trim().toUpperCase(Locale.ROOT));
        if (body.city() != null) p.setCity(body.city().isBlank() ? null : body.city().trim());
        if (body.yearsExperience() != null) p.setYearsExperience(body.yearsExperience());
        if (body.summary() != null) p.setSummary(body.summary());
        if (body.skills() != null) {
            Set<Skill> selected = body.skills().stream().filter(s -> s != null && !s.isBlank()).map(s -> skillCatalog.findOrCreate(s, true)).collect(java.util.stream.Collectors.toSet());
            p.setSkills(selected);
        }
        return view(p);
    }

    @Transactional
    public ProfileDtos.ResumeView upload(String email, MultipartFile file) {
        if (file == null || file.isEmpty() || file.getSize() > 5 * 1024 * 1024) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Resume must be a file under 5 MB");
        String originalName = file.getOriginalFilename() == null ? "resume" : Path.of(file.getOriginalFilename()).getFileName().toString();
        String extension = originalName.contains(".") ? originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT) : "";
        if (!Set.of("pdf", "doc", "docx").contains(extension)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Upload a PDF, DOC, or DOCX resume");
        try {
            Files.createDirectories(uploadDirectory);
            String storedName = UUID.randomUUID() + "." + extension;
            Path destination = uploadDirectory.resolve(storedName).normalize();
            if (!destination.startsWith(uploadDirectory)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file name");
            file.transferTo(destination);
            UserProfile p = profile(email);
            if (p.getResumePath() != null) Files.deleteIfExists(uploadDirectory.resolve(p.getResumePath()).normalize());
            p.setResumePath(storedName);
            String contentType = switch (extension) { case "pdf" -> "application/pdf"; case "doc" -> "application/msword"; default -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"; };
            return new ProfileDtos.ResumeView(originalName, contentType);
        } catch (IOException e) { throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Resume could not be saved"); }
    }

    @Transactional(readOnly = true)
    public DownloadedResume download(String email) {
        UserProfile p = profile(email);
        if (p.getResumePath() == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No resume uploaded");
        Path path = uploadDirectory.resolve(p.getResumePath()).normalize();
        if (!path.startsWith(uploadDirectory) || !Files.exists(path)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Resume file is unavailable");
        String extension = p.getResumePath().substring(p.getResumePath().lastIndexOf('.') + 1);
        String type = switch (extension) { case "pdf" -> "application/pdf"; case "doc" -> "application/msword"; default -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"; };
        return new DownloadedResume(new FileSystemResource(path), type);
    }

    private UserProfile profile(String email) {
        AppUser user = users.findByEmailIgnoreCase(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (user.getProfile() == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found");
        return user.getProfile();
    }
    private ProfileDtos.ProfileView view(UserProfile p) {
        return new ProfileDtos.ProfileView(p.getId(), p.getUser().getEmail(), p.getFullName(), p.getTargetTitle(), p.getCountryCode(), p.getCity(),
                p.getYearsExperience(), p.getSummary(), p.getSkills().stream().map(Skill::getName).sorted().toList(), p.getResumePath() != null);
    }
    public record DownloadedResume(Resource resource, String contentType) {}
}

