package com.example.game3server.domain

import com.example.game3server.domain.command.Command

data class GameUpdate(val game: Game, val commands: List<Command> = emptyList())