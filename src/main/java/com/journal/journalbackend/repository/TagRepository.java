package com.journal.journalbackend.repository;

import com.journal.journalbackend.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    boolean existsByNameAndUserId(String name, Long userId);
    List<Tag> findByUserId(Long userId);
    List<Tag> findByUserIdAndNameContainingIgnoreCase(Long userId, String nameQuery);
}
