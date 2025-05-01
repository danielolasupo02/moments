package com.journal.journalbackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TagRequest {
    @NotBlank(message = "Tag name is required")
    @Size(max = 50, message = "Tag name cannot exceed 50 characters")
    private String name;

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
