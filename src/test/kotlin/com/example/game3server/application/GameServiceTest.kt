package com.example.game3server.application

import com.example.game3server.BaseIntegrationTest
import com.example.game3server.application.messaging.GameLogConsumable
import com.example.game3server.application.persistence.GameSnapshot
import com.example.game3server.application.persistence.GameSnapshotRepository
import com.example.game3server.domain.Game
import com.example.game3server.infrastructure.messaging.consumption.ClientCommandKafkaListener
import com.example.game3server.infrastructure.messaging.consumption.GameLogKafkaListener
import com.example.game3server.infrastructure.messaging.production.GameLogKafkaSender
import com.example.game3server.infrastructure.messaging.production.ServerCommandKafkaSender
import com.ninjasquad.springmockk.MockkBean
import game3.events.client.command.AuthenticateCommand
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig
import io.mockk.every
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.MockitoAnnotations
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import org.springframework.test.annotation.DirtiesContext
import scala.reflect.internal.TreeInfo.Applied
import java.util.*

internal class GameServiceTest(
    @Value("\${kafka.bootstrapServers}") private val bootstrapServers: String,
    @Value("\${kafka.schemaRegistryUrl}") private val schemaRegistryUrl: String
) : BaseIntegrationTest() {

    // @MockkBean(relaxed = true)
    // lateinit var gameSnapshotRepository: GameSnapshotRepository<Long>

    // @MockkBean
    // lateinit var publisher: ApplicationEventPublisher

    // @MockkBean
    // lateinit var gameLogConsumable: GameLogConsumable

    // //@MockkBean(relaxed = true)
    // //lateinit var gameLogListener: GameLogKafkaListener

    // //@MockkBean(relaxed = true)
    // lateinit var clientCommandListener: ClientCommandKafkaListener

    // @MockkBean
    // lateinit var serverCommandSender: ServerCommandKafkaSender

    // @MockkBean
    // lateinit var gameLogSender: GameLogKafkaSender

    @Autowired
    lateinit var gameService: GameService

    // Support val
    private val clientCommandProducer = KafkaProducer<String, SpecificRecord>(kafkaProducerProps())
    private val gameLogConsumer = KafkaConsumer<String, SpecificRecord>(kafkaConsumerProps(topic = GAME_LOG_TOPIC))

    companion object {
        const val CLIENT_COMMAND_TOPIC = "game3.events.client.command"
        const val GAME_LOG_TOPIC = "game3.events.game.log"
    }

    @Test
    @DirtiesContext
    fun `write a InitGameMove to the game log`() {
        // GIVEN:
        // - empty game log
        // as well as:
        val senderId = "playerA"
        clientCommandProducer.send(ProducerRecord(CLIENT_COMMAND_TOPIC, senderId, AuthenticateCommand(senderId)))

        // WHEN
        gameService.start()

        // THEN

    }

    private fun kafkaConsumerProps(topic: String? = null, enableAutoCommit: Boolean = true): Map<String, Any> {
        val props = mutableMapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to "org.apache.kafka.common.serialization.StringDeserializer",
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to "io.confluent.kafka.serializers.KafkaAvroDeserializer",
            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to enableAutoCommit,
            ConsumerConfig.GROUP_ID_CONFIG to UUID.randomUUID().toString(),
            KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG to redPandaContainer.schemaRegistryAddress,
            KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG to true,
        )

        if (topic != null) {
            props[ConsumerConfig.GROUP_ID_CONFIG] = topic
        }

        return props
    }

    private fun kafkaProducerProps(): Properties {
        val props = Properties()
        props["bootstrap.servers"] = bootstrapServers
        props["key.serializer"] = "org.apache.kafka.common.serialization.StringSerializer"
        props["value.serializer"] = "io.confluent.kafka.serializers.KafkaAvroSerializer"
        props["schema.registry.url"] = schemaRegistryUrl
        props[KafkaAvroSerializerConfig.VALUE_SUBJECT_NAME_STRATEGY] = "io.confluent.kafka.serializers.subject.TopicRecordNameStrategy"
        return props
    }
}