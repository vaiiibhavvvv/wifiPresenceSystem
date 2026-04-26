package com.presence.ingestion.service;


import com.presence.ingestion.model.RssiEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
//@RequiredArgsConstructor
public class RssiIngestionService {

    private final KafkaTemplate<String, RssiEvent> kafkaTemplate;

    public RssiIngestionService(KafkaTemplate<String, RssiEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Value("${app.kafka.topic.rssi-events}")
    private String rssiEventTopic;

    public Mono<Void> ingest(RssiEvent event){
        return Mono.fromCallable(() -> {
            event.setReceivedAt(Instant.now().toEpochMilli());
            // deviceId as key -> same device always goes to same partition -> ordered
            kafkaTemplate.send(rssiEventTopic, String.valueOf(event.getDeviceId()),event)
                    .whenComplete((result,ex) -> {
//                        if(ex != null){
//                         //   log.error("Failed to publish: device = {} error = {}",event.getDeviceId(),ex.getMessage());
//                        }
//                        else{
//                            log.debug("Published : device = {} rssi={}", event.getDeviceId(),event.getRssi());
//                        }
                    });
            return null;
        }).then();
    }

    public Mono<Void> ingestBatch(List<RssiEvent> events){
        return Mono.fromRunnable(() -> events.forEach(e-> ingest(e).subscribe()));
    }
}
