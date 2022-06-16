package com.moondy.simplesseclient.controller;

import com.moondy.simplesseclient.domain.RandomCode;
import java.time.Duration;
import java.util.UUID;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class SimpleSseController {
    @GetMapping("/sse/locationCode")
    public Flux<ServerSentEvent<RandomCode>> stocks(@RequestParam("code") int code) {
        RandomCode randomCode = RandomCode.builder()
                                    .code(code)
                                    .name(randomValue())
                                    .build();
        return Flux.interval(Duration.ofSeconds(1))
                   .map(t -> RandomCode.builder()
                                 .code(code)
                                 .name(randomValue())
                                 .build())
                   .map(stock -> ServerSentEvent.builder(stock).build());
    }

    private String randomValue() {
        return String.valueOf(UUID.randomUUID());
    }
}
