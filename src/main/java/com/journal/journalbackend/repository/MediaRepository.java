package com.journal.journalbackend.repository;

import com.journal.journalbackend.model.Media;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MediaRepository extends JpaRepository<Media, Long> {
    List<Media> findByEntryId(Long entryId);
    Optional<Media> findByIdAndEntryId(Long id, Long entryId);
}
