package com.journal.journalbackend.messaging;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;

public class ReminderMessage {
    private Long userId;
    private String email;
    private long entryCount;
    private String timezone;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate monthYear;

    // Required for JSON deserialization
    public ReminderMessage() {}


    // Constructor using LocalDate
    public ReminderMessage(Long userId, String email, long entryCount,
                           LocalDate monthYear, String timezone) {
        this.userId = userId;
        this.email = email;
        this.entryCount = entryCount;
        this.monthYear = monthYear;
        this.timezone = timezone;
    }

    // Getters and Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public long getEntryCount() { return entryCount; }
    public void setEntryCount(long entryCount) { this.entryCount = entryCount; }

    public LocalDate getMonthYear() { return monthYear; }
    public void setMonthYear(LocalDate monthYear) { this.monthYear = monthYear; }

    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }


    @Override
    public String toString() {
        return "ReminderMessage{" +
                "userId=" + userId +
                ", email='" + email + '\'' +
                ", entryCount=" + entryCount +
                ", monthYear=" + monthYear +
                ", timezone='" + timezone + '\'' +
                '}';
    }
}
