package com.example.game3server.infrastructure.messaging.production

import com.example.game3server.application.messaging.ServerCommandSendable
import game3.events.server.command.ClearClientViewCommand
import game3.events.server.command.PrintMessageToClientCommand
import game3.events.server.command.ViableGameActionsCommand
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.springframework.stereotype.Component
import java.util.concurrent.Future

@Component
class ServerCommandKafkaSender(
    private val kafkaProducer: KafkaProducer<String, SpecificRecord>
) : ServerCommandSendable {

   companion object {
       private const val TOPIC = "game3.events.server.command"
   }

    override fun send(printMessageToClientCommand: PrintMessageToClientCommand): Future<RecordMetadata> = kafkaProducer.send(
        ProducerRecord(TOPIC, printMessageToClientCommand.playerId, printMessageToClientCommand)
    )

    override fun send(clearCommand: ClearClientViewCommand): Future<RecordMetadata> = kafkaProducer.send(
        ProducerRecord(TOPIC, clearCommand.playerId, clearCommand)
    )

    override fun send(command: ViableGameActionsCommand): Future<RecordMetadata> = kafkaProducer.send(
        ProducerRecord(TOPIC, command.playerId, command)
    )
}