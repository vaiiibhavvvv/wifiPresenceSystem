package com.presence.ingestion.controller;


import com.presence.ingestion.model.RssiEvent;
import com.presence.ingestion.service.RssiIngestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Slf4j
@RestController
@RequestMapping("/ingest")
@RequiredArgsConstructor
public class IngestionController {



    private RssiIngestionService ingestionService;

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Mono<ResponseEntity<Map<String, String>>> ingest(
            @Valid @RequestBody RssiEvent event) {
       // log.info("Received: device={} rssi={}", event.getDeviceId(), event.getRssi());
        return ingestionService.ingest(event)
                .thenReturn(ResponseEntity.accepted()
                        .body(Map.of("status", "accepted", "deviceId", event.getDeviceId().toString())));
    }

    @PostMapping("/batch")
    public Mono<ResponseEntity<Map<String,Object>>> ingestBatch(List<@Valid RssiEvent> events){
        if(events.size() > 500){
            return Mono.just(ResponseEntity.badRequest()
                    .body(Map.of("Error","Batch size must be <= 500")));
        }
        return ingestionService.ingestBatch(events)
                .thenReturn(ResponseEntity.accepted()
                        .body(Map.of("error","accepted","count",events.size())));
    }

    public Mono<Map<String,String>> ping(){
        return Mono.just(Map.of("Status","UP","Service","ingestion-service"));

    }

}
