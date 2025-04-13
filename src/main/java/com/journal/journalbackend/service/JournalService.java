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
        User user = getUserByUsername(username);

        Journal journal = new Journal();
        journal.setTitle(journalRequest.getTitle());
        journal.setUser(user);

        return mapToJournalResponse(journalRepository.save(journal));
    }

    public List<JournalResponse> getJournalsByUsername(String username) {
        User user = getUserByUsername(username);

        return journalRepository.findByUser(user).stream()
                .map(this::mapToJournalResponse)
                .toList();
    }

    public JournalResponse getJournalByIdAndUsername(Long journalId, String username) {
        User user = getUserByUsername(username);
        Journal journal = getJournalById(journalId);

        verifyOwnership(journal, user);

        return mapToJournalResponse(journal);
    }

    @Transactional
    public JournalResponse updateJournal(Long journalId, String username, JournalUpdateRequest updateRequest) {
        User user = getUserByUsername(username);
        Journal journal = getJournalById(journalId);

        verifyOwnership(journal, user);

        journal.setTitle(updateRequest.getTitle());

        return mapToJournalResponse(journalRepository.save(journal));
    }

    // 🔁 Reused methods to remove repetitive logic

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private Journal getJournalById(Long journalId) {
        return journalRepository.findById(journalId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Journal not found"));
    }

    private void verifyOwnership(Journal journal, User user) {
        if (!journal.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
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
