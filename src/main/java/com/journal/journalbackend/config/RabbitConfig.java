package com.journal.journalbackend.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    public static final String QUEUE_MONTHLY_REMINDERS = "monthly-reminders-queue";
    public static final String QUEUE_MEMORY_LANE_REMINDERS = "memory-lane-reminders";
    public static final String QUEUE_ANNIVERSARY_REMINDERS = "anniversary-reminders";

    // Declare the Monthly Reminders Queue
    @Bean
    public Queue monthlyRemindersQueue() {return new Queue(QUEUE_MONTHLY_REMINDERS, true); // durable = true
    }

    // Declare the Anniversary Reminders Queue
    @Bean
    public Queue anniversaryRemindersQueue() { return new Queue(QUEUE_ANNIVERSARY_REMINDERS, true);}

    // Declare the Memory Lane (On This Day) Reminders Queue
    @Bean
    public Queue memoryLaneRemindersQueue() {
        return new Queue(QUEUE_MEMORY_LANE_REMINDERS, true); // durable = true
    }





    // JSON converter with JavaTime support
    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2JsonMessageConverter(mapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
