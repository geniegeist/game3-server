package com.example.game3server.infrastructure.messaging.consumption

import game3.events.client.command.AuthenticateCommand
import game3.events.client.command.ExecuteGameMoveCommand
import org.apache.avro.specific.SpecificRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.config.KafkaListenerEndpointRegistry
import org.springframework.stereotype.Component

interface ClientCommandKafkaListenerDelegate {
    fun didReceive(command: AuthenticateCommand) {}
    fun didReceive(command: ExecuteGameMoveCommand) {}
}

@Component
class ClientCommandKafkaListener(
    private val kafkaListenerEndpointRegistry: KafkaListenerEndpointRegistry,
) {

    var delegate: ClientCommandKafkaListenerDelegate? = null

    fun startListening() {
        kafkaListenerEndpointRegistry.getListenerContainer("client-command-listener-game-server")?.start()
    }

    @KafkaListener(
        id = "client-command-listener-game-server",
        topics = ["game3.events.client.command"],
        autoStartup = "false"
    )
    fun listen(data: SpecificRecord) {
        when (data) {
            is AuthenticateCommand -> delegate?.didReceive(data)
            is ExecuteGameMoveCommand -> delegate?.didReceive(data)
            else -> println("Unhandled event: $data")
        }
    }
}
