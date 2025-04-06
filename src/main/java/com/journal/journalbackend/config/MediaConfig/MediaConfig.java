package com.journal.journalbackend.config.MediaConfig;


import com.journal.journalbackend.repository.EntryRepository;
import com.journal.journalbackend.repository.MediaRepository;
import com.journal.journalbackend.service.MediaService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MediaConfig {
     @Bean
     public MediaService mediaService(
                MediaRepository mediaRepository,
                EntryRepository entryRepository,
                String uploadDir) {
            return new MediaService(mediaRepository, entryRepository, uploadDir);
        }

        @Bean
        public String uploadDir() {
            return "uploads";
        }
}
