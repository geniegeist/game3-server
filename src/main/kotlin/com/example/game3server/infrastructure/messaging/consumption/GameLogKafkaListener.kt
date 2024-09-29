package com.example.game3server.infrastructure.messaging.consumption

import game3.events.game.log.DecMoveExecuted
import game3.events.game.log.GameEnded
import game3.events.game.log.GameInitialized
import game3.events.game.log.GameJoined
import game3.events.game.log.IncMoveExecuted
import game3.events.game.log.NoOpMoveExecuted
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.common.TopicPartition
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.config.KafkaListenerEndpointRegistry
import org.springframework.kafka.listener.ConsumerSeekAware
import org.springframework.kafka.listener.ConsumerSeekAware.ConsumerSeekCallback
import org.springframework.stereotype.Component

interface GameLogKafkaListenerDelegate {
    fun didReceive(event: GameInitialized) {}
    fun didReceive(event: GameEnded) {}
    fun didReceive(event: GameJoined) {}
    fun didReceive(event: IncMoveExecuted) {}
    fun didReceive(event: DecMoveExecuted) {}
    fun didReceive(event: NoOpMoveExecuted) {}
}

@Component
class GameLogKafkaListener(
    private val kafkaListenerEndpointRegistry: KafkaListenerEndpointRegistry,
) : ConsumerSeekAware {

    var delegate: GameLogKafkaListenerDelegate? = null
    private var offset = 0L

    fun startListening(offset: Long) {
        this.offset = offset
        kafkaListenerEndpointRegistry.getListenerContainer("game-log-listener-game-server")?.start()
    }

    @KafkaListener(
        id = "game-log-listener-game-server",
        topics = ["game3.events.game.log"],
        autoStartup = "false"
    )
    fun listen(data: SpecificRecord) {
        when (data) {
            is GameInitialized -> delegate?.didReceive(data)
            is GameEnded -> delegate?.didReceive(data)
            is GameJoined -> delegate?.didReceive(data)
            is NoOpMoveExecuted -> delegate?.didReceive(data)
            is IncMoveExecuted -> delegate?.didReceive(data)
            is DecMoveExecuted -> delegate?.didReceive(data)
        }
    }

    // ConsumerSeekAware implementation
    override fun onPartitionsAssigned(
        assignments: MutableMap<TopicPartition, Long>,
        callback: ConsumerSeekCallback
    ) {
        for (topicPartition in assignments.keys) {
            callback.seek(topicPartition.topic(), topicPartition.partition(), offset)
        }
    }
}