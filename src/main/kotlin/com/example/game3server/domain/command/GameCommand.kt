package com.example.game3server.domain.command

import com.example.game3server.domain.Player

data class IncGameCommand(val receiver: Player) : Command
data class DecGameCommand(val receiver: Player) : Command
data class NoOpGameCommand(val receiver: Player) : Command


