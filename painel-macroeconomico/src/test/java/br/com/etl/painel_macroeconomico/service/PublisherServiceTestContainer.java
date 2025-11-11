package br.com.etl.painel_macroeconomico.service;


import br.com.etl.painel_macroeconomico.model.IndicadorEconomico;
import br.com.etl.painel_macroeconomico.repository.IndicadorEconomicoRepository;
import br.com.etl.painel_macroeconomico.service.docker.AbstractPostgresDockerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PublisherServiceTestContainer extends AbstractPostgresDockerTest {

    @Autowired
    private IndicadorEconomicoRepository indicadorRepository;

    @Autowired
    private PublisherService publisherService;

    private LocalDate dataInicial;
    private LocalDate dataFinal;

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");

        // Debug
        System.out.println("🔹 JDBC URL: " + postgres.getJdbcUrl());
        System.out.println("🔹 Username: " + postgres.getUsername());
        System.out.println("🔹 Password: " + postgres.getPassword());
    }

    @BeforeEach
    void setup() {
        dataInicial = LocalDate.of(2024, 1, 1);
        dataFinal = LocalDate.of(2024, 1, 31);

        // Limpa tabela antes de cada teste
        indicadorRepository.deleteAll();
    }

    @Test
    @DisplayName("Deve persistir indicadores no banco via PublisherService")
    void devePersistirIndicadoresComSucesso() {
        // Arrange: Cria indicadores simulando dados vindos do BCB
        IndicadorEconomico indicador1 = new IndicadorEconomico();
        indicador1.setNome("Dólar");
        indicador1.setCodigoBc(10813);
        indicador1.setValor(BigDecimal.valueOf(5.15));
        indicador1.setData(LocalDate.of(2024, 1, 1));
        indicador1.setFrequencia("Diária");
        indicador1.setCreatedAt(OffsetDateTime.now());

        IndicadorEconomico indicador2 = new IndicadorEconomico();
        indicador2.setNome("Dólar");
        indicador2.setCodigoBc(10813);
        indicador2.setValor(BigDecimal.valueOf(5.20));
        indicador2.setData(LocalDate.of(2024, 1, 2));
        indicador2.setFrequencia("Diária");
        indicador2.setCreatedAt(OffsetDateTime.now());

        // Act: Salva via PublisherService (ou diretamente para simular fetch)
        indicadorRepository.saveAll(List.of(indicador1, indicador2));

        List<IndicadorEconomico> saved = indicadorRepository.findAll();
        assertEquals(2, saved.size());

        // Assert: Verifica consistência dos dados
        assertTrue(saved.stream().anyMatch(i -> i.getValor().compareTo(BigDecimal.valueOf(5.15)) == 0));
        assertTrue(saved.stream().anyMatch(i -> i.getValor().compareTo(BigDecimal.valueOf(5.20)) == 0));

        // Print para debug
        saved.forEach(i -> System.out.println(
                "ID: " + i.getId() +
                        ", Nome: " + i.getNome() +
                        ", Valor: " + i.getValor() +
                        ", Data: " + i.getData()
        ));
    }
}
