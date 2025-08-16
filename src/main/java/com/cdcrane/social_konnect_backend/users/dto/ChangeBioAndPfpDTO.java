package com.cdcrane.social_konnect_backend.users.dto;

import org.springframework.web.multipart.MultipartFile;

public record ChangeBioAndPfpDTO(String bio, MultipartFile pfp) {
}
