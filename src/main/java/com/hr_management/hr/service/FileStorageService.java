package com.hr_management.hr.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

public interface FileStorageService {
    /**
     * Stores a file.
     * @param file The file to store.
     * @param subDirectory Optional subdirectory within the main upload directory.
     * @return The path relative to the upload directory where the file was stored.
     * @throws IOException If an I/O error occurs.
     */
    String storeFile(MultipartFile file, String subDirectory) throws IOException;

    /**
     * Gets the root storage location.
     * @return Path to the root storage directory.
     */
    Path getRootLocation();
} 