package com.example.game3server.infrastructure.persistence

import com.example.game3server.application.messaging.GameLogConsumable
import com.example.game3server.application.persistence.GameSnapshot
import com.example.game3server.application.persistence.GameSnapshotRepository
import com.example.game3server.domain.Game
import com.example.game3server.domain.GameMove
import com.example.game3server.domain.Player
import com.example.game3server.infrastructure.messaging.mapper.GameMoveMapper
import game3.events.game.log.DecMoveExecuted
import game3.events.game.log.GameEnded
import game3.events.game.log.GameInitialized
import game3.events.game.log.IncMoveExecuted
import game3.events.game.log.NoOpMoveExecuted
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.springframework.stereotype.Repository

@Repository
class KafkaGameSnapshotRepository(
    private val gameLogConsumer: GameLogConsumable
) : GameSnapshotRepository<Long> {

    private var snapshot: GameSnapshot<Long>? = null

    override fun get(): GameSnapshot<Long> = snapshot ?: create().also { snapshot = it }

    override fun create(): GameSnapshot<Long> {
        val records = gameLogConsumer.fetchFromBeginning()
        val snapshot = findCurrentGameSnapshot(records)
        return snapshot
    }

    private fun findCurrentGameSnapshot(records: ConsumerRecords<String, SpecificRecord>): GameSnapshot<Long> {
        val allEvents = records.toList()
        val startingIndex = allEvents.indexOfLast { it.value() is GameInitialized || it.value() is GameEnded }

        if (startingIndex == -1) return GameSnapshot(Game.createEmpty(), 0)

        val definingEvent = allEvents[startingIndex]
        gameLogConsumer.commitSync(definingEvent.offset())

        if (definingEvent.value() is GameEnded) {
            return GameSnapshot(Game.createEmpty(), startingIndex.toLong())
        }

        val gameInitialized = definingEvent.value() as GameInitialized
        val gameId = gameInitialized.gameId
        val currentGameEvents = allEvents.subList(startingIndex, allEvents.size)

        val participants = gameInitialized.participants.map { Player(it) }
        val moveHistory = currentGameEvents
            .map { it.value() }
            .filter {
                when (it) {
                    is NoOpMoveExecuted -> it.gameId == gameId
                    is IncMoveExecuted -> it.gameId == gameId
                    is DecMoveExecuted -> it.gameId == gameId
                    else -> false
                }
            }
            .map {
                when (it) {
                    is NoOpMoveExecuted -> GameMoveMapper.fromEvent(it)
                    is IncMoveExecuted -> GameMoveMapper.fromEvent(it)
                    is DecMoveExecuted -> GameMoveMapper.fromEvent(it)
                    else -> assert(false)
                }
            } as List<GameMove>

        return GameSnapshot(Game(gameId, moveHistory, participants), startingIndex.toLong())
    }

}