package com.journal.journalbackend.controller;

import com.journal.journalbackend.dto.request.TagRequest;
import com.journal.journalbackend.dto.response.TagResponse;
import com.journal.journalbackend.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/tags")
@Tag(name = "Tag Controller", description = "Endpoints for managing tags")
@SecurityRequirement(name = "bearerAuth")
public class TagController {
    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @PostMapping
    @Operation(summary = "Create a new tag")
    public ResponseEntity<TagResponse> createTag(
            @Valid @RequestBody TagRequest tagRequest,
            Principal principal) {

        TagResponse response = tagService.createTag(tagRequest, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all tags for the current user")
    public ResponseEntity<List<TagResponse>> getAllTags(Principal principal) {
        List<TagResponse> tags = tagService.getAllTagsForUser(principal.getName());
        return ResponseEntity.ok(tags);
    }



}
