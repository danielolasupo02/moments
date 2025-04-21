package com.journal.journalbackend.messaging.consumer;

import com.journal.journalbackend.config.EmailConfig;
import com.journal.journalbackend.config.RabbitConfig;
import com.journal.journalbackend.messaging.ReminderMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

// ReminderConsumer.java
@Component
public class ReminderConsumer {

    private final EmailConfig emailService;

    public ReminderConsumer(EmailConfig emailService) {
        this.emailService = emailService;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE_MONTHLY_REMINDERS)
    public void processMonthlyReminder(ReminderMessage message) {
        emailService.sendMonthlySummary(
                message.getEmail(),
                message.getEntryCount(),
                message.getMonthYear()
        );
    }

    @RabbitListener(queues = RabbitConfig.QUEUE_MEMORY_LANE_REMINDERS)
    public void processMemoryLaneReminder(ReminderMessage message) {
        emailService.sendMemoryLaneEmail(
                message.getEmail(),
                message.getMonthYear(),
                message.getEntryCount()

        );
    }

}