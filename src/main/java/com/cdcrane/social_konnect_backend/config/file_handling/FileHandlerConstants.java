package com.cdcrane.social_konnect_backend.config.file_handling;

import java.util.Set;

public class FileHandlerConstants {

    public static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/heic",
            "image/heif"
    );

    public static final Set<String> ALLOWED_VIDEO_TYPES = Set.of(
            "video/mp4",
            "video/quicktime",  // MOV
            "video/webm"
    );

    public static final String IMAGE_TYPE = "IMAGE";
    public static final String VIDEO_TYPE = "VIDEO";

}
