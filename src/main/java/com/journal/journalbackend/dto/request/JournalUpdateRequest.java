package com.journal.journalbackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class JournalUpdateRequest {
    @NotBlank(message = "Journal title cannot be blank")
    @Size(max = 100, message = "Journal title cannot exceed 100 characters")
    private String title;

    // Getters and setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
