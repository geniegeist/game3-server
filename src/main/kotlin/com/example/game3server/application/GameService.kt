package com.example.game3server.application

import com.example.game3server.application.model.toLog
import com.example.game3server.application.model.toMove
import com.example.game3server.application.persistence.GameSnapshot
import com.example.game3server.application.persistence.GameSnapshotRepository
import com.example.game3server.domain.*
import com.example.game3server.domain.command.*
import com.example.game3server.infrastructure.messaging.consumption.ClientCommandKafkaListener
import com.example.game3server.infrastructure.messaging.consumption.ClientCommandKafkaListenerDelegate
import com.example.game3server.infrastructure.messaging.consumption.GameLogKafkaListener
import com.example.game3server.infrastructure.messaging.consumption.GameLogKafkaListenerDelegate
import com.example.game3server.infrastructure.messaging.production.GameLogKafkaSender
import com.example.game3server.infrastructure.messaging.production.ServerCommandKafkaSender
import game3.events.client.command.AuthenticateCommand
import game3.events.client.command.ExecuteGameMoveCommand
import game3.events.game.log.*
import game3.events.server.command.ClearClientViewCommand
import game3.events.server.command.PrintMessageToClientCommand
import game3.events.server.command.ViableGameActionsCommand
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

@Service
class GameService(
    gameSnapshotRepository: GameSnapshotRepository<Long>,
    private val gameLogListener: GameLogKafkaListener,
    private val clientCommandListener: ClientCommandKafkaListener,
    private val serverCommandSender: ServerCommandKafkaSender,
    private val gameLogSender: GameLogKafkaSender,
) : GameLogKafkaListenerDelegate, ClientCommandKafkaListenerDelegate {
    private var game: Game = Game.createEmpty()
    private val snapshot: GameSnapshot<Long> = gameSnapshotRepository.get()

    init {
        game = snapshot.game
    }

    @EventListener(ApplicationReadyEvent::class)
    private fun onApplicationEvent(event: ApplicationReadyEvent) {
        // do it here to avoid leaking this in init
        gameLogListener.delegate = this
        clientCommandListener.delegate = this

        gameLogListener.startListening(snapshot.offset)
        clientCommandListener.startListening()
    }

    override fun didReceive(event: GameInitialized) {
        val update = event.toMove().apply(game)
        process(update)
    }

    override fun didReceive(event: GameJoined) {
        val update = event.toMove().apply(game)
        process(update)
    }

    override fun didReceive(event: GameEnded) = process(event.toMove().apply(game))

    override fun didReceive(event: DecMoveExecuted) = process(event.toMove().apply(game))

    override fun didReceive(event: IncMoveExecuted) = process(event.toMove().apply(game))

    override fun didReceive(event: NoOpMoveExecuted) = process(event.toMove().apply(game))

    private fun process(gameUpdate: GameUpdate) {
        game = gameUpdate.game
        handle(gameUpdate.commands)

        if (game.ended) {
            processGameEnd()
        }
    }

    private fun processGameEnd() {
        game.winner?.let {
            this.gameLogSender.send(GameEnded(game.gameId, it.id))
        } ?: throw Exception("Game ended but no winner")
    }

    // ClientCommandKafkaListener Delegate

    override fun didReceive(command: AuthenticateCommand) {
        println("didReceive AuthenticateCommand $command")

        val player = Player(command.playerId)

        if (!game.initialized) {
            gameLogSender.send(InitGameMove(executedBy = player).toLog())
        } else if (JoinGameMove.canJoin(game, player)) {
            gameLogSender.send(JoinGameMove(executedBy = player).toLog(game))
        } else {
            TODO("replay everything beginning from first move of game")
        }
    }

    override fun didReceive(command: ExecuteGameMoveCommand) {
        println("didReceive ExecuteGameMoveCommand $command")

        val player = Player(command.playerId)

        if (!game.ended) {
            when (command.type) {
                "inc" -> gameLogSender.send(IncGameMove(executedBy = player).toLog(game))
                "dec" -> gameLogSender.send(DecGameMove(executedBy = player).toLog(game))
                "noop" -> gameLogSender.send(NoOpGameMove(executedBy = player).toLog(game))
            }
        }
    }

    //

    private fun handle(commands: List<Command>) = commands.forEach { handle(it) }

    private fun handle(command: Command) {
        when (command) {
            is PrintMessageViewCommand -> serverCommandSender.send(PrintMessageToClientCommand(command.player.id, command.message))
            is ClearViewCommand -> serverCommandSender.send(ClearClientViewCommand(command.player.id))
            is IncGameCommand -> serverCommandSender.send(ViableGameActionsCommand(command.receiver.id, listOf("inc")))
            is DecGameCommand -> serverCommandSender.send(ViableGameActionsCommand(command.receiver.id, listOf("dec")))
            is NoOpGameCommand -> serverCommandSender.send(ViableGameActionsCommand(command.receiver.id, listOf("noop")))
        }
    }
}