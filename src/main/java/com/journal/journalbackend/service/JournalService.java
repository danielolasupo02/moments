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