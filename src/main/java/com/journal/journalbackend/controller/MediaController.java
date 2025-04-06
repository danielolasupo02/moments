package com.journal.journalbackend.controller;

import com.journal.journalbackend.dto.response.MediaResponse;
import com.journal.journalbackend.service.MediaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/entries/{entryId}/media")
public class MediaController {

    private final MediaService mediaService;


    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @Operation(
            summary = "Upload media for a journal entry",
            description = "Uploads a file (image, video, audio) and associates it with the specified journal entry"
    )
    @ApiResponse(
            responseCode = "201",
            description = "Media uploaded successfully",
            content = @Content(schema = @Schema(implementation = MediaResponse.class))
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MediaResponse> uploadMedia(
            @PathVariable Long entryId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description) {
        MediaResponse response = mediaService.uploadMedia(entryId, file, description);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Get all media for a journal entry")
    @GetMapping
    public ResponseEntity<List<MediaResponse>> getAllMediaForEntry(@PathVariable Long entryId) {
        List<MediaResponse> mediaList = mediaService.getAllMediaForEntry(entryId);
        return ResponseEntity.ok(mediaList);
    }


}