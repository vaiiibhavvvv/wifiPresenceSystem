package com.presence.processing.repository;
import com.presence.processing.model.RssiReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface RssiReadingRepository extends JpaRepository<RssiReading, UUID> {}