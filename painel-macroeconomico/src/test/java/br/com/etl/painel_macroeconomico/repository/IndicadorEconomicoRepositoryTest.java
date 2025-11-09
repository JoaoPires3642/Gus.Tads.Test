package br.com.etl.painel_macroeconomico.repository;

import br.com.etl.painel_macroeconomico.dto.ResultadoAgregacaoAnual;
import br.com.etl.painel_macroeconomico.dto.ResultadoAgregacaoMensal;
import br.com.etl.painel_macroeconomico.model.IndicadorEconomico;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.default_schema=test"
})
class IndicadorEconomicoRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private IndicadorEconomicoRepository repository;

    private IndicadorEconomico indicador1;
    private IndicadorEconomico indicador2;

    @BeforeEach
    void setUp() {
        // Limpa o banco antes de cada teste
        repository.deleteAll();
        entityManager.clear();

        indicador1 = new IndicadorEconomico();
        indicador1.setNome("Dólar");
        indicador1.setCodigoBc(10813);
        indicador1.setValor(BigDecimal.valueOf(5.15));
        indicador1.setData(LocalDate.of(2024, 1, 15));
        indicador1.setFrequencia("Diária");
        indicador1.setCreatedAt(OffsetDateTime.now());

        indicador2 = new IndicadorEconomico();
        indicador2.setNome("Dólar");
        indicador2.setCodigoBc(10813);
        indicador2.setValor(BigDecimal.valueOf(5.20));
        indicador2.setData(LocalDate.of(2024, 1, 16));
        indicador2.setFrequencia("Diária");
        indicador2.setCreatedAt(OffsetDateTime.now());
    }

    @Test
    @DisplayName("Deve salvar indicador econômico")
    void deveSalvarIndicadorEconomico() {
        // Act
        IndicadorEconomico saved = repository.save(indicador1);
        entityManager.flush();

        // Assert
        assertNotNull(saved.getId());
        assertEquals("Dólar", saved.getNome());
        assertEquals(10813, saved.getCodigoBc());
        assertEquals(0, BigDecimal.valueOf(5.15).compareTo(saved.getValor()));
    }

    @Test
    @DisplayName("Deve buscar indicador por código BC e data")
    void deveBuscarIndicadorPorCodigoBcEData() {
        // Arrange
        entityManager.persist(indicador1);
        entityManager.flush();
        entityManager.clear();

        // Act
        Optional<IndicadorEconomico> resultado = repository
                .findByCodigoBcAndData(10813, LocalDate.of(2024, 1, 15));

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals("Dólar", resultado.get().getNome());
        assertEquals(10813, resultado.get().getCodigoBc());
    }

    @Test
    @DisplayName("Deve retornar Optional.empty quando não encontrar indicador")
    void deveRetornarOptionalEmptyQuandoNaoEncontrarIndicador() {
        // Act
        Optional<IndicadorEconomico> resultado = repository
                .findByCodigoBcAndData(99999, LocalDate.of(2024, 1, 15));

        // Assert
        assertFalse(resultado.isPresent());
    }

    @Test
    @DisplayName("Deve calcular agregados mensais")
    void deveCalcularAgregadosMensais() {
        // Arrange
        entityManager.persist(indicador1);
        entityManager.persist(indicador2);
        entityManager.flush();
        entityManager.clear();

        LocalDate dataInicio = LocalDate.of(2024, 1, 1);
        LocalDate dataFim = LocalDate.of(2024, 1, 31);

        // Act
        List<ResultadoAgregacaoMensal> resultados = repository
                .calcularAgregadosMensais(dataInicio, dataFim);

        // Assert
        assertNotNull(resultados);
        assertFalse(resultados.isEmpty(), "Deve retornar pelo menos um resultado");

        ResultadoAgregacaoMensal primeiro = resultados.get(0);
        assertEquals(10813, primeiro.codigoBc());
        assertEquals(2024, primeiro.ano());
        assertEquals(1, primeiro.mes());
        assertNotNull(primeiro.valorMedio());
        assertNotNull(primeiro.valorMaximo());
        assertNotNull(primeiro.valorMinimo());
    }

    @Test
    @DisplayName("Deve calcular agregados anuais")
    void deveCalcularAgregadosAnuais() {
        // Arrange
        entityManager.persist(indicador1);
        entityManager.persist(indicador2);
        entityManager.flush();
        entityManager.clear();

        LocalDate dataInicio = LocalDate.of(2024, 1, 1);
        LocalDate dataFim = LocalDate.of(2024, 12, 31);

        // Act
        List<ResultadoAgregacaoAnual> resultados = repository
                .calcularAgregadosAnuais(dataInicio, dataFim);

        // Assert
        assertNotNull(resultados);
        assertFalse(resultados.isEmpty(), "Deve retornar pelo menos um resultado");

        ResultadoAgregacaoAnual primeiro = resultados.get(0);
        assertEquals(10813, primeiro.codigoBc());
        assertEquals(2024, primeiro.ano());
        assertNotNull(primeiro.valorMedio());
        assertNotNull(primeiro.valorMaximo());
        assertNotNull(primeiro.valorMinimo());
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há dados no período")
    void deveRetornarListaVaziaQuandoNaoHaDadosNoPeriodo() {
        // Arrange
        LocalDate dataInicio = LocalDate.of(2025, 1, 1);
        LocalDate dataFim = LocalDate.of(2025, 1, 31);

        // Act
        List<ResultadoAgregacaoMensal> resultados = repository
                .calcularAgregadosMensais(dataInicio, dataFim);

        // Assert
        assertNotNull(resultados);
        assertTrue(resultados.isEmpty(), "Deve retornar lista vazia");
    }

    @Test
    @DisplayName("Deve agrupar por código BC corretamente")
    void deveAgruparPorCodigoBcCorretamente() {
        // Arrange
        IndicadorEconomico selic = new IndicadorEconomico();
        selic.setNome("Selic");
        selic.setCodigoBc(4390);
        selic.setValor(BigDecimal.valueOf(11.75));
        selic.setData(LocalDate.of(2024, 1, 15));
        selic.setFrequencia("Mensal");
        selic.setCreatedAt(OffsetDateTime.now());

        entityManager.persist(indicador1);
        entityManager.persist(selic);
        entityManager.flush();
        entityManager.clear();

        LocalDate dataInicio = LocalDate.of(2024, 1, 1);
        LocalDate dataFim = LocalDate.of(2024, 1, 31);

        // Act
        List<ResultadoAgregacaoMensal> resultados = repository
                .calcularAgregadosMensais(dataInicio, dataFim);

        // Assert
        assertNotNull(resultados);
        assertEquals(2, resultados.size(), "Deve retornar 2 grupos (um para cada código BC)");
        assertTrue(resultados.stream().anyMatch(r -> r.codigoBc() == 10813));
        assertTrue(resultados.stream().anyMatch(r -> r.codigoBc() == 4390));
    }

    @Test
    @DisplayName("Deve persistir e recuperar todos os campos corretamente")
    void devePersistirERecuperarTodosCamposCorretamente() {
        // Arrange & Act
        IndicadorEconomico saved = repository.save(indicador1);
        entityManager.flush();
        entityManager.clear();

        IndicadorEconomico found = repository.findById(saved.getId()).orElse(null);

        // Assert
        assertNotNull(found);
        assertEquals(indicador1.getNome(), found.getNome());
        assertEquals(indicador1.getCodigoBc(), found.getCodigoBc());
        assertEquals(0, indicador1.getValor().compareTo(found.getValor()));
        assertEquals(indicador1.getData(), found.getData());
        assertEquals(indicador1.getFrequencia(), found.getFrequencia());
        assertNotNull(found.getCreatedAt());
    }

    @Test
    @DisplayName("Deve calcular média corretamente nos agregados mensais")
    void deveCalcularMediaCorretamenteNosAgregadosMensais() {
        // Arrange
        // Adiciona um terceiro indicador para testar a média
        IndicadorEconomico indicador3 = new IndicadorEconomico();
        indicador3.setNome("Dólar");
        indicador3.setCodigoBc(10813);
        indicador3.setValor(BigDecimal.valueOf(5.25));
        indicador3.setData(LocalDate.of(2024, 1, 17));
        indicador3.setFrequencia("Diária");
        indicador3.setCreatedAt(OffsetDateTime.now());

        entityManager.persist(indicador1); // 5.15
        entityManager.persist(indicador2); // 5.20
        entityManager.persist(indicador3); // 5.25
        entityManager.flush();
        entityManager.clear();

        LocalDate dataInicio = LocalDate.of(2024, 1, 1);
        LocalDate dataFim = LocalDate.of(2024, 1, 31);

        // Act
        List<ResultadoAgregacaoMensal> resultados = repository
                .calcularAgregadosMensais(dataInicio, dataFim);

        // Assert
        assertNotNull(resultados);
        assertFalse(resultados.isEmpty());

        ResultadoAgregacaoMensal resultado = resultados.get(0);

        // Média esperada: (5.15 + 5.20 + 5.25) / 3 = 5.20
        assertEquals(5.20, resultado.valorMedio(), 0.01);

        // Máximo: 5.25
        assertEquals(0, BigDecimal.valueOf(5.25).compareTo(resultado.valorMaximo()));

        // Mínimo: 5.15
        assertEquals(0, BigDecimal.valueOf(5.15).compareTo(resultado.valorMinimo()));
    }
}