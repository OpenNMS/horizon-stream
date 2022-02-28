package org.opennms.horizon.alarms.itests;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class AlarmIT {

    private final GenericContainer<?> pgContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:13.3-alpine"))
            .withEnv("POSTGRES_HOST_AUTH_METHOD", "trust")
            .withNetwork(Network.SHARED)
            .withNetworkAliases(CoreContainer.DB_ALIAS);

    private final KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:6.2.1"))
            .withStartupTimeout(Duration.of(2, ChronoUnit.MINUTES))
            .withNetwork(Network.SHARED)
            .withNetworkAliases(CoreContainer.KAFKA_ALIAS);

    private final CoreContainer coreContainer = new CoreContainer((PostgreSQLContainer<?>) pgContainer, kafkaContainer);

    @Rule
    public RuleChain stackChain = RuleChain
            .outerRule(pgContainer)
            .around(kafkaContainer)
            .around(coreContainer);

    @Test
    public void canStartContainers() {
        // if we got this far, the container start successfully - that's all we want to check for now
    }

}
