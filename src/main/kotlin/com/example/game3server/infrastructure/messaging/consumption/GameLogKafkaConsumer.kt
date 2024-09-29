package com.example.game3server.infrastructure.messaging.consumption

import com.example.game3server.application.messaging.GameLogConsumable
import com.example.game3server.infrastructure.messaging.config.MessagingConfig.Companion.kafkaConsumerProps
import jakarta.annotation.PreDestroy
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class GameLogKafkaConsumer() : GameLogConsumable {

    companion object {
        private const val GROUP_ID = "game-snapshot-log-consumer"
        private const val TOPIC = "game3.events.game.log"
        private const val POLL_INTERVAL = 100L
    }

    private var kafkaConsumer: KafkaConsumer<String, SpecificRecord> = KafkaConsumer<String, SpecificRecord>(kafkaConsumerProps(
        GROUP_ID))

    init {
        kafkaConsumer.subscribe(listOf(TOPIC))
    }

    override fun fetch(offset: Int): ConsumerRecords<String, SpecificRecord> = kafkaConsumer.poll(Duration.ofMillis(POLL_INTERVAL))

    @PreDestroy
    fun cleanup() {
        kafkaConsumer.close()
    }
}