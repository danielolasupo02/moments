package com.journal.journalbackend.repository;

import com.journal.journalbackend.model.EntryVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EntryVersionRepository extends JpaRepository<EntryVersion, Long> {
    List<EntryVersion> findByEntryId(Long entryId);

    @Query("SELECT v FROM EntryVersion v WHERE v.deletedAt IS NOT NULL AND v.deletedAt < :cutoff")
    List<EntryVersion> findSoftDeletedBefore(LocalDateTime cutoff);

    Optional<EntryVersion> findByIdAndEntryId(Long id, Long entryId);
}
