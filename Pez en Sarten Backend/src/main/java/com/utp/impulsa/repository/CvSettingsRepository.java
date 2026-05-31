package com.utp.impulsa.repository;

import com.utp.impulsa.model.CvSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CvSettingsRepository extends JpaRepository<CvSettings, UUID> {
}
