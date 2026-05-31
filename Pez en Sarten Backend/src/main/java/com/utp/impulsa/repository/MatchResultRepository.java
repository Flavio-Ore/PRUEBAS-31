package com.utp.impulsa.repository;

import com.utp.impulsa.model.MatchResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MatchResultRepository extends JpaRepository<MatchResult, UUID> {
    List<MatchResult> findByUserId(UUID userId);
}
