package com.journal.journalbackend.controller;

import com.journal.journalbackend.dto.request.EntryRequest;
import com.journal.journalbackend.dto.response.EntryResponse;
import com.journal.journalbackend.dto.response.TagResponse;
import com.journal.journalbackend.service.EntryService;
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
@RequestMapping("/api/journals/{journalId}/entries")
@Tag(name = "Entry Controller", description = "Endpoints for managing journal entries")
@SecurityRequirement(name = "bearerAuth")
public class EntryController {
    private final EntryService entryService;
    private final TagService tagService;

    public EntryController(EntryService entryService, TagService tagService) {
        this.entryService = entryService;
        this.tagService = tagService;
    }
    @PostMapping
    @Operation(summary = "Create a new entry for a journal")
    public ResponseEntity<EntryResponse> createEntry(
            @PathVariable Long journalId,
            @Valid @RequestBody EntryRequest entryRequest,
            Principal principal) {

        EntryResponse response = entryService.createEntry(journalId, entryRequest, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all entries for a journal")
    public ResponseEntity<List<EntryResponse>> getEntriesByJournalId(
            @PathVariable Long journalId,
            Principal principal) {

        List<EntryResponse> entries = entryService.getEntriesByJournalId(journalId, principal.getName());
        return ResponseEntity.ok(entries);
    }

    @GetMapping("/{entryId}")
    @Operation(summary = "Get a specific entry by ID")
    public ResponseEntity<EntryResponse> getEntryById(
            @PathVariable Long journalId,
            @PathVariable Long entryId,
            Principal principal) {

        EntryResponse entry = entryService.getEntryById(journalId, entryId, principal.getName());
        return ResponseEntity.ok(entry);
    }

    @PutMapping("/{entryId}")
    @Operation(summary = "Update an existing entry")
    public ResponseEntity<EntryResponse> updateEntry(
            @PathVariable Long journalId,
            @PathVariable Long entryId,
            @Valid @RequestBody EntryRequest entryRequest,
            Principal principal) {

        EntryResponse response = entryService.updateEntry(journalId, entryId, entryRequest, principal.getName());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{entryId}")
    @Operation(summary = "Delete an entry")
    public ResponseEntity<Void> deleteEntry(
            @PathVariable Long journalId,
            @PathVariable Long entryId,
            Principal principal) {

        entryService.deleteEntry(journalId, entryId, principal.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{entryId}/tags")
    @Operation(summary = "Get all tags for an entry")
    public ResponseEntity<List<TagResponse>> getTagsForEntry(
            @PathVariable Long journalId,
            @PathVariable Long entryId,
            Principal principal) {

        // Verify access to journal first
        entryService.getEntryById(journalId, entryId, principal.getName());

        List<TagResponse> tags = tagService.getTagsForEntry(entryId, principal.getName());
        return ResponseEntity.ok(tags);
    }


}
