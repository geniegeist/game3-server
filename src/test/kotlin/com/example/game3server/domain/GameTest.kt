package com.example.game3server.domain

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class GameTest {

    @Test
    fun `should calculate the correct current game number`() {
        // GIVEN
        val startingNumber = GameNumber(3)
        val gameId = "some-random-game"
        val playerA = Player("playerA")
        val playerB = Player("playerB")
        val participants = listOf(playerA, playerB)
        val moveHistory = listOf(
            InitGameMove(
                executedBy = playerA,
                startingNumber = startingNumber,
            ),
            JoinGameMove(
                executedBy = playerB
            ),
            NoOpGameMove(
                executedBy = playerB
            )
        )
        val game = Game(
            gameId,
            moveHistory,
            participants,
            initialized = true
        )
        val expectedGameNumber = GameNumber(1)

        // WHEN
        val result = game.currentNumber

        // THEN
        assertEquals(expectedGameNumber, result)
    }

    @Test
    fun `should calculate the correct winner when the winner move is NoOp`() {
        // GIVEN
        val startingNumber = GameNumber(3)
        val gameId = "some-random-game"
        val playerA = Player("playerA")
        val playerB = Player("playerB")
        val participants = listOf(playerA, playerB)
        val moveHistory = listOf(
            InitGameMove(
                executedBy = playerA,
                startingNumber = startingNumber,
            ),
            JoinGameMove(
                executedBy = playerB
            ),
            NoOpGameMove(
                executedBy = playerB
            )
        )
        val game = Game(
            gameId,
            moveHistory,
            participants,
            initialized = true
        )
        val expectedWinner = playerB

        // WHEN
        val result = game.winner

        // THEN
        assertEquals(expectedWinner, result)
    }

    @Test
    fun `should calculate the correct winner when the winner move is Inc`() {
        // GIVEN
        val startingNumber = GameNumber(2)
        val gameId = "some-random-game"
        val playerA = Player("playerA")
        val playerB = Player("playerB")
        val participants = listOf(playerA, playerB)
        val moveHistory = listOf(
            InitGameMove(
                executedBy = playerA,
                startingNumber = startingNumber,
            ),
            JoinGameMove(
                executedBy = playerB
            ),
            IncGameMove(
                executedBy = playerB
            )
        )
        val game = Game(
            gameId,
            moveHistory,
            participants,
            initialized = true
        )
        val expectedWinner = playerB

        // WHEN
        val result = game.winner

        // THEN
        assertEquals(expectedWinner, result)
    }

    @Test
    fun `should calculate the correct winner when the winner move is Dec`() {
        // GIVEN
        val startingNumber = GameNumber(4)
        val gameId = "some-random-game"
        val playerA = Player("playerA")
        val playerB = Player("playerB")
        val participants = listOf(playerA, playerB)
        val moveHistory = listOf(
            InitGameMove(
                executedBy = playerA,
                startingNumber = startingNumber,
            ),
            JoinGameMove(
                executedBy = playerB
            ),
            IncGameMove(
                executedBy = playerB
            )
        )
        val game = Game(
            gameId,
            moveHistory,
            participants,
            initialized = true
        )
        val expectedWinner = playerB

        // WHEN
        val result = game.winner

        // THEN
        assertEquals(expectedWinner, result)
    }
}