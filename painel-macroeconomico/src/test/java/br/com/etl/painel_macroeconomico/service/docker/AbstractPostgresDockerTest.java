package br.com.etl.painel_macroeconomico.service.docker;

import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
public abstract class AbstractPostgresDockerTest {

    public static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:15-alpine"))
                    .withDatabaseName("testdb")
                    .withUsername("testuser")
                    .withPassword("testpass")
                    .withInitScript("docker.sql"); // 🔥 Lê seu dump e executa ao subir

    @BeforeAll
    static void startContainer() {
        System.out.println("🐳 Subindo Postgres Docker com dump...");
        postgres.start();
        System.out.println("✅ Container pronto e dump importado!");
    }
}
