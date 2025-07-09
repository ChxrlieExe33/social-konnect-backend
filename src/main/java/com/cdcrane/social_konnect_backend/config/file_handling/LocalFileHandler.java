package com.cdcrane.social_konnect_backend.config.file_handling;

import com.cdcrane.social_konnect_backend.posts.post_media.PostMedia;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * Class to handle uploaded files in MultipartFile format and store them locally.
 */
@Component
public class LocalFileHandler implements FileHandler {

    @Override
    public List<PostMedia> saveFiles(List<MultipartFile> files) {

        // Log failed files
        List<String> errors = new ArrayList<>();

        // Create media objects, will be persisted when saving the associated post.
        List<PostMedia> media = new ArrayList<>();

        for  (MultipartFile file : files) {

            if(file.isEmpty()) {
                errors.add(file.getOriginalFilename() + " is empty");
                continue;
            }

            String fileName = storeFile(file);

            // TODO: Change this hardcoded URL to be created dynamically.
            String fileUrl = "http://localhost:8080/" + fileName;

            // TODO: Change the media type to be calculated from file/mime type.
            media.add(PostMedia.builder().mediaType("IMAGE").mediaUrl(fileUrl).build());

        }

        // TODO: Implement real error/exception handling for the file saving.
        for (String error : errors) {
            System.err.println(error);
        }

        return media;

    }

    private String storeFile(MultipartFile file) {

        try {

            String originalName = file.getOriginalFilename();

            // Remove all spaces from original name
            originalName = originalName.replace(" ", "");

            // Generate unique name even if files with same name are uploaded twice.
            String fileName = System.currentTimeMillis() + "_" + originalName;

            Path upload = Paths.get("uploads/", fileName);

            // Create dir if it doesn't exist
            Files.createDirectories(upload.getParent());

            Files.copy(file.getInputStream(), upload, StandardCopyOption.REPLACE_EXISTING);

            return fileName;

        } catch (IOException e) {

            // TODO: Create file upload failed exception and handler, which gives details on which files failed.
            throw new RuntimeException(e.toString());
        }


    }
}
