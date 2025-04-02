package com.journal.journalbackend.repository;

import com.journal.journalbackend.model.Journal;
import com.journal.journalbackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JournalRepository extends JpaRepository<Journal, Long> {
    List<Journal> findByUserId(Long userId);
    boolean existsByIdAndUserId(Long id, Long userId);
    List<Journal> findByUser(User user);
}
