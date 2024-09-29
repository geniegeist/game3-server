package com.example.game3server.infrastructure.messaging.mapper

import com.example.game3server.domain.*
import game3.events.game.log.DecMoveExecuted
import game3.events.game.log.IncMoveExecuted
import game3.events.game.log.NoOpMoveExecuted

class GameMoveMapper {
    companion object {
        fun fromEvent(event: NoOpMoveExecuted): GameMove = NoOpGameMove(event.id, Player(event.executedBy))
        fun fromEvent(event: IncMoveExecuted): GameMove = IncGameMove(event.id, Player(event.executedBy))
        fun fromEvent(event: DecMoveExecuted): GameMove = DecGameMove(event.id, Player(event.executedBy))
    }
}