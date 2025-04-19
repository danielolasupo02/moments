package com.journal.journalbackend.model;

import jakarta.persistence.*;

import java.time.*;

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

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_edited_at")
    private LocalDateTime lastEditedAt;

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