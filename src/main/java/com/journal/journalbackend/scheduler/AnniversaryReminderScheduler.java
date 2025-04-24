package com.journal.journalbackend.scheduler;

import com.journal.journalbackend.messaging.producer.ReminderProducer;
import com.journal.journalbackend.model.User;
import com.journal.journalbackend.service.EntryService;
import com.journal.journalbackend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class AnniversaryReminderScheduler {

    private static final Logger logger = LoggerFactory.getLogger(AnniversaryReminderScheduler.class);
    private final UserService userService;
    private final EntryService entryService;
    private final ReminderProducer reminderProducer;

    public AnniversaryReminderScheduler(UserService userService, EntryService entryService, ReminderProducer reminderProducer) {
        this.userService = userService;
        this.entryService = entryService;
        this.reminderProducer = reminderProducer;
    }

    @Scheduled(cron = "0 0 9 * * ?") // Runs every day at 9:00 AM UTC
    /* @Scheduled(cron = "0/30 * * * * ?") /*Tests trigger method every 30 seconds*/
    public void triggerAnniversaryReminders() {
        logger.info("Checking for journal anniversaries...");

        List<User> users = userService.getAllVerifiedUsers();

        users.parallelStream().forEach(user -> {
            try {
                ZoneId userZone = user.getTimezone();
                ZonedDateTime nowUserTime = ZonedDateTime.now(ZoneOffset.UTC).withZoneSameInstant(userZone);
                LocalDate todayDate = nowUserTime.toLocalDate();

                List<LocalDate> pastEntryDates = entryService.getDistinctEntryDates();

                pastEntryDates.forEach(entryDate -> {
                    long yearsAgo = ChronoUnit.YEARS.between(entryDate, todayDate);
                    if (yearsAgo >= 1 && entryDate.getMonthValue() == todayDate.getMonthValue() && entryDate.getDayOfMonth() == todayDate.getDayOfMonth()) {
                        reminderProducer.sendAnniversaryReminder(user, yearsAgo, entryDate);
                    }
                });

            } catch (Exception e) {
                logger.error("Error sending anniversary reminders for user {}: {}", user.getId(), e.getMessage());
            }
        });
    }
}

