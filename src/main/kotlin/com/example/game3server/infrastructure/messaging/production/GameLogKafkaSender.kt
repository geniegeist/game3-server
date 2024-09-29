package com.example.game3server.infrastructure.messaging.production

import com.example.game3server.application.messaging.GameLogSendable
import game3.events.game.log.DecMoveExecuted
import game3.events.game.log.GameEnded
import game3.events.game.log.GameInitialized
import game3.events.game.log.GameJoined
import game3.events.game.log.IncMoveExecuted
import game3.events.game.log.NoOpMoveExecuted
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.springframework.stereotype.Component
import java.util.concurrent.Future

@Component
class GameLogKafkaSender(
    private val kafkaProducer: KafkaProducer<String, SpecificRecord>
) : GameLogSendable {

    companion object {
        const val TOPIC = "game3.events.game.log"
    }

    override fun send(log: GameInitialized): Future<RecordMetadata> = kafkaProducer.send(ProducerRecord(TOPIC, log.gameId, log))

    override fun send(log: GameEnded): Future<RecordMetadata> = kafkaProducer.send(ProducerRecord(TOPIC, log.gameId, log))

    override fun send(log: GameJoined): Future<RecordMetadata> = kafkaProducer.send(ProducerRecord(TOPIC, log.gameId, log))

    override fun send(log: IncMoveExecuted): Future<RecordMetadata> = kafkaProducer.send(ProducerRecord(TOPIC, log.gameId, log))

    override fun send(log: DecMoveExecuted): Future<RecordMetadata> = kafkaProducer.send(ProducerRecord(TOPIC, log.gameId, log))

    override fun send(log: NoOpMoveExecuted): Future<RecordMetadata> = kafkaProducer.send(ProducerRecord(TOPIC, log.gameId, log))
}