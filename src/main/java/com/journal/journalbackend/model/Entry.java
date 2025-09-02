package com.journal.journalbackend.model;

import jakarta.persistence.*;

import java.time.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "entries", indexes = {
        @Index(name = "idx_entry_user_created", columnList = "journal_id, created_at"),
        @Index(name = "idx_entry_created_at", columnList = "created_at")
})
public class Entry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String body;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journal_id", nullable = false)
    private Journal journal;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "entry_tags",
            joinColumns = @JoinColumn(name = "entry_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"),
            indexes = {
                    @Index(name = "idx_entry_tags_entry", columnList = "entry_id"),
                    @Index(name = "idx_entry_tags_tag", columnList = "tag_id")
            }
    )
    private Set<Tag> tags = new HashSet<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_edited_at")
    private LocalDateTime lastEditedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;  // Soft deletion timestamp

    @OneToMany(mappedBy = "entry", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EntryVersion> versions = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_version_id")
    private EntryVersion currentVersion;

    // Soft delete method (cascades to versions)
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
        this.versions.forEach(EntryVersion::softDelete);
    }

    // Restore method (cascades to versions)
    public void restore() {
        this.deletedAt = null;
        this.versions.forEach(EntryVersion::restore);
    }



    public Entry() {
    }

    public Entry(String title, String body, LocalDate entryDate, Journal journal) {
        this.title = title;
        this.body = body;
        this.entryDate = entryDate;
        this.journal = journal;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public LocalDate getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(LocalDate entryDate) {
        this.entryDate = entryDate;
    }


    public Journal getJournal() {
        return journal;
    }

    public void setJournal(Journal journal) {
        this.journal = journal;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getLastEditedAt() {
        return lastEditedAt;
    }

    public void setLastEditedAt(LocalDateTime lastEditedAt) {
        this.lastEditedAt = lastEditedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public List<EntryVersion> getVersions() {
        return versions;
    }

    public void setVersions(List<EntryVersion> versions) {
        this.versions = versions;
    }

    public EntryVersion getCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(EntryVersion currentVersion) {
        this.currentVersion = currentVersion;
    }

    // Lifecycle methods
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        lastEditedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        lastEditedAt = LocalDateTime.now();
    }


    public ZonedDateTime getCreatedAtInTimezone(ZoneId timezone) {
        return createdAt.atZone(ZoneOffset.UTC).withZoneSameInstant(timezone);
    }

    public ZonedDateTime getUpdatedAtInTimezone(ZoneId timezone) {
        return updatedAt != null ?
                updatedAt.atZone(ZoneOffset.UTC).withZoneSameInstant(timezone) :
                null;
    }
}