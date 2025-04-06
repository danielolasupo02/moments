package com.journal.journalbackend.service;

import com.journal.journalbackend.dto.response.MediaResponse;
import com.journal.journalbackend.model.Entry;
import com.journal.journalbackend.model.Media;
import com.journal.journalbackend.repository.EntryRepository;
import com.journal.journalbackend.repository.MediaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MediaService {

    private final MediaRepository mediaRepository;
    private final EntryRepository entryRepository;
    private final Path fileStorageLocation;

    public MediaService(
            MediaRepository mediaRepository,
            EntryRepository entryRepository,
            @Value("${app.file.upload-dir:uploads}") String uploadDir) {
        this.mediaRepository = mediaRepository;
        this.entryRepository = entryRepository;

        this.fileStorageLocation = Paths.get(uploadDir)
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public MediaResponse uploadMedia(Long entryId, MultipartFile file, String description) {
        // Validate entry exists
        Entry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entry not found with id: " + entryId));

        // Validate file is not empty
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to store empty file");
        }

        try {
            // Generate a unique filename
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID().toString() + fileExtension;

            // Create media record
            Media media = new Media();
            media.setEntry(entry);
            media.setFilename(filename);
            media.setOriginalFilename(originalFilename);
            media.setFileType(file.getContentType());
            media.setFileSize(file.getSize());
            media.setDescription(description);
            media.setUploadDate(LocalDateTime.now());

            // Store file on disk
            Path targetLocation = this.fileStorageLocation.resolve(filename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Save media record to database
            Media savedMedia = mediaRepository.save(media);

            return convertToMediaResponse(savedMedia);
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not store file. Please try again.", ex);
        }
    }

        public List<MediaResponse> getAllMediaForEntry(Long entryId) {
        // Validate entry exists
        entryRepository.findById(entryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entry not found with id: " + entryId));

        // Get all media for entry
        List<Media> mediaList = mediaRepository.findByEntryId(entryId);

        return mediaList.stream()
                .map(this::convertToMediaResponse)
                .collect(Collectors.toList());
    }


    private MediaResponse convertToMediaResponse(Media media) {
        MediaResponse response = new MediaResponse();
        response.setId(media.getId());
        response.setFilename(media.getOriginalFilename());
        response.setFileType(media.getFileType());
        response.setFileSize(media.getFileSize());
        response.setDescription(media.getDescription());
        response.setUploadDate(media.getUploadDate());
        response.setUrl("/api/entries/" + media.getEntry().getId() + "/media/" + media.getId());
        return response;
    }
}