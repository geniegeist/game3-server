package com.example.game3server.domain.command

import com.example.game3server.domain.Player

data class PrintMessageViewCommand(val player: Player, val message: String) : Command
data class ClearViewCommand(val player: Player) : Command