package com.journal.journalbackend.service;

import com.journal.journalbackend.dto.request.TagRequest;
import com.journal.journalbackend.dto.response.EntryResponse;
import com.journal.journalbackend.dto.response.TagResponse;
import com.journal.journalbackend.model.Entry;
import com.journal.journalbackend.model.Tag;
import com.journal.journalbackend.model.User;
import com.journal.journalbackend.repository.EntryRepository;
import com.journal.journalbackend.repository.TagRepository;
import com.journal.journalbackend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TagService {
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final EntryRepository entryRepository;

    public TagService(TagRepository tagRepository, UserRepository userRepository, EntryRepository entryRepository) {
        this.tagRepository = tagRepository;
        this.userRepository = userRepository;
        this.entryRepository = entryRepository;
    }

    public TagResponse createTag(TagRequest tagRequest, String username) {
        User user = getUserByUsername(username);

        // Check if tag already exists for this user
        if (tagRepository.existsByNameAndUserId(tagRequest.getName(), user.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tag already exists");
        }

        Tag tag = new Tag();
        tag.setName(tagRequest.getName());
        tag.setUser(user);
        tag.setCreatedAt(LocalDateTime.now());

        Tag savedTag = tagRepository.save(tag);
        return mapToTagResponse(savedTag);
    }

    public List<TagResponse> getAllTagsForUser(String username) {
        User user = getUserByUsername(username);
        return tagRepository.findByUserId(user.getId()).stream()
                .map(this::mapToTagResponse)
                .collect(Collectors.toList());
    }

    public List<TagResponse> searchTagsByName(String nameQuery, String username) {
        User user = getUserByUsername(username);
        return tagRepository.findByUserIdAndNameContainingIgnoreCase(user.getId(), nameQuery).stream()
                .map(this::mapToTagResponse)
                .collect(Collectors.toList());
    }

    public List<EntryResponse> getEntriesByTag(Long tagId, String username) {
        User user = getUserByUsername(username);

        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tag not found"));

        if (!tag.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have access to this tag");
        }

        return tagRepository.findEntriesByTagId(tagId).stream()
                .map(this::mapToEntryResponse)
                .collect(Collectors.toList());
    }

    public List<TagResponse> getTagsForEntry(Long entryId, String username) {
        Entry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entry not found"));

        // Optional: validate ownership here if not already done in controller
        return entry.getTags().stream()
                .map(this::mapToTagResponse)
                .collect(Collectors.toList());
    }

    public void addTagsToEntry(Long entryId, List<Long> tagIds, String username) {
        Entry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entry not found"));

        // Optional: validate ownership if not already validated earlier

        List<Tag> tagsToAdd = tagRepository.findAllById(tagIds);

        entry.getTags().addAll(tagsToAdd);
        entryRepository.save(entry);
    }






    private TagResponse mapToTagResponse(Tag tag) {
        TagResponse response = new TagResponse();
        response.setId(tag.getId());
        response.setName(tag.getName());
        response.setCreatedAt(tag.getCreatedAt());
        return response;
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

        // Add tags to the response
        List<TagResponse> tagResponses = entry.getTags().stream()
                .map(this::mapToTagResponse)
                .collect(Collectors.toList());
        response.setTags(tagResponses);

        return response;
    }

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
