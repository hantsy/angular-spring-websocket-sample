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
import reactor.core.publisher.Processors
import java.net.URI
import java.time.Duration


@SpringBootTest()
class WebSocketServerApplicationTests {

    lateinit var client: WebSocketClient

    @Autowired
    lateinit var mapper: ObjectMapper

    @BeforeEach
    fun setup() {
        this.client = ReactorNettyWebSocketClient()
    }

    // see:
    // https://stackoverflow.com/questions/49467913/spring-reactive-reactornettywebsocketclient-not-logging-anything
    // https://stackoverflow.com/questions/47065219/how-to-use-spring-reactive-websocket-and-transform-it-into-the-flux-stream
    // https://github.com/spring-projects/spring-framework/blob/master/spring-webflux/src/test/java/org/springframework/web/reactive/socket/WebSocketIntegrationTests.java
    @Test
    fun contextLoads() {
        val replay = Processors.replay<Message>(100)
        try {
            client.execute(
                    URI("ws://localhost:8080/ws/messages")
            ) { session: WebSocketSession ->

                session
                        .send(
                                Mono.delay(Duration.ofMillis(500)).thenMany(
                                        Flux.just("test message", "test message2")
                                                .map(session::textMessage)
                                )
                        )
                        .then(
                                session.receive()
                                        .map { mapper.readValue(it.payloadAsText, Message::class.java) }
                                        .log("received from server::")
                                        .subscribeWith(replay)
                                        .then()
                        )
            }.block(Duration.ofSeconds(5L))

            assertThat(replay.blockLast(Duration.ofSeconds(5L))?.body).isEqualTo("test message2")
        } catch (e: Exception) {
            println(e.localizedMessage)
        }

//        `as` { StepVerifier.create(it) }
//                .consumeNextWith { it -> assertThat(it.body).isEqualTo("test message") }
//                .consumeNextWith { it -> assertThat(it.body).isEqualTo("test message2") }
//                .verifyComplete()
    }
}

