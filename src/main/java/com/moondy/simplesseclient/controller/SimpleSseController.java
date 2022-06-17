package com.moondy.simplesseclient.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moondy.simplesseclient.domain.RandomCode;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/sse")
public class SimpleSseController {
    private SseEmitter emitter;
    private static final String[] WORDS = "The quick brown fox jumps over the lazy dog.".split(" ");
    private final ExecutorService cachedThreadPool = Executors.newCachedThreadPool();

    @GetMapping("/locationCode")
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

    @GetMapping(path = "/words", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter getWords() {
        SseEmitter emitter = new SseEmitter();
        cachedThreadPool.execute(() -> {
            try {

                for (int i = 0; i < WORDS.length; i++) {
                    RandomCode randomCode = RandomCode.builder()
                                                .code(i)
                                                .name(WORDS[i])
                                                .build();
                    ObjectMapper objMapper = new ObjectMapper();
                    String json = objMapper.writeValueAsString(randomCode);
                    emitter.send(json);
                    TimeUnit.SECONDS.sleep(1);
//                    if (i ==3)
//                        throw new RuntimeException("예외처리");
                }
                RandomCode randomCode = RandomCode.builder()
                                            .code(-1)
                                            .name("END")
                                            .build();
                emitter.send(randomCode);
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }

    @GetMapping("/srb")
    public ResponseEntity<StreamingResponseBody> handleRbe() {
        StreamingResponseBody stream = out -> {
            for (int i = 0; i < WORDS.length; i++) {
                String msg = WORDS[i] + " ";
                out.write(msg.getBytes());
            }
        };
        return new ResponseEntity(stream, HttpStatus.OK);
    }

    private String randomValue() {
        return String.valueOf(UUID.randomUUID());
    }



}
