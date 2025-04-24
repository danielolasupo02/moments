package com.journal.journalbackend.repository;

import com.journal.journalbackend.model.Entry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EntryRepository extends JpaRepository<Entry, Long> {
    List<Entry> findByJournalId(Long journalId);
    Page<Entry> findByJournalId(Long journalId, Pageable pageable);
    Optional<Entry> findByIdAndJournalId(Long id, Long journalId);
    List<Entry> findByJournalIdAndEntryDate(Long journalId, LocalDate entryDate);
    boolean existsByIdAndJournalId(Long id, Long journalId);


    @Query("SELECT COUNT(e) FROM Entry e JOIN e.journal j WHERE j.user.id = :userId AND e.createdAt BETWEEN :start AND :end")
    long countByUserAndDateRange(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT DISTINCT e.entryDate FROM Entry e")
    List<LocalDate> findDistinctEntryDates();


}

