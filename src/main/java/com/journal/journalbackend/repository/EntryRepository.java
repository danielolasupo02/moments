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
    // Only non-deleted entries
    List<Entry> findByJournalIdAndDeletedAtIsNull(Long journalId);
    Page<Entry> findByJournalIdAndDeletedAtIsNull(Long journalId, Pageable pageable);
    Optional<Entry> findByIdAndJournalIdAndDeletedAtIsNull(Long id, Long journalId);
    List<Entry> findByJournalIdAndEntryDateAndDeletedAtIsNull(Long journalId, LocalDate entryDate);
    boolean existsByIdAndJournalIdAndDeletedAtIsNull(Long id, Long journalId);

    // Include deleted entries (for admin/recycle bin views)
    List<Entry> findByJournalIdAndDeletedAtIsNotNull(Long journalId);
    List<Entry> findByJournalIdAndEntryDate(Long journalId, LocalDate entryDate);
    boolean existsByIdAndJournalId(Long id, Long journalId);



    // Count only non-deleted entries
    @Query("SELECT COUNT(e) FROM Entry e JOIN e.journal j WHERE j.user.id = :userId AND e.deletedAt IS NULL AND e.createdAt BETWEEN :start AND :end")
    long countByUserAndDateRange(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT DISTINCT e.entryDate FROM Entry e")
    List<LocalDate> findDistinctEntryDates();

    // Find including soft-deleted entries
    @Query("SELECT e FROM Entry e WHERE e.id = :id AND e.journal.id = :journalId")
    Optional<Entry> findByIdAndJournalIdIncludeDeleted(
            @Param("id") Long id,
            @Param("journalId") Long journalId);

    // Find soft-deleted entries with no versions
    @Query("SELECT e FROM Entry e WHERE e.deletedAt IS NOT NULL AND " +
            "NOT EXISTS (SELECT v FROM EntryVersion v WHERE v.entry = e)")
    List<Entry> findSoftDeletedWithNoVersions();


}

