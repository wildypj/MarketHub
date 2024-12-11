package com.ecommerce.project.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService{

    @Override
    public String uploadImage(String path, MultipartFile file) throws IOException {
        // First get file names of current file
        String originalFilename = file.getOriginalFilename();

        // Generate a unique file name
        String randomId = UUID.randomUUID().toString();
        // Ex: of code line below -- mat.jpg --> 1234 --> 1234.jpg
        String fileName = randomId.concat(originalFilename.substring(originalFilename.lastIndexOf('.')));
        String filePath = path + File.separator + fileName;

        // Check if path exists and create
        File folder  = new File(path);
        if (!folder.exists()) {
            folder.mkdir();
        }
        // Upload to Server
        Files.copy(file.getInputStream(), Paths.get(filePath));

        // Return file name
        return fileName;
    }

}
