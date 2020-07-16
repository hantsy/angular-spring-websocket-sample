package com.example.demo

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import java.time.Duration
import java.time.Instant
import java.util.*

@SpringBootApplication
class WebSocketServerApplication {

    @Bean
    fun webSocketMapping(mapper: ObjectMapper): HandlerMapping? {
        val map = mapOf("/ws/messages" to ChatSocketHandler(mapper))
        val simpleUrlHandlerMapping = SimpleUrlHandlerMapping().apply {
            urlMap = map
            order = 10
        }
        return simpleUrlHandlerMapping
    }

    @Bean
    fun handlerAdapter(): WebSocketHandlerAdapter = WebSocketHandlerAdapter()
}

fun main(args: Array<String>) {
    runApplication<WebSocketServerApplication>(*args)
}

class ChatSocketHandler(val mapper: ObjectMapper) : WebSocketHandler {
    val sink = Sinks.replay<Message>(100);
    val outputMessages: Flux<Message> = sink.asFlux();

    override fun handle(session: WebSocketSession): Mono<Void> {
        println("handling WebSocketSession...")
        session.receive()
                .map { it.payloadAsText }
                .map { Message(id= UUID.randomUUID().toString(), body = it, sentAt = Instant.now()) }
                .doOnNext { println(it) }
                .subscribe(
                        { message: Message -> sink.next(message) },
                        { error: Throwable -> sink.error(error) }
                );

        return session.send(
                Mono.delay(Duration.ofMillis(100))
                        .thenMany(outputMessages.map { session.textMessage(toJson(it)) })

        )

    }

    fun toJson(message: Message): String = mapper.writeValueAsString(message)

}

data class Message @JsonCreator constructor(
        @JsonProperty("id") var id: String? = null,
        @JsonProperty("body") var body: String,
        @JsonProperty("sentAt") var sentAt: Instant = Instant.now()
)
