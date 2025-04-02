package com.journal.journalbackend.repository;

import com.journal.journalbackend.model.Entry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EntryRepository extends JpaRepository<Entry, Long> {
    List<Entry> findByJournalId(Long journalId);
    Page<Entry> findByJournalId(Long journalId, Pageable pageable);
    Optional<Entry> findByIdAndJournalId(Long id, Long journalId);
    List<Entry> findByJournalIdAndEntryDate(Long journalId, LocalDate entryDate);
    boolean existsByIdAndJournalId(Long id, Long journalId);
}

