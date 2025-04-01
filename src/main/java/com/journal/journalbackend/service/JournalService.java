package com.journal.journalbackend.service;

import com.journal.journalbackend.dto.request.JournalRequest;
import com.journal.journalbackend.dto.response.JournalResponse;
import com.journal.journalbackend.model.Journal;
import com.journal.journalbackend.model.User;
import com.journal.journalbackend.repository.JournalRepository;
import com.journal.journalbackend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class JournalService {
    private final JournalRepository journalRepository;
    private final UserRepository userRepository;

    public JournalService(JournalRepository journalRepository, UserRepository userRepository) {
        this.journalRepository = journalRepository;
        this.userRepository = userRepository;
    }

    public JournalResponse createJournal(JournalRequest journalRequest, String username) {
        // Fetch user by username
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Journal journal = new Journal();
        journal.setTitle(journalRequest.getTitle());
        journal.setUser(user); // Hibernate will handle user_id automatically

        Journal savedJournal = journalRepository.save(journal);

        return mapToJournalResponse(savedJournal);
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