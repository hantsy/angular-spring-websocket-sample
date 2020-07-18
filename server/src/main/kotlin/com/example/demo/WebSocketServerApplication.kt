package com.example.demo

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.data.mongodb.repository.Tailable
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration
import java.time.Instant

@SpringBootApplication
class WebSocketServerApplication {

    @Bean
    fun runner(template: ReactiveMongoTemplate) = CommandLineRunner {
        println("running CommandLineRunner...")
        template.executeCommand("{\"convertToCapped\": \"messages\", size: 100000}")
                .subscribe(::println);
    }

    @Bean
    fun webSocketMapping(mapper: ObjectMapper, messages: MessageRepository): HandlerMapping? {
        val map = mapOf("/ws/messages" to ChatSocketHandler(mapper, messages))
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

class ChatSocketHandler(val mapper: ObjectMapper, val messages: MessageRepository) : WebSocketHandler {

    override fun handle(session: WebSocketSession): Mono<Void> {
        println("handling WebSocketSession...")
        session.receive()
                .map { it.payloadAsText }
                .map { Message(body = it, sentAt = Instant.now()) }
                .flatMap { this.messages.save(it) }
                .subscribe({ println(it) }, { println(it) })

        return session.send(
                Mono.delay(Duration.ofMillis(1000))
                        .thenMany(this.messages.getMessagesBy())
                        .map { session.textMessage(toJson(it)) }
        ).then()

    }

    fun toJson(message: Message): String = mapper.writeValueAsString(message)

}

interface MessageRepository : ReactiveMongoRepository<Message, String> {
    @Tailable
    fun getMessagesBy(): Flux<Message>
}

@Document(collection = "messages")
data class Message @JsonCreator constructor(
        @JsonProperty("id") @Id var id: String? = null,
        @JsonProperty("body") var body: String,
        @JsonProperty("sentAt") var sentAt: Instant = Instant.now()
)
