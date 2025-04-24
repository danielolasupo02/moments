package com.journal.journalbackend.messaging.producer;

import com.journal.journalbackend.config.RabbitConfig;
import com.journal.journalbackend.messaging.ReminderMessage;
import com.journal.journalbackend.model.User;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class ReminderProducer {
    private final RabbitTemplate rabbitTemplate;

    public ReminderProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendMonthlyReminder(User user, long entryCount, LocalDate monthYear) {
        ReminderMessage message = new ReminderMessage(
                user.getId(),
                user.getEmail(),
                entryCount,
                monthYear,
                user.getTimezone().getId()
        );

        rabbitTemplate.convertAndSend(
                RabbitConfig.QUEUE_MONTHLY_REMINDERS,
                message
        );
    }

    public void sendMemoryLaneReminder(User user, long entryCount, LocalDate originalDate) {
        ReminderMessage message = new ReminderMessage(
                user.getId(),
                user.getEmail(),
                entryCount,
                originalDate,
                user.getTimezone().getId()
        );

        rabbitTemplate.convertAndSend(
                RabbitConfig.QUEUE_MEMORY_LANE_REMINDERS,
                message
        );
    }

    public void sendAnniversaryReminder(User user, long yearsAgo, LocalDate originalDate) {
        ReminderMessage message = new ReminderMessage(
                user.getId(),
                user.getEmail(),
                yearsAgo,
                originalDate,
                user.getTimezone().getId()
        );

        rabbitTemplate.convertAndSend(
                RabbitConfig.QUEUE_ANNIVERSARY_REMINDERS,
                message
        );
    }


}
