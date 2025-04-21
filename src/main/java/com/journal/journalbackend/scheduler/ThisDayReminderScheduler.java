package com.journal.journalbackend.scheduler;

import com.journal.journalbackend.messaging.producer.ReminderProducer;
import com.journal.journalbackend.model.User;
import com.journal.journalbackend.service.EntryService;
import com.journal.journalbackend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

@Component
public class ThisDayReminderScheduler {
    private static final Logger logger = LoggerFactory.getLogger(ThisDayReminderScheduler.class);
    private final UserService userService;
    private final EntryService entryService;
    private final ReminderProducer reminderProducer;

    public ThisDayReminderScheduler(UserService userService, EntryService entryService, ReminderProducer reminderProducer) {
        this.userService = userService;
        this.entryService = entryService;
        this.reminderProducer = reminderProducer;
    }

    @Scheduled(cron = "0 0 9 * * ?") // Runs every day at 9:00 AM UTC
    /*@Scheduled(cron = "0/30 * * * * ?")*/
    public void triggerMemoryLaneReminders() {
        logger.info("Starting 'Memory Lane' daily reminders");

        List<User> verifiedUsers = userService.getAllVerifiedUsers();

        verifiedUsers.parallelStream().forEach(user -> {
            try {
                ZoneId userZone = user.getTimezone();
                ZonedDateTime todayUserZone = ZonedDateTime.now(ZoneOffset.UTC).withZoneSameInstant(userZone);
                int day = todayUserZone.getDayOfMonth();

                for (int i = 1; i <= 11; i++) {
                    ZonedDateTime targetDate = todayUserZone.minusMonths(i);

                    if (day <= targetDate.toLocalDate().lengthOfMonth()) {
                        ZonedDateTime start = targetDate.withDayOfMonth(day).toLocalDate().atStartOfDay(userZone);
                        ZonedDateTime end = start.plusDays(1).minusNanos(1);

                        LocalDateTime startUtc = start.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
                        LocalDateTime endUtc = end.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();

                        long entryCount = entryService.getEntryCountForUserBetweenDates(user.getId(), startUtc, endUtc);

                        if (entryCount > 0) {
                            reminderProducer.sendMemoryLaneReminder(user, entryCount, start.toLocalDate());
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Error processing memory lane reminder for user {}: {}", user.getId(), e.getMessage());
            }
        });
    }
}

