package com.utp.impulsa.repository;

import com.utp.impulsa.model.CvData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CvDataRepository extends JpaRepository<CvData, UUID> {
}
