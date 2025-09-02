package com.journal.journalbackend.service;


import com.journal.journalbackend.dto.request.EntryRequest;
import com.journal.journalbackend.dto.response.EntryResponse;
import com.journal.journalbackend.dto.response.EntryVersionResponse;
import com.journal.journalbackend.dto.response.TagResponse;
import com.journal.journalbackend.model.*;
import com.journal.journalbackend.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EntryService {
    private final EntryRepository entryRepository;
    private final EntryVersionRepository entryVersionRepository;
    private final JournalRepository journalRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;

    public EntryService(EntryRepository entryRepository,
                        JournalRepository journalRepository,
                        UserRepository userRepository,
                        TagRepository tagRepository,
                        EntryVersionRepository entryVersionRepository) {
        this.entryRepository = entryRepository;
        this.journalRepository = journalRepository;
        this.userRepository = userRepository;
        this.tagRepository = tagRepository;
        this.entryVersionRepository = entryVersionRepository;
    }

    // Create new entry with initial version
    public EntryResponse createEntry(Long journalId, EntryRequest entryRequest, String username) {
        Journal journal = getJournalIfOwnedByUser(journalId, username);
        getUserByUsername(username);

        Entry entry = new Entry();
        entry.setTitle(entryRequest.getTitle());
        entry.setBody(entryRequest.getBody());
        entry.setEntryDate(entryRequest.getEntryDate());
        entry.setJournal(journal);
        entry.setCreatedAt(LocalDateTime.now());
        entry.setUpdatedAt(LocalDateTime.now());
        entry.setLastEditedAt(LocalDateTime.now());

        // Create initial version
        EntryVersion initialVersion = createVersion(entry, entryRequest, "1.0.0", username);
        entry.setCurrentVersion(initialVersion);
        entry.getVersions().add(initialVersion);

        Entry savedEntry = entryRepository.save(entry);
        return mapToEntryResponse(savedEntry);
    }

    public List<EntryVersionResponse> getEntryVersions(Long journalId, Long entryId, String username) {
        // Verify journal ownership
        Journal journal = getJournalIfOwnedByUser(journalId, username);

        // Fetch entry including soft-deleted versions
        Entry entry = entryRepository.findByIdAndJournalIdIncludeDeleted(entryId, journalId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entry not found"));

        // Return all versions (including soft-deleted ones)
        return entry.getVersions().stream()
                .map(this::mapToVersionResponse)
                .collect(Collectors.toList());
    }

    public List<EntryResponse> getEntriesByJournalId(Long journalId, String username) {
        getJournalIfOwnedByUser(journalId, username);
        return entryRepository.findByJournalIdAndDeletedAtIsNull(journalId).stream()
                .map(this::mapToEntryResponse)
                .collect(Collectors.toList());
    }

    public EntryResponse getEntryById(Long journalId, Long entryId, String username) {
        getJournalIfOwnedByUser(journalId, username);
        Entry entry = entryRepository.findByIdAndJournalIdAndDeletedAtIsNull(entryId, journalId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entry not found"));
        return mapToEntryResponse(entry);
    }

    public List<EntryResponse> getRecycleBinEntriesByJournal(Long journalId, String username) {
        getJournalIfOwnedByUser(journalId, username);
        return entryRepository.findByJournalIdAndDeletedAtIsNotNull(journalId).stream()
                .map(this::mapToEntryResponse)
                .collect(Collectors.toList());
    }

    // Update entry with versioning
    public EntryResponse updateEntry(Long journalId, Long entryId, EntryRequest entryRequest, String username) {
        // Verify journal ownership
        getJournalIfOwnedByUser(journalId, username);
        User user = getUserByUsername(username);

        Entry entry = entryRepository.findByIdAndJournalIdAndDeletedAtIsNull(entryId, journalId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entry not found"));


        // Check for significant changes (title or body)
        if (isSignificantChange(entry.getCurrentVersion(), entryRequest)) {
            String newVersion = generateNextVersion(entry.getCurrentVersion().getVersionNumber());
            EntryVersion newVersionObj = createVersion(entry, entryRequest, newVersion, username);
            entryVersionRepository.save(newVersionObj);
            entry.setCurrentVersion(newVersionObj);
            entry.getVersions().add(newVersionObj);
        } else {
            // Only update tags if no content change
            updateTags(entry.getCurrentVersion(), entryRequest, user);
            entryVersionRepository.save(entry.getCurrentVersion());
        }

        // Update entry metadata
        entry.setEntryDate(entryRequest.getEntryDate());
        entry.setUpdatedAt(LocalDateTime.now());
        entry.setLastEditedAt(LocalDateTime.now());

        Entry updatedEntry = entryRepository.save(entry);
        return mapToEntryResponse(updatedEntry);
    }

    // Soft delete entry and versions
    public void softDeleteEntry(Long journalId, Long entryId, String username) {
        // Verify journal ownership
        getJournalIfOwnedByUser(journalId, username);

        Entry entry = entryRepository.findByIdAndJournalIdAndDeletedAtIsNull(entryId, journalId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entry not found"));

        // Soft delete the entry
        entry.setDeletedAt(LocalDateTime.now());
        entryRepository.save(entry);

        // Soft delete all versions
        for (EntryVersion version : entry.getVersions()) {
            version.setDeletedAt(LocalDateTime.now());
        }
        entryVersionRepository.saveAll(entry.getVersions());
    }

    // Restore entry and versions
    public void restoreEntry(Long journalId, Long entryId, String username) {
        Journal journal = getJournalIfOwnedByUser(journalId, username);

        // Using custom method that includes soft-deleted entries
        Entry entry = entryRepository.findByIdAndJournalIdIncludeDeleted(entryId, journalId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entry not found"));

        if (entry.getDeletedAt() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Entry is not deleted");
        }

        // Restore the entry
        entry.setDeletedAt(null);
        entryRepository.save(entry);

        // Restore all versions
        for (EntryVersion version : entry.getVersions()) {
            version.setDeletedAt(null);
        }
        entryVersionRepository.saveAll(entry.getVersions());
    }

    // Restore specific version
    @Transactional
    public EntryResponse restoreVersion(Long journalId, Long entryId, Long versionId, String username) {
        // Verify journal ownership
        getJournalIfOwnedByUser(journalId, username);

        // Fetch entry including soft-deleted entries
        Entry entry = entryRepository.findByIdAndJournalIdIncludeDeleted(entryId, journalId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entry not found"));

        // If entry is soft-deleted, restore it first
        if (entry.getDeletedAt() != null) {
            entry.setDeletedAt(null);
            entryRepository.save(entry);

            // Restore all versions
            for (EntryVersion version : entry.getVersions()) {
                version.setDeletedAt(null);
            }
            entryVersionRepository.saveAll(entry.getVersions());
        }

        // Fetch the specific version to restore
        EntryVersion versionToRestore = entryVersionRepository.findByIdAndEntryId(versionId, entryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Version not found"));

        // Create new version from restored content
        EntryRequest restoreRequest = new EntryRequest();
        restoreRequest.setTitle(versionToRestore.getTitle());
        restoreRequest.setBody(versionToRestore.getBody());
        restoreRequest.setEntryDate(entry.getEntryDate());
        restoreRequest.setTagIds(versionToRestore.getTags().stream()
                .map(Tag::getId)
                .collect(Collectors.toList()));

        return updateEntry(journalId, entryId, restoreRequest, username);
    }

    // Hard delete old versions (scheduled)
    @Scheduled(cron = "0 0 1 * * ?") // Daily at 1 AM
    @Transactional
    public void purgeOldVersions() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);

        // Find versions deleted more than 30 days ago
        List<EntryVersion> oldVersions = entryVersionRepository.findSoftDeletedBefore(cutoff);
        entryVersionRepository.deleteAll(oldVersions);

        // Find entries that are soft-deleted and have no versions left
        List<Entry> orphanedEntries = entryRepository.findSoftDeletedWithNoVersions();
        entryRepository.deleteAll(orphanedEntries);
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

    @Transactional(readOnly = true)
    public List<LocalDate> getDistinctEntryDates() {
        return entryRepository.findDistinctEntryDates();
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

    // Helper method: Create new version
    private EntryVersion createVersion(Entry entry, EntryRequest request, String versionNumber, String username) {
        // Get the User object instead of just the username
        User user = getUserByUsername(username);

        EntryVersion version = new EntryVersion();
        version.setTitle(request.getTitle());
        version.setBody(request.getBody());
        version.setVersionNumber(versionNumber);
        version.setEntry(entry);
        version.setCreatedAt(LocalDateTime.now());
        version.setCreatedBy(user);  // Set User object here
        version.setTags(processTags(request.getTagIds(), user));

        // ✅ Fixes below
        version.setEntryDate(entry.getEntryDate()); // ensure this is not null
        version.setDeleted(false); // non-nullable column

        return version;
    }


    // Helper method: Check for significant changes
    private boolean isSignificantChange(EntryVersion current, EntryRequest request) {
        return !current.getTitle().equals(request.getTitle()) ||
                !current.getBody().equals(request.getBody());
    }

    // Helper method: Process tags
    private Set<Tag> processTags(List<Long> tagIds, User user) {
        // Remove username lookup since we already have the User object
        if (tagIds == null || tagIds.isEmpty()) {
            return Collections.emptySet();
        }

        Set<Tag> tags = new HashSet<>();
        for (Long tagId : tagIds) {
            Tag tag = tagRepository.findById(tagId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tag not found: " + tagId));

            if (!tag.getUser().getId().equals(user.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to tag: " + tagId);
            }
            tags.add(tag);
        }
        return tags;
    }

    // Helper method: map to version response
    private EntryVersionResponse mapToVersionResponse(EntryVersion version) {
        EntryVersionResponse response = new EntryVersionResponse();
        response.setId(version.getId());
        response.setTitle(version.getTitle());
        response.setBody(version.getBody());
        response.setVersionNumber(version.getVersionNumber());
        response.setCreatedAt(version.getCreatedAt());
        response.setUpdatedAt(version.getUpdatedAt());
        response.setDeletedAt(version.getDeletedAt());

        // Map tags
        List<TagResponse> tagResponses = version.getTags().stream()
                .map(this::mapToTagResponse)
                .collect(Collectors.toList());
        response.setTags(tagResponses);

        return response;
    }

    // Helper method: Update tags for existing version
    private void updateTags(EntryVersion version, EntryRequest request, User user) {
        Set<Tag> newTags = processTags(request.getTagIds(), user);
        version.setTags(newTags);
        version.setUpdatedAt(LocalDateTime.now());
    }

    // Helper method: Generate next semantic version
    private String generateNextVersion(String currentVersion) {
        String[] parts = currentVersion.split("\\.");
        if (parts.length != 3) {
            // Fallback to simple increment if format is invalid
            return currentVersion + ".1";
        }

        try {
            int patch = Integer.parseInt(parts[2]) + 1;
            return parts[0] + "." + parts[1] + "." + patch;
        } catch (NumberFormatException e) {
            // Fallback to simple increment
            return currentVersion + ".1";
        }
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

        // Include tags in the response
        List<TagResponse> tagResponses = entry.getTags().stream()
                .map(this::mapToTagResponse)
                .collect(Collectors.toList());
        response.setTags(tagResponses);
        return response;


    }

    private TagResponse mapToTagResponse(Tag tag) {
        TagResponse response = new TagResponse();
        response.setId(tag.getId());
        response.setName(tag.getName());
        response.setCreatedAt(tag.getCreatedAt());
        return response;
    }
}

