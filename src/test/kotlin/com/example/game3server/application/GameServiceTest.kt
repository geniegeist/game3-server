package com.example.game3server.application

import com.example.game3server.BaseIntegrationTest
import com.example.game3server.application.messaging.GameLogConsumable
import com.example.game3server.application.persistence.GameSnapshot
import com.example.game3server.application.persistence.GameSnapshotRepository
import com.example.game3server.domain.Game
import com.example.game3server.infrastructure.messaging.consumption.ClientCommandKafkaListener
import com.example.game3server.infrastructure.messaging.consumption.GameLogKafkaListener
import com.example.game3server.infrastructure.messaging.production.GameLogKafkaSender
import com.example.game3server.infrastructure.messaging.production.ServerCommandKafkaSender
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.MockitoAnnotations
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationEventPublisher
import scala.reflect.internal.TreeInfo.Applied

internal class GameServiceTest : BaseIntegrationTest() {

    @MockkBean(relaxed = true)
    lateinit var gameSnapshotRepository: GameSnapshotRepository<Long>

    @MockkBean
    lateinit var publisher: ApplicationEventPublisher

    @MockkBean
    lateinit var gameLogConsumable: GameLogConsumable

    //@MockkBean(relaxed = true)
    //lateinit var gameLogListener: GameLogKafkaListener

    //@MockkBean(relaxed = true)
    lateinit var clientCommandListener: ClientCommandKafkaListener

    @MockkBean
    lateinit var serverCommandSender: ServerCommandKafkaSender

    @MockkBean
    lateinit var gameLogSender: GameLogKafkaSender

    @Autowired
    lateinit var gameService: GameService

    @Test
    fun `write a InitGameMove to the game log`() {

        every { gameSnapshotRepository.get() }.returns(
            GameSnapshot<Long>(
                Game.createEmpty(),
                0
            )
        )

        gameService.start()

    }
}