package com.journal.journalbackend.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
    @Table(name = "media")
    public class Media {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "entry_id", nullable = false)
        private Entry entry;

        @Column(nullable = false)
        private String filename;

        @Column(nullable = false)
        private String originalFilename;

        @Column(nullable = false)
        private String fileType;

        @Column(nullable = false)
        private Long fileSize;

        private String description;

        @Column(nullable = false)
        private LocalDateTime uploadDate;

        // Getters and setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Entry getEntry() {
            return entry;
        }

        public void setEntry(Entry entry) {
            this.entry = entry;
        }

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public String getOriginalFilename() {
            return originalFilename;
        }

        public void setOriginalFilename(String originalFilename) {
            this.originalFilename = originalFilename;
        }

        public String getFileType() {
            return fileType;
        }

        public void setFileType(String fileType) {
            this.fileType = fileType;
        }

        public Long getFileSize() {
            return fileSize;
        }

        public void setFileSize(Long fileSize) {
            this.fileSize = fileSize;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public LocalDateTime getUploadDate() {
            return uploadDate;
        }

        public void setUploadDate(LocalDateTime uploadDate) {
            this.uploadDate = uploadDate;
        }
    }

