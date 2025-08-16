package com.cdcrane.social_konnect_backend.config.file_handling;

import com.cdcrane.social_konnect_backend.posts.post_media.PostMedia;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileHandler {

    List<PostMedia> saveFiles(List<MultipartFile> files);

    void deleteFile(String fileName);

    String saveNewProfilePicture(MultipartFile file);

}
