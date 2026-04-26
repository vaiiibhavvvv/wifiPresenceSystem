package com.presence.processing.service;

import com.presence.processing.model.PresenceEvent;
import com.presence.processing.repository.PresenceEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PresencePersistenceService {

    private final PresenceEventRepository presenceEventRepository;

    @Transactional
    public void save(PresenceEvent event) {
        try {
            presenceEventRepository.save(event);
        } catch (Exception ex) {
            log.error("Failed to persist event device={}: {}", event.getDeviceId(), ex.getMessage());
        }
    }
}