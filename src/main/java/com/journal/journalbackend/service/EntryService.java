package com.journal.journalbackend.service;


import com.journal.journalbackend.dto.request.EntryRequest;
import com.journal.journalbackend.dto.response.EntryResponse;
import com.journal.journalbackend.model.Entry;
import com.journal.journalbackend.model.Journal;
import com.journal.journalbackend.model.User;
import com.journal.journalbackend.repository.EntryRepository;
import com.journal.journalbackend.repository.JournalRepository;
import com.journal.journalbackend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EntryService {
    private final EntryRepository entryRepository;
    private final JournalRepository journalRepository;
    private final UserRepository userRepository;

    public EntryService(EntryRepository entryRepository, JournalRepository journalRepository, UserRepository userRepository) {
        this.entryRepository = entryRepository;
        this.journalRepository = journalRepository;
        this.userRepository = userRepository;
    }

    public EntryResponse createEntry(Long journalId, EntryRequest entryRequest, String username) {
        Journal journal = getJournalIfOwnedByUser(journalId, username);

        Entry entry = new Entry();
        entry.setTitle(entryRequest.getTitle());
        entry.setBody(entryRequest.getBody());
        entry.setEntryDate(entryRequest.getEntryDate());
        entry.setJournal(journal);

        Entry savedEntry = entryRepository.save(entry);
        return mapToEntryResponse(savedEntry);
    }

    public List<EntryResponse> getEntriesByJournalId(Long journalId, String username) {
        getJournalIfOwnedByUser(journalId, username); // Just to verify access

        return entryRepository.findByJournalId(journalId).stream()
                .map(this::mapToEntryResponse)
                .collect(Collectors.toList());
    }

    public EntryResponse getEntryById(Long journalId, Long entryId, String username) {
        getJournalIfOwnedByUser(journalId, username); // Ensure access

        Entry entry = entryRepository.findByIdAndJournalId(entryId, journalId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entry not found"));

        return mapToEntryResponse(entry);
    }

    public EntryResponse updateEntry(Long journalId, Long entryId, EntryRequest entryRequest, String username) {
        getJournalIfOwnedByUser(journalId, username); // Ensure access

        Entry entry = entryRepository.findByIdAndJournalId(entryId, journalId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entry not found"));

        entry.setTitle(entryRequest.getTitle());
        entry.setBody(entryRequest.getBody());
        entry.setEntryDate(entryRequest.getEntryDate());
        entry.setLastEditedAt(LocalDateTime.now());

        Entry updatedEntry = entryRepository.save(entry);
        return mapToEntryResponse(updatedEntry);
    }

    public void deleteEntry(Long journalId, Long entryId, String username) {
        getJournalIfOwnedByUser(journalId, username); // Ensure access

        if (!entryRepository.existsByIdAndJournalId(entryId, journalId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Entry not found");
        }

        entryRepository.deleteById(entryId);
    }


    @Transactional(readOnly = true)
    public long getEntryCountForUserBetweenDates(Long userId, LocalDateTime start, LocalDateTime end) {
        return entryRepository.countByUserAndDateRange(userId, start, end);
    }

    // 🔁 Reused helper methods to remove repetitive logic
    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private Journal getJournalIfOwnedByUser(Long journalId, String username) {
        User user = getUserByUsername(username);
        Journal journal = journalRepository.findById(journalId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Journal not found"));

        if (!journal.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have access to this journal");
        }

        return journal;
    }

    private EntryResponse mapToEntryResponse(Entry entry) {
        EntryResponse response = new EntryResponse();
        response.setId(entry.getId());
        response.setTitle(entry.getTitle());
        response.setBody(entry.getBody());
        response.setEntryDate(entry.getEntryDate());
        response.setJournalId(entry.getJournal().getId());
        response.setCreatedAt(entry.getCreatedAt());
        response.setUpdatedAt(entry.getUpdatedAt());
        response.setLastEditedAt(entry.getLastEditedAt());
        return response;
    }
}

