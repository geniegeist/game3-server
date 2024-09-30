package com.example.game3server.infrastructure.messaging.consumption

import com.example.game3server.application.messaging.GameLogConsumable
import com.example.game3server.infrastructure.messaging.config.MessagingConfig
import jakarta.annotation.PreDestroy
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class GameLogKafkaConsumer(
    private val messagingConfig: MessagingConfig
) : GameLogConsumable {

    companion object {
        private const val GROUP_ID = "game-snapshot-log-consumer"
        private const val TOPIC = "game3.events.game.log"
        private const val POLL_INTERVAL = 100L
    }

    private var kafkaConsumer: KafkaConsumer<String, SpecificRecord> = KafkaConsumer<String, SpecificRecord>(this.messagingConfig.kafkaConsumerProps(
        GROUP_ID, enableAutoCommit = false))

    init {
        kafkaConsumer.subscribe(listOf(TOPIC))
    }

    override fun fetchFromBeginning(): ConsumerRecords<String, SpecificRecord> = kafkaConsumer.poll(Duration.ofMillis(POLL_INTERVAL))

    override fun commitSync(offset: Long) {
        val topicPartitionToOffset = kafkaConsumer.assignment()
            .map { it to OffsetAndMetadata(offset) }
            .associateBy({ it.first }, { it.second })
        kafkaConsumer.commitSync(topicPartitionToOffset)
    }

    @PreDestroy
    fun cleanup() {
        kafkaConsumer.close()
    }
}