package com.example.game3server.application.messaging

import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.clients.consumer.ConsumerRecords

interface GameLogConsumable {
    fun fetch(offset: Int): ConsumerRecords<String, SpecificRecord>
}