package com.example.library.service;

import com.example.library.exception.FileStorageException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Saves uploaded files to a folder on the local disk.
 * Only the generated file name is stored in the database.
 */
@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of("jpg", "jpeg", "png", "gif", "webp");

    private final Path uploadDir;

    public FileStorageService(@Value("${app.upload.dir}") String uploadDir) {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new FileStorageException(
                    "Could not create upload folder " + uploadDir, e);
        }
    }

    /**
     * Validates and saves an image, returning the generated file name.
     */
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("Please select a file to upload");
        }

        String originalName = StringUtils.cleanPath(
                String.valueOf(file.getOriginalFilename()));
        String extension = StringUtils.getFilenameExtension(originalName);
        if (extension == null
                || !ALLOWED_EXTENSIONS.contains(
                        extension.toLowerCase(Locale.ROOT))) {
            throw new FileStorageException(
                    "Only JPG, PNG, GIF and WEBP images are allowed");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new FileStorageException("The uploaded file is not an image");
        }

        String storedName = UUID.randomUUID() + "."
                + extension.toLowerCase(Locale.ROOT);
        Path target = resolve(storedName);

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new FileStorageException(
                    "Could not save file " + originalName, e);
        }
        return storedName;
    }

    /**
     * Deletes a stored file. Does nothing if the name is null.
     */
    public void delete(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return;
        }
        try {
            Files.deleteIfExists(resolve(fileName));
        } catch (IOException e) {
            throw new FileStorageException(
                    "Could not delete file " + fileName, e);
        }
    }

    /**
     * Deletes the file once the current database transaction commits,
     * so a failed save never leaves a novel pointing to a missing file.
     */
    public void deleteAfterCommit(String fileName) {
        if (fileName == null) {
            return;
        }
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            delete(fileName);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        delete(fileName);
                    }
                });
    }

    /**
     * Deletes a newly saved file if the current transaction rolls back,
     * so no unused files are left on disk.
     */
    public void deleteOnRollback(String fileName) {
        if (fileName == null
                || !TransactionSynchronizationManager
                        .isSynchronizationActive()) {
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCompletion(int status) {
                        if (status == STATUS_ROLLED_BACK) {
                            delete(fileName);
                        }
                    }
                });
    }

    public Path getUploadDir() {
        return uploadDir;
    }

    private Path resolve(String fileName) {
        Path path = uploadDir.resolve(fileName).normalize();
        if (!path.startsWith(uploadDir)) {
            throw new FileStorageException("Invalid file name " + fileName);
        }
        return path;
    }
}
