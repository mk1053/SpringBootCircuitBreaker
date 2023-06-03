package com.circuitBreaker;


import com.circuitBreaker.controller.Resilience4jController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Duration;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = Resilience4jController.class)
@AutoConfigureWebTestClient
class Resilience4JControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @BeforeEach
    public void setUp() {
        webTestClient = webTestClient.mutate()
                .responseTimeout(Duration.ofMillis(30000))
                .build();
    }

    @RepeatedTest(10)
    void delayTest(RepetitionInfo repetitionInfo) {
        int delay = 1 + (repetitionInfo.getCurrentRepetition() % 2);
        webTestClient.get()
                .uri("/api/delay/{delay}", delay)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    void timeoutTest() {
        webTestClient.get()
                .uri("/api/timeout/{timeout}", 5)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    void errorTest_ok() {
        webTestClient.get()
                .uri("/api/error/{valid}", false)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    void errorTest_case_ko() {
        webTestClient.get()
                .uri("/api/error/{valid}", true)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus()
                .is5xxServerError();
    }
}
