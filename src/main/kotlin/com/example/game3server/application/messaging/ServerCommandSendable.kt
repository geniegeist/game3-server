package com.example.game3server.application.messaging

import game3.events.server.command.ClearClientViewCommand
import game3.events.server.command.PrintMessageToClientCommand
import game3.events.server.command.ViableGameActionsCommand
import org.apache.kafka.clients.producer.RecordMetadata
import java.util.concurrent.Future

interface ServerCommandSendable {
    fun send(printMessageToClientCommand: PrintMessageToClientCommand): Future<RecordMetadata>

    fun send(clearCommand: ClearClientViewCommand): Future<RecordMetadata>

    fun send(command: ViableGameActionsCommand): Future<RecordMetadata>
}