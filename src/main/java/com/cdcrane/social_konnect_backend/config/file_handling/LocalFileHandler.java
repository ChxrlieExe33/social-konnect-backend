package com.cdcrane.social_konnect_backend.config.file_handling;

import com.cdcrane.social_konnect_backend.config.exceptions.FileTypeNotValidException;
import com.cdcrane.social_konnect_backend.posts.post_media.PostMedia;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
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
@Slf4j
public class LocalFileHandler implements FileHandler {

    @Value("${app.backend-base-url}")
    private String backendBaseUrl;

    /**
     * Take a list of MultipartFile, validate them, and call saveFile method to handle saving to the local filesystem.
     * @param files The list of MultipartFile to store.
     * @return A list of PostMedia, which can be added to a Post entity before persisting.
     */
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

            Map<String, String> fileInfo = storeFile(file);

            String fileUrl = backendBaseUrl + "/media/" + fileInfo.get("filename");


            media.add(PostMedia.builder()
                    .mediaType(fileInfo.get("mimeType"))
                    .mediaUrl(fileUrl)
                    .fileName(fileInfo.get("filename"))
                    .build());

        }

        // TODO: Implement real error/exception handling for the file saving.
        for (String error : errors) {
            log.warn("File upload failed for file: {} , cause {} .", error, error);
        }

        return media;

    }

    /**
     * Handle the saving in the local filesystem of each file.
     * @param file The validated MultipartFile to save.
     * @return A HashMap with file information, being fileName and mimeType.
     */
    private Map<String, String> storeFile(MultipartFile file) {

        try {

            Map<String, String> fileInfo = new HashMap<>();

            String originalName = file.getOriginalFilename();

            if (originalName == null) {
                originalName = "no_name_file";
            }

            // For checking and validating file type, much more accurate than the native support.
            Tika tika = new Tika();
            String mimeType = tika.detect(file.getInputStream());
            String actualType;

            if (FileHandlerConstants.ALLOWED_IMAGE_TYPES.contains(mimeType)) {

                actualType = FileHandlerConstants.IMAGE_TYPE;

            } else if (FileHandlerConstants.ALLOWED_VIDEO_TYPES.contains(mimeType)) {

                actualType = FileHandlerConstants.VIDEO_TYPE;

            } else {

                throw new FileTypeNotValidException("File type not supported: " + mimeType + " for file: " + originalName + " .");

            }


            // Remove all spaces from original name
            originalName = originalName.replace(" ", "");

            // Generate unique name even if files with same name are uploaded twice.
            String fileName = System.currentTimeMillis() + "_" + originalName;


            Path upload = Paths.get("uploads/", fileName);

            // Create dir if it doesn't exist
            Files.createDirectories(upload.getParent());

            Files.copy(file.getInputStream(), upload, StandardCopyOption.REPLACE_EXISTING);

            fileInfo.put("filename", fileName);
            fileInfo.put("mimeType", actualType);

            return fileInfo;

        } catch (IOException e) {

            throw new RuntimeException(e.toString());
        }


    }

    public void deleteFile(String fileName) {

        try {

            Path file = Paths.get("uploads/" + fileName);

            Files.delete(file);

        } catch (IOException e) {

            log.warn("File deletion failed for file: {} , cause {} .", fileName, e.getMessage());

        }
    }

    @Override
    public String saveNewProfilePicture(MultipartFile file) {

        try {

            String originalName = file.getOriginalFilename();

            if (originalName == null) {
                originalName = "profile_picture";
            }

            // For checking and validating file type, much more accurate than the native support.
            Tika tika = new Tika();
            String mimeType = tika.detect(file.getInputStream());
            String actualType;

            if (!FileHandlerConstants.ALLOWED_IMAGE_TYPES.contains(mimeType)) {

                throw new FileTypeNotValidException("File type not supported: " + mimeType + " for file: " + originalName + " .");

            }

            // Remove all spaces from original name
            originalName = originalName.replace(" ", "");

            // Generate unique name even if files with same name are uploaded twice.
            String fileName = System.currentTimeMillis() + "_profile_picture_" + originalName;

            Path upload = Paths.get("uploads/", fileName);

            // Create dir if it doesn't exist
            Files.createDirectories(upload.getParent());

            Files.copy(file.getInputStream(), upload, StandardCopyOption.REPLACE_EXISTING);

            return backendBaseUrl + "/media/" + fileName;


        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
