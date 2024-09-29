package com.example.game3server.infrastructure.messaging.config

import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig
import io.confluent.kafka.serializers.KafkaAvroSerializer
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import java.util.*

@Configuration
class MessagingConfig {

    companion object {
        private const val BOOTSTRAP_SERVERS = "localhost:9092"
        private const val SCHEMA_REGISTRY_URL = "http://localhost:8085"

        fun kafkaConsumerProps(topic: String? = null, enableAutoCommit: Boolean = true): Map<String, Any> {
            val props = mutableMapOf(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to BOOTSTRAP_SERVERS,
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to "org.apache.kafka.common.serialization.StringDeserializer",
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to "io.confluent.kafka.serializers.KafkaAvroDeserializer",
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to enableAutoCommit,
                "schema.registry.url" to SCHEMA_REGISTRY_URL,
                KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG to true,
            )

            if (topic != null) {
                props[ConsumerConfig.GROUP_ID_CONFIG] = topic
            }

            return props
        }
    }

    @Bean
    fun consumerFactory() = DefaultKafkaConsumerFactory<String, SpecificRecord>(kafkaConsumerProps())

    @Bean
    fun kafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, SpecificRecord> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, SpecificRecord>()
        factory.consumerFactory = consumerFactory()
        return factory
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    fun kafkaConsumer(groupId: String) = KafkaConsumer<String, SpecificRecord>(kafkaConsumerProps(groupId))

    @Bean
    fun kafkaSender() = KafkaProducer<String, SpecificRecord>(kafkaSenderProps())



    private fun kafkaSenderProps(): Properties {
        val props = Properties()
        props["bootstrap.servers"] = "localhost:9092"
        props["key.serializer"] = "org.apache.kafka.common.serialization.StringSerializer"
        props["value.serializer"] = "io.confluent.kafka.serializers.KafkaAvroSerializer"
        props["schema.registry.url"] = "http://localhost:8085"
        props[KafkaAvroSerializerConfig.VALUE_SUBJECT_NAME_STRATEGY] = "io.confluent.kafka.serializers.subject.TopicRecordNameStrategy"
        return props
    }
}
