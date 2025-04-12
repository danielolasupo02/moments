package com.journal.journalbackend.service;

import com.journal.journalbackend.dto.request.JournalRequest;
import com.journal.journalbackend.dto.request.JournalUpdateRequest;
import com.journal.journalbackend.dto.response.JournalResponse;
import com.journal.journalbackend.model.Journal;
import com.journal.journalbackend.model.User;
import com.journal.journalbackend.repository.JournalRepository;
import com.journal.journalbackend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class JournalService {
    private final JournalRepository journalRepository;
    private final UserRepository userRepository;
    public JournalService(JournalRepository journalRepository, UserRepository userRepository) {
        this.journalRepository = journalRepository;
        this.userRepository = userRepository;
    }



    public JournalResponse createJournal(JournalRequest journalRequest, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Journal journal = new Journal();
        journal.setTitle(journalRequest.getTitle());
        journal.setUser(user);

        Journal savedJournal = journalRepository.save(journal);

        return mapToJournalResponse(savedJournal);
    }

    public List<JournalResponse> getJournalsByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        List<Journal> journals = journalRepository.findByUser(user);

        return journals.stream()
                .map(this::mapToJournalResponse)
                .toList();
    }

    public JournalResponse getJournalByIdAndUsername(Long journalId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Journal journal = journalRepository.findById(journalId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Journal not found"));

        if (!journal.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        return mapToJournalResponse(journal);
    }

    @Transactional
    public JournalResponse updateJournal(Long journalId, String username, JournalUpdateRequest updateRequest) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Find the journal by ID
        Journal journal = journalRepository.findById(journalId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Journal not found with id: " + journalId));

        // Check if the journal belongs to the user
        if (!journal.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You don't have permission to update this journal");
        }

        // Update the journal title
        journal.setTitle(updateRequest.getTitle());

        // Save the updated journal - updatedAt timestamp will be automatically updated by @UpdateTimestamp
        Journal updatedJournal = journalRepository.save(journal);

        // Convert to response DTO and return
        return mapToJournalResponse(updatedJournal);
    }



    private JournalResponse mapToJournalResponse(Journal journal) {
        JournalResponse response = new JournalResponse();
        response.setId(journal.getId());
        response.setTitle(journal.getTitle());
        response.setUserId(journal.getUser().getId());
        response.setCreatedAt(journal.getCreatedAt());
        response.setUpdatedAt(journal.getUpdatedAt());
        return response;
    }

}