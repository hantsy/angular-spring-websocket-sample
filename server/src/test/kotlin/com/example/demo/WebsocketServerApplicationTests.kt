package com.example.demo

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.web.reactive.socket.WebSocketSession
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient
import org.springframework.web.reactive.socket.client.WebSocketClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import reactor.test.StepVerifier
import java.net.URI
import java.time.Duration


@SpringBootTest()
class WebsocketServerApplicationTests {

    lateinit var client: WebSocketClient;

    @Autowired
    lateinit var mapper: ObjectMapper;

    @BeforeEach
    fun setup() {
        this.client = ReactorNettyWebSocketClient()
    }

    @Test
    fun contextLoads() {
        val replay = Sinks.replay<Message>(10)

        client.execute(
                URI("ws://localhost:8080/ws/messages")
        ) { session: WebSocketSession ->
            println("Starting to send messages")
            session.receive()
                    .map { mapper.readValue(it.payloadAsText, Message::class.java) }
                    .subscribe { replay.next(it) }

            session.send(
                    Mono.delay(Duration.ofSeconds(100)).thenMany(
                            Flux.just("test message", "test message2")
                                    .map(session::textMessage)
                    )
            ).then()
        }.subscribe()

        StepVerifier.create(replay.asFlux().takeLast(2))
                .consumeNextWith { it -> assertThat(it.body).isEqualTo("test message") }
                .consumeNextWith { it -> assertThat(it.body).isEqualTo("test message2") }
                .verifyComplete()
    }
}

