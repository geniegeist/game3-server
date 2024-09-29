package com.example.game3server.application.messaging

import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.clients.consumer.ConsumerRecords

interface GameLogConsumable {
    fun fetchFromBeginning(): ConsumerRecords<String, SpecificRecord>
}