package org.example.services;

import org.example.dto.upload.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    FileUploadResponse storeImage(MultipartFile file, String folder);
    void deleteImageByUrl(String fileUrl);
    void deleteManagedImageIfExists(String fileUrl);
}
