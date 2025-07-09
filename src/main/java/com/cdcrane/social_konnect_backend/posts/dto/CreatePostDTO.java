package com.cdcrane.social_konnect_backend.posts.dto;

import com.cdcrane.social_konnect_backend.posts.post_media.dto.PostMediaDTO;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record CreatePostDTO(@NotBlank @Max(value = 254, message = "Post caption cannot be longer than 254 characters.") String caption,
                            List<MultipartFile> files) {
}
