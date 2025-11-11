package br.com.etl.painel_macroeconomico.service;

import br.com.etl.painel_macroeconomico.model.IndicadorEconomico;
import br.com.etl.painel_macroeconomico.model.agregado.IndicadorAgregadoMensal;
import br.com.etl.painel_macroeconomico.model.agregado.IndicadorAgregadoAnual;
import br.com.etl.painel_macroeconomico.service.docker.AbstractPostgresDockerTest;
import br.com.etl.painel_macroeconomico.repository.IndicadorEconomicoRepository;
import br.com.etl.painel_macroeconomico.repository.IndicadorAgregadoMensalRepository;
import br.com.etl.painel_macroeconomico.repository.IndicadorAgregadoAnualRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AgregacaoServiceDockerTest extends AbstractPostgresDockerTest {

    @Autowired
    private IndicadorEconomicoRepository indicadorRepository;

    @Autowired
    private IndicadorAgregadoMensalRepository agregadoMensalRepository;

    @Autowired
    private IndicadorAgregadoAnualRepository agregadoAnualRepository;

    @Autowired
    private AgregacaoService agregacaoService;

    private LocalDate mesReferencia;
    private LocalDate anoReferencia;

    @BeforeEach
    void setUp() {
        mesReferencia = LocalDate.of(2024, 1, 15);
        anoReferencia = LocalDate.of(2024, 6, 1);

        // Não cria indicadores aqui, cada teste decide seus dados
    }

    @Test
    @DisplayName("Deve criar agregado mensal com sucesso")
    void deveCriarAgregadoMensalComSucesso() {
        // Criando indicadores específicos para IPCA que geram os valores do mock
        IndicadorEconomico ipca1 = new IndicadorEconomico();
        ipca1.setNome("IPCA");
        ipca1.setCodigoBc(10813);
        ipca1.setValor(BigDecimal.valueOf(5.45)); // máximo
        ipca1.setData(LocalDate.of(2024,1,5));
        ipca1.setFrequencia("MENSAL");
        ipca1.setCreatedAt(OffsetDateTime.now());
        indicadorRepository.save(ipca1);

        IndicadorEconomico ipca2 = new IndicadorEconomico();
        ipca2.setNome("IPCA");
        ipca2.setCodigoBc(10813);
        ipca2.setValor(BigDecimal.valueOf(5.15)); // médio
        ipca2.setData(LocalDate.of(2024,1,10));
        ipca2.setFrequencia("MENSAL");
        ipca2.setCreatedAt(OffsetDateTime.now());
        indicadorRepository.save(ipca2);

        IndicadorEconomico ipca3 = new IndicadorEconomico();
        ipca3.setNome("IPCA");
        ipca3.setCodigoBc(10813);
        ipca3.setValor(BigDecimal.valueOf(4.89)); // mínimo
        ipca3.setData(LocalDate.of(2024,1,12));
        ipca3.setFrequencia("MENSAL");
        ipca3.setCreatedAt(OffsetDateTime.now());
        indicadorRepository.save(ipca3);

        // Executa o serviço
        agregacaoService.calcularEsalvarAgregadosParaMes(mesReferencia);

        List<IndicadorAgregadoMensal> saved = agregadoMensalRepository.findAll();
        assertFalse(saved.isEmpty());

        // Verifica apenas IPCA, que é o que este teste exige
        Optional<IndicadorAgregadoMensal> ipcaAgregado = agregadoMensalRepository
                .findByCodigoBcAndAnoAndMes(10813, 2024, 1);

        assertTrue(ipcaAgregado.isPresent());
        assertEquals(0, BigDecimal.valueOf(5.15).compareTo(ipcaAgregado.get().getValorMedio()));
        assertEquals(0, BigDecimal.valueOf(5.45).compareTo(ipcaAgregado.get().getValorMaximo()));
        assertEquals(0, BigDecimal.valueOf(4.89).compareTo(ipcaAgregado.get().getValorMinimo()));
    }

    @Test
    @DisplayName("Deve criar agregado anual com sucesso")
    void deveCriarAgregadoAnualComSucesso() {
        // Apenas IPCA médio para anual
        IndicadorEconomico ipca = new IndicadorEconomico();
        ipca.setNome("IPCA");
        ipca.setCodigoBc(10813);
        ipca.setValor(BigDecimal.valueOf(5.15));
        ipca.setData(LocalDate.of(2024,1,10));
        ipca.setFrequencia("MENSAL");
        ipca.setCreatedAt(OffsetDateTime.now());
        indicadorRepository.save(ipca);

        agregacaoService.calcularEsalvarAgregadosParaAno(anoReferencia);

        List<IndicadorAgregadoAnual> saved = agregadoAnualRepository.findAll();
        assertFalse(saved.isEmpty());

        Optional<IndicadorAgregadoAnual> ipcaAgregado = agregadoAnualRepository
                .findByCodigoBcAndAno(10813, 2024);
        assertTrue(ipcaAgregado.isPresent());
        assertEquals(0, BigDecimal.valueOf(5.15).compareTo(ipcaAgregado.get().getValorMedio()));
    }

    @Test
    @DisplayName("Deve atualizar agregado mensal existente")
    void deveAtualizarAgregadoMensalExistente() {
        IndicadorAgregadoMensal existente = new IndicadorAgregadoMensal();
        existente.setCodigoBc(10813);
        existente.setAno(2024);
        existente.setMes(1);
        existente.setValorMedio(BigDecimal.valueOf(5.10));
        existente.setValorMaximo(BigDecimal.valueOf(5.50));
        existente.setValorMinimo(BigDecimal.valueOf(4.90));
        agregadoMensalRepository.save(existente);

        // Apenas IPCA médio para atualizar
        IndicadorEconomico ipca = new IndicadorEconomico();
        ipca.setNome("IPCA");
        ipca.setCodigoBc(10813);
        ipca.setValor(BigDecimal.valueOf(5.15));
        ipca.setData(LocalDate.of(2024,1,10));
        ipca.setFrequencia("MENSAL");
        ipca.setCreatedAt(OffsetDateTime.now());
        indicadorRepository.save(ipca);

        agregacaoService.calcularEsalvarAgregadosParaMes(mesReferencia);

        Optional<IndicadorAgregadoMensal> atualizado = agregadoMensalRepository
                .findByCodigoBcAndAnoAndMes(10813, 2024, 1);

        assertTrue(atualizado.isPresent());
        assertEquals(0, BigDecimal.valueOf(5.15).compareTo(atualizado.get().getValorMedio()));
    }

    @Test
    @DisplayName("Deve processar múltiplos indicadores mensais")
    void deveProcessarMultiplosIndicadoresMensais() {
        // Indicadores IPCA e INPC
        IndicadorEconomico ipca = new IndicadorEconomico();
        ipca.setNome("IPCA");
        ipca.setCodigoBc(10813);
        ipca.setValor(BigDecimal.valueOf(5.15));
        ipca.setData(LocalDate.of(2024,1,10));
        ipca.setFrequencia("MENSAL");
        ipca.setCreatedAt(OffsetDateTime.now());
        indicadorRepository.save(ipca);

        IndicadorEconomico inpc = new IndicadorEconomico();
        inpc.setNome("INPC");
        inpc.setCodigoBc(4390);
        inpc.setValor(BigDecimal.valueOf(11.75));
        inpc.setData(LocalDate.of(2024,1,12));
        inpc.setFrequencia("MENSAL");
        inpc.setCreatedAt(OffsetDateTime.now());
        indicadorRepository.save(inpc);

        agregacaoService.calcularEsalvarAgregadosParaMes(mesReferencia);

        List<IndicadorAgregadoMensal> saved = agregadoMensalRepository.findAll();
        assertTrue(saved.size() >= 2);

        // Verifica valores médios
        Optional<IndicadorAgregadoMensal> ipcaAgregado = agregadoMensalRepository
                .findByCodigoBcAndAnoAndMes(10813, 2024, 1);
        Optional<IndicadorAgregadoMensal> inpcAgregado = agregadoMensalRepository
                .findByCodigoBcAndAnoAndMes(4390, 2024, 1);

        assertEquals(0, BigDecimal.valueOf(5.15).compareTo(ipcaAgregado.get().getValorMedio()));
        assertEquals(0, BigDecimal.valueOf(11.75).compareTo(inpcAgregado.get().getValorMedio()));
    }

    @Test
    @DisplayName("Deve processar lista vazia de agregações mensais")
    void deveProcessarListaVaziaDeAgregacoesMensais() {
        indicadorRepository.deleteAll();

        agregacaoService.calcularEsalvarAgregadosParaMes(mesReferencia);

        List<IndicadorAgregadoMensal> saved = agregadoMensalRepository.findAll();
        assertTrue(saved.isEmpty());
    }

    @Test
    @DisplayName("Deve processar lista vazia de agregações anuais")
    void deveProcessarListaVaziaDeAgregacoesAnuais() {
        indicadorRepository.deleteAll();

        agregacaoService.calcularEsalvarAgregadosParaAno(anoReferencia);

        List<IndicadorAgregadoAnual> saved = agregadoAnualRepository.findAll();
        assertTrue(saved.isEmpty());
    }
}
