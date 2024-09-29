package com.example.game3server.domain

import com.example.game3server.domain.command.ClearViewCommand
import com.example.game3server.domain.command.PrintMessageViewCommand
import java.util.UUID

interface GameMove {
    val id: String
    fun apply(game: Game): GameUpdate
}

data class InitGameMove(
    override val id: String = UUID.randomUUID().toString(),
    val executedBy: Player,
    val startingNumber: GameNumber = GameNumber.random(),
    val gameId: String = UUID.randomUUID().toString(),
) : GameMove {

    companion object {
        fun isNotInitialized(game: Game) = !game.initialized
    }

    override fun apply(game: Game): GameUpdate {
        val updatedGame = Game(
            gameId = gameId,
            moveHistory = game.moveHistory + this,
            participants = game.participants + executedBy,
            initialized = true,
        )
        return GameUpdate(updatedGame, listOf(
            ClearViewCommand(executedBy),
            PrintMessageViewCommand(executedBy, "😭 No ongoing game found."),
            PrintMessageViewCommand(executedBy, "🏁 Starting a new game."),
            PrintMessageViewCommand(executedBy, ""),
            PrintMessageViewCommand(executedBy, "----- Game(${updatedGame.gameId}) -----"),
            PrintMessageViewCommand(executedBy, ""),
            PrintMessageViewCommand(executedBy, "🍀 You drew the number $startingNumber and sent it."),
        ))
    }
}

data class EndGameMove(
    override val id: String = UUID.randomUUID().toString(),
) : GameMove {
    override fun apply(game: Game): GameUpdate
        = GameUpdate(
            Game.createEmpty(),
            game.participants.map {
                if (it == game.winner) PrintMessageViewCommand(it, "🎉 You won.")
                else PrintMessageViewCommand(it, "😭 Your opponent obtained ${game.currentNumber}. You lost.")
            }
        )
}

data class JoinGameMove(
    override val id: String = UUID.randomUUID().toString(),
    val executedBy: Player,
) : GameMove {

    companion object {
        fun canJoin(game: Game, player: Player) = !game.full && !game.participants.contains(player)
    }

    override fun apply(game: Game): GameUpdate {
        val updatedGame = game
                .modifiedMoves(game.moveHistory + this)
                .modifiedParticipants(game.participants + executedBy)

        val commands = listOfNotNull(
            ClearViewCommand(executedBy),
            PrintMessageViewCommand(executedBy, "🙋‍♀️ Ongoing game found."),
            PrintMessageViewCommand(executedBy, "💃 You joined this game."),
            PrintMessageViewCommand(executedBy, ""),
            PrintMessageViewCommand(executedBy, "----- Game(${updatedGame.gameId}) -----"),
            PrintMessageViewCommand(executedBy, ""),
            PrintMessageViewCommand(executedBy, "🆕 You received ${updatedGame.currentNumber}"),
            updatedGame.currentNumber?.nextValidMove?.toCommand(executedBy)
        )

        return when (val lastMove = game.lastMove) {
            is InitGameMove -> GameUpdate(updatedGame, commands)
            else -> throw Exception("Invalid lastMove $lastMove")
        }
    }
}

data class IncGameMove(
    override val id: String = UUID.randomUUID().toString(),
    val executedBy: Player,
) : GameMove {

    companion object {
        fun isValid(game: Game) = game.full && game.initialized && game.startingNumber != null && game.currentNumber != null
    }

    override fun apply(game: Game): GameUpdate {
        val updatedGame = game
            .modifiedMoves(game.moveHistory + this)
        val otherPlayer = game.otherPlayer(executedBy)

        return otherPlayer?.let {
            GameUpdate(
                updatedGame,
                listOfNotNull(
                    PrintMessageViewCommand(executedBy, "🙆‍♀️ You increment. Send (${game.currentNumber} + 1) / 3 = ${updatedGame.currentNumber}"),
                    PrintMessageViewCommand(otherPlayer, "🤖 Your opponent incremented: Received (${game.currentNumber} + 1) / 3 = ${updatedGame.currentNumber}"),
                    updatedGame.currentNumber?.nextValidMove?.toCommand(otherPlayer)
                ))
        } ?: throw Exception("No other player")
    }

}

data class DecGameMove(
    override val id: String = UUID.randomUUID().toString(),
    val executedBy: Player,
) : GameMove {

    companion object {
        fun isValid(game: Game) = game.full && game.initialized
    }

    override fun apply(game: Game): GameUpdate {
        val updatedGame = game
            .modifiedMoves(game.moveHistory + this)
        val otherPlayer = game.otherPlayer(executedBy)

        return otherPlayer?.let {
            GameUpdate(
                updatedGame,
                listOfNotNull(
                    PrintMessageViewCommand(executedBy, "🙆‍♀️ You increment: Send (${game.currentNumber} - 1) / 3 = ${updatedGame.currentNumber}"),
                    PrintMessageViewCommand(otherPlayer, "🤖 Your opponent decremented: Received (${game.currentNumber} - 1) / 3 = ${updatedGame.currentNumber}"),
                    updatedGame.currentNumber?.nextValidMove?.toCommand(otherPlayer)
                ))
        } ?: throw Exception("No other player")
    }
}

data class NoOpGameMove(
    override val id: String = UUID.randomUUID().toString(),
    val executedBy: Player,
) : GameMove {

    companion object {
        fun isValid(game: Game) = game.full && game.initialized
    }

    override fun apply(game: Game): GameUpdate {
        val updatedGame = game
            .modifiedMoves(game.moveHistory + this)

        val otherPlayer = game.otherPlayer(executedBy)

        return otherPlayer?.let {
            GameUpdate(
                updatedGame,
                listOfNotNull(
                    PrintMessageViewCommand(executedBy, "🙆‍♀️ You keep the number: Send ${game.currentNumber} / 3 = ${updatedGame.currentNumber}"),
                    PrintMessageViewCommand(otherPlayer, "🤖 Your opponent kept the number: Received ${game.currentNumber} / 3 = ${updatedGame.currentNumber}"),
                    updatedGame.currentNumber?.nextValidMove?.toCommand(otherPlayer)
                ))
        } ?: throw Exception("No other player")
    }
}
