package com.presence.processing.consumer;

import com.presence.processing.model.PresenceEvent;
import com.presence.processing.model.RssiEvent;
import com.presence.processing.service.PresencePersistenceService;
import com.presence.processing.service.PresenceStateService;
import com.presence.processing.util.SignalProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class RssiKafkaConsumer {

    private final SignalProcessor            signalProcessor;
    private final PresenceStateService       presenceStateService;
    private final PresencePersistenceService persistenceService;

    @KafkaListener(
            topics           = "${app.kafka.topic.rssi-events:rssi-events}",
            groupId          = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory",
            concurrency      = "3"
    )
    public void consume(
            @Payload RssiEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        try {
            log.debug("Consuming partition={} offset={} device={}", partition, offset, event.getDeviceId());

            SignalProcessor.ProcessingResult result = signalProcessor.process(event.getDeviceId(), event.getRssi());
            presenceStateService.updateState(event.getDeviceId(), result.movementDetected(),
                    result.smoothedRssi(), result.variance());

            var presenceEvent = PresenceEvent.builder()
                    .deviceId(event.getDeviceId())
                    .status(presenceStateService.getCurrentStatus(event.getDeviceId()))
                    .movement(result.movementDetected())
                    .variance(result.variance())
                    .avgRssi(result.average())
                    .detectedAt(event.getTimestamp() != null ? event.getTimestamp() : Instant.now().getEpochSecond())
                    .build();

            persistenceService.save(presenceEvent);

        } catch (Exception ex) {
            log.error("Error processing device={}: {}", event.getDeviceId(), ex.getMessage(), ex);
        }
    }
}