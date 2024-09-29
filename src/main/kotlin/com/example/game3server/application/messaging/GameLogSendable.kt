package com.example.game3server.application.messaging

import game3.events.game.log.*
import org.apache.kafka.clients.producer.RecordMetadata
import java.util.concurrent.Future

interface GameLogSendable {
    fun send(log: GameInitialized): Future<RecordMetadata>
    fun send(log: GameEnded): Future<RecordMetadata>
    fun send(log: GameJoined): Future<RecordMetadata>

    fun send(log: DecMoveExecuted): Future<RecordMetadata>
    fun send(log: IncMoveExecuted): Future<RecordMetadata>
    fun send(log: NoOpMoveExecuted): Future<RecordMetadata>
}