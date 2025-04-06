package com.journal.journalbackend.dto.response;

import java.time.LocalDateTime;

public class MediaResponse {
        private Long id;
        private String filename;
        private String fileType;
        private Long fileSize;
        private String description;
        private LocalDateTime uploadDate;
        private String url;

        // Getters and setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
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

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
