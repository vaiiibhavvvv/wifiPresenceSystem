package com.presence.ingestion;

import com.presence.ingestion.model.RssiEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

@SpringBootApplication
public class IngestionServiceApplication {

   public static void main(String[] args){
       SpringApplication.run(IngestionServiceApplication.class,args);
   }

}
