package com.journal.journalbackend;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
@EnableScheduling
@EnableRabbit
public class JournalbackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(JournalbackendApplication.class, args);
		BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
		String bcrypt = bCryptPasswordEncoder.encode("Daniel2000.");
		System.out.println(bcrypt);
	}

}
