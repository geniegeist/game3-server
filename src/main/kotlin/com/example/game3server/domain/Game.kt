package com.example.game3server.domain

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