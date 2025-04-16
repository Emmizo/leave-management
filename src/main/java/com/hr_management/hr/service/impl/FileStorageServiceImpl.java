package com.hr_management.hr.service.impl;

import com.hr_management.hr.service.FileStorageService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${file.upload-dir:./uploads}") // Default value if property not set
    private String uploadDir;

    private Path rootLocation;

    @PostConstruct
    public void init() throws IOException {
        rootLocation = Paths.get(uploadDir);
        Files.createDirectories(rootLocation);
    }

    @Override
    public String storeFile(MultipartFile file, String subDirectory) throws IOException {
        if (file == null || file.isEmpty()) {
            return null; // Or throw exception if file is mandatory but passed as null
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        if (originalFilename.contains("..")) {
            // This is a security check
            throw new IOException("Cannot store file with relative path outside current directory " + originalFilename);
        }

        // Generate a unique filename to avoid collisions
        String fileExtension = "";
        int lastDot = originalFilename.lastIndexOf('.');
        if (lastDot > 0) {
            fileExtension = originalFilename.substring(lastDot);
        }
        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

        // Create subdirectory if specified and doesn't exist
        Path targetDirectory = this.rootLocation;
        String relativePath = "";
        if (subDirectory != null && !subDirectory.trim().isEmpty()) {
            targetDirectory = this.rootLocation.resolve(Paths.get(subDirectory.trim())).normalize();
            Files.createDirectories(targetDirectory);
            relativePath = Paths.get(subDirectory.trim()).resolve(uniqueFileName).toString();
        } else {
             relativePath = Paths.get(uniqueFileName).toString();
        }
        
        Path targetLocation = targetDirectory.resolve(uniqueFileName).normalize();

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
        }
        
        // Return the path relative to the root upload directory
        return relativePath; 
    }

    @Override
    public Path getRootLocation() {
        return rootLocation;
    }
} 