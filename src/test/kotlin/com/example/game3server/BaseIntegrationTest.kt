package com.example.game3server

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.redpanda.RedpandaContainer

@SpringBootTest
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class BaseIntegrationTest {

    companion object {
        // private val kafkaContainer: KafkaContainer =
        //     KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka"))
        //         .apply {
        //             withNetwork(network)
        //             withExposedPorts(9093)
        //             start()
        //         }
        val redPandaContainer =
            RedpandaContainer("docker.redpanda.com/redpandadata/redpanda")
                .apply {
                    withExposedPorts(8081, 9091, 9092)
                    start()
                }


        @JvmStatic
        @DynamicPropertySource
        private fun registerProperties(registry: DynamicPropertyRegistry) {
            registry.add("kafka.bootstrapServers") { redPandaContainer.bootstrapServers }
            registry.add("kafka.schemaRegistryUrl") { redPandaContainer.schemaRegistryAddress }
            registry.add("game.service.auto-start") { false }
        }
    }

}