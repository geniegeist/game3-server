package com.example.game3server.infrastructure.messaging.config

import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import java.util.*

@Configuration
@EnableConfigurationProperties(MessagingConfig.KafkaConfig::class)
class MessagingConfig(
    private val kafkaConfig: KafkaConfig,
) {

    @ConfigurationProperties(prefix = "kafka")
    data class KafkaConfig(
        val bootstrapServers: String,
        val schemaRegistryUrl: String
    )

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


    fun kafkaConsumerProps(topic: String? = null, enableAutoCommit: Boolean = true): Map<String, Any> {
        val props = mutableMapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaConfig.bootstrapServers,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to "org.apache.kafka.common.serialization.StringDeserializer",
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to "io.confluent.kafka.serializers.KafkaAvroDeserializer",
            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to enableAutoCommit,
            KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG to kafkaConfig.schemaRegistryUrl,
            KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG to true,
        )

        if (topic != null) {
            props[ConsumerConfig.GROUP_ID_CONFIG] = topic
        }

        return props
    }

    fun kafkaSenderProps(): Properties {
        val props = Properties()
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = kafkaConfig.bootstrapServers
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = "org.apache.kafka.common.serialization.StringSerializer"
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = "io.confluent.kafka.serializers.KafkaAvroSerializer"
        props[KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG] = kafkaConfig.schemaRegistryUrl
        props[KafkaAvroSerializerConfig.VALUE_SUBJECT_NAME_STRATEGY] = "io.confluent.kafka.serializers.subject.TopicRecordNameStrategy"
        return props
    }
}
