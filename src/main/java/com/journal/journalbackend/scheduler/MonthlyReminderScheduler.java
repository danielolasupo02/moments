package com.journal.journalbackend.scheduler;

import com.journal.journalbackend.messaging.producer.ReminderProducer;
import com.journal.journalbackend.model.User;
import com.journal.journalbackend.service.EntryService;
import com.journal.journalbackend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.*;
import java.util.List;

@Component
public class MonthlyReminderScheduler {
    private static final Logger logger = LoggerFactory.getLogger(MonthlyReminderScheduler.class);
    private UserService userService;
    private EntryService entryService;
    private ReminderProducer reminderProducer;

    public MonthlyReminderScheduler(UserService userService, EntryService entryService, ReminderProducer reminderProducer) {
        this.userService = userService;
        this.entryService = entryService;
        this.reminderProducer = reminderProducer;
    }


    @Scheduled(cron = "0 0 9 1 * ?") // Runs at 9:00 AM UTC on the 1st of every month
    public void triggerMonthlyReflections() {
        logger.info("Starting monthly reflection reminders");

        List<User> verifiedUsers = userService.getAllVerifiedUsers();

        verifiedUsers.parallelStream().forEach(user -> {
            try {
                ZoneId userZone = user.getTimezone();
                ZonedDateTime nowInUserZone = ZonedDateTime.now(ZoneOffset.UTC).withZoneSameInstant(userZone);

                // Get first and last moment of previous month in user's timezone
                YearMonth prevYearMonth = YearMonth.from(nowInUserZone.minusMonths(1));
                ZonedDateTime start = prevYearMonth.atDay(1).atStartOfDay(userZone);
                ZonedDateTime end = prevYearMonth.atEndOfMonth()
                        .atTime(23, 59, 59, 999999999)
                        .atZone(userZone);

                // Convert to UTC for database query
                LocalDateTime startUtc = start.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
                LocalDateTime endUtc = end.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();

                long entryCount = entryService.getEntryCountForUserBetweenDates(
                        user.getId(),
                        startUtc,
                        endUtc
                );

                if (entryCount > 0) {
                    // Convert YearMonth to LocalDate for message
                    LocalDate monthYearDate = prevYearMonth.atDay(1);
                    reminderProducer.sendMonthlyReminder(user, entryCount, monthYearDate);
                }
            } catch (Exception e) {
                logger.error("Error processing user {}: {}", user.getId(), e.getMessage());
            }
        });
    }
}
