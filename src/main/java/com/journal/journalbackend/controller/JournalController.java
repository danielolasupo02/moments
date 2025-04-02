package com.journal.journalbackend.controller;

import com.journal.journalbackend.dto.request.JournalRequest;
import com.journal.journalbackend.dto.response.JournalResponse;
import com.journal.journalbackend.model.User;
import com.journal.journalbackend.service.JournalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/journals")
@Tag(name = "Journal Controller", description = "Endpoints for managing journals")
@SecurityRequirement(name = "bearerAuth")
public class JournalController {
    private final JournalService journalService;

    public JournalController(JournalService journalService) {
        this.journalService = journalService;
    }


    @PostMapping
    @Operation(summary = "Create a new journal")
    public ResponseEntity<JournalResponse> createJournal(
            @Valid @RequestBody JournalRequest journalRequest,
            Principal principal
    ) {
        String username = principal.getName();
        JournalResponse journalResponse = journalService.createJournal(journalRequest, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(journalResponse);
    }

    @GetMapping
    @Operation(summary = "Get all journals for the logged-in user")
    public ResponseEntity<List<JournalResponse>> getMyJournals(Principal principal) {
        String username = principal.getName();
        List<JournalResponse> journals = journalService.getJournalsByUsername(username);
        return ResponseEntity.ok(journals);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a specific journal by ID")
    public ResponseEntity<JournalResponse> getJournalById(
            @PathVariable Long id,
            Principal principal) {

        String username = principal.getName();
        JournalResponse journal = journalService.getJournalByIdAndUsername(id, username);
        return ResponseEntity.ok(journal);
    }

}