package com.example.game3server.domain

import com.example.game3server.domain.command.Command
import com.example.game3server.domain.command.DecGameCommand
import com.example.game3server.domain.command.IncGameCommand
import com.example.game3server.domain.command.NoOpGameCommand

data class GameUpdate(val game: Game, val commands: List<Command> = emptyList())

data class GameNumber(val value: Int) {

    enum class ValidGameMove {
        INC {
            override fun toCommand(receiver: Player): Command = IncGameCommand(receiver)
        },
        DEC {
            override fun toCommand(receiver: Player): Command = DecGameCommand(receiver)
        },
        NOOP {
            override fun toCommand(receiver: Player): Command = NoOpGameCommand(receiver)
        };

        abstract fun toCommand(receiver: Player): Command
    }

    companion object {
        fun random() = GameNumber((0..100000000).random())
    }

    val nextValidMove: ValidGameMove?
        get() =
            if (value <= 1) null
            else
                when (value % 3) {
                    0 -> ValidGameMove.NOOP
                    1 -> ValidGameMove.DEC
                    2 -> ValidGameMove.INC
                    else -> null
                }

    fun incremented() = GameNumber((value + 1) / 3)
    fun decremented() = GameNumber((value - 1) / 3)
    fun noop() = GameNumber(value / 3)

    override fun toString(): String = "$value"

}

data class Game(
    val gameId: String,
    val moveHistory: List<GameMove>,
    val participants: List<Player>,
    val initialized: Boolean = false,
) {

    companion object {
        fun createEmpty(): Game = Game("", mutableListOf(), mutableListOf(), false)
    }

    val full: Boolean
        get() = participants.size == 2

    val startingNumber: GameNumber?
        get() {
            if (moveHistory.isEmpty()) return null

            val firstMove = moveHistory.first()
            if (firstMove !is InitGameMove) {
                return null
            }
            return firstMove.startingNumber
        }
    
    val currentNumber: GameNumber?
        get() = startingNumber?.let {
            moveHistory.fold(it) { acc, move ->
                when (move) {
                    is IncGameMove -> acc.incremented()
                    is DecGameMove -> acc.decremented()
                    is NoOpGameMove -> acc.noop()
                    else -> acc
                }
            }
        }

    val lastMove = if (moveHistory.isEmpty()) null else moveHistory.last()

    val ended: Boolean
        get() = currentNumber?.value == 1

    val winner: Player?
        get() {
            if (ended) {
                return moveHistory.findLast { it is IncGameMove || it is DecGameMove }?.let {
                    when (it) {
                        is IncGameMove -> it.executedBy
                        is DecGameMove -> it.executedBy
                        else -> throw Exception("Last move is of invalid type")
                    }
                }
            }

            return null
        }

    // Constructor
    fun modifiedMoves(moves: List<GameMove>): Game = Game(gameId, moves, participants, initialized)
    fun modifiedParticipants(participants: List<Player>): Game = Game(gameId, moveHistory, participants, initialized)


    fun otherPlayer(player: Player): Player? = participants.find { it != player }
}