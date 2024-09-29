package com.example.game3server.application.model

import com.example.game3server.domain.*
import game3.events.game.log.*

fun InitGameMove.toLog(): GameInitialized {
    return GameInitialized(
        executedBy.id,
        this.gameId,
        startingNumber.value,
        listOf(executedBy.id)
    )
}

fun GameInitialized.toMove(): InitGameMove {
    return InitGameMove(
        executedBy = Player(this.initializedBy),
        startingNumber = GameNumber(this.startingNumber),
        gameId = this.gameId
    )
}

fun EndGameMove.toLog(game: Game): GameEnded {
    return GameEnded(
        game.gameId,
        game.winner?.id
    )
}

fun GameEnded.toMove(): EndGameMove {
    return EndGameMove()
}

fun JoinGameMove.toLog(game: Game): GameJoined {
    return GameJoined(
        this.executedBy.id,
        game.gameId
    )
}

fun GameJoined.toMove(): JoinGameMove {
    return JoinGameMove(
        executedBy = Player(this.playerId),
    )
}

fun IncGameMove.toLog(game: Game): IncMoveExecuted {
    return IncMoveExecuted(
        this.id,
        this.executedBy.id,
        game.gameId
    )
}

fun IncMoveExecuted.toMove(): IncGameMove {
    return IncGameMove(
        this.id,
        executedBy = Player(this.executedBy),
    )
}

fun DecGameMove.toLog(game: Game): DecMoveExecuted {
    return DecMoveExecuted(
        this.id,
        this.executedBy.id,
        game.gameId
    )
}

fun DecMoveExecuted.toMove(): DecGameMove {
    return DecGameMove(
        this.id,
        executedBy = Player(this.executedBy),
    )
}

fun NoOpGameMove.toLog(game: Game): NoOpMoveExecuted {
    return NoOpMoveExecuted(
        this.id,
        this.executedBy.id,
        game.gameId
    )
}

fun NoOpMoveExecuted.toMove(): NoOpGameMove {
    return NoOpGameMove(
        this.id,
        executedBy = Player(this.executedBy),
    )
}
