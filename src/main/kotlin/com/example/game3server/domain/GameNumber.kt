package com.example.game3server.domain

import com.example.game3server.domain.command.Command
import com.example.game3server.domain.command.DecGameCommand
import com.example.game3server.domain.command.IncGameCommand
import com.example.game3server.domain.command.NoOpGameCommand

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
