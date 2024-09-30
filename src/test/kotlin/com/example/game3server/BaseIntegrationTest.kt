package com.example.game3server

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@SpringBootTest
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class BaseIntegrationTest {

    companion object {
        private val kafkaContainer: KafkaContainer =
            KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka"))
                .apply {
                    withExposedPorts(9093)
                    start()
                }

        @JvmStatic
        @DynamicPropertySource
        private fun registerProperties(registry: DynamicPropertyRegistry) {
            registry.add("kafka.bootstrapServers") { kafkaContainer.bootstrapServers }
            registry.add("game.service.auto-start") { false }
        }
    }

}