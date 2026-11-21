package com.careernexus.profile;

import com.careernexus.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/users/profile")
public class ProfileController {
    private final ProfileService profiles;
    public ProfileController(ProfileService profiles) { this.profiles = profiles; }
    @GetMapping public ApiResponse<ProfileDtos.ProfileView> get(Authentication auth) { return ApiResponse.ok(profiles.get(auth.getName())); }
    @PutMapping public ApiResponse<ProfileDtos.ProfileView> update(Authentication auth, @Valid @RequestBody ProfileDtos.ProfileUpdate body) { return ApiResponse.ok(profiles.update(auth.getName(), body)); }
    @PostMapping(value = "/resume", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ProfileDtos.ResumeView> upload(Authentication auth, @RequestPart("file") MultipartFile file) { return ApiResponse.ok(profiles.upload(auth.getName(), file)); }
    @GetMapping("/resume")
    public ResponseEntity<Resource> download(Authentication auth) {
        var file = profiles.download(auth.getName());
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(file.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("resume", StandardCharsets.UTF_8).build().toString()).body(file.resource());
    }
}

