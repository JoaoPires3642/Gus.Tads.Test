package br.com.etl.painel_macroeconomico.service;

import br.com.etl.painel_macroeconomico.dto.ResultadoAgregacaoAnual;
import br.com.etl.painel_macroeconomico.dto.ResultadoAgregacaoMensal;
import br.com.etl.painel_macroeconomico.model.agregado.IndicadorAgregadoAnual;
import br.com.etl.painel_macroeconomico.model.agregado.IndicadorAgregadoMensal;
import br.com.etl.painel_macroeconomico.repository.IndicadorAgregadoAnualRepository;
import br.com.etl.painel_macroeconomico.repository.IndicadorAgregadoMensalRepository;
import br.com.etl.painel_macroeconomico.repository.IndicadorEconomicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgregacaoServiceTest {

    @Mock
    private IndicadorEconomicoRepository indicadorRepository;

    @Mock
    private IndicadorAgregadoMensalRepository agregadoMensalRepository;

    @Mock
    private IndicadorAgregadoAnualRepository agregadoAnualRepository;

    @InjectMocks
    private AgregacaoService agregacaoService;

    private LocalDate mesReferencia;
    private LocalDate anoReferencia;

    @BeforeEach
    void setUp() {
        mesReferencia = LocalDate.of(2024, 1, 15);
        anoReferencia = LocalDate.of(2024, 6, 1);
    }

    @Test
    @DisplayName("Deve calcular e salvar agregados mensais com sucesso")
    void deveCalcularESalvarAgregadosMensaisComSucesso() {
        // Arrange
        ResultadoAgregacaoMensal resultado = new ResultadoAgregacaoMensal(
                10813, 2024, 1, 5.15,
                BigDecimal.valueOf(5.45), BigDecimal.valueOf(4.89)
        );
        List<ResultadoAgregacaoMensal> resultados = Arrays.asList(resultado);

        when(indicadorRepository.calcularAgregadosMensais(any(), any()))
                .thenReturn(resultados);
        when(agregadoMensalRepository.findByCodigoBcAndAnoAndMes(anyInt(), anyInt(), anyInt()))
                .thenReturn(Optional.empty());

        // Act
        agregacaoService.calcularEsalvarAgregadosParaMes(mesReferencia);

        // Assert
        verify(indicadorRepository).calcularAgregadosMensais(
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 1, 31)
        );

        ArgumentCaptor<IndicadorAgregadoMensal> captor =
                ArgumentCaptor.forClass(IndicadorAgregadoMensal.class);
        verify(agregadoMensalRepository).save(captor.capture());

        IndicadorAgregadoMensal saved = captor.getValue();
        assertEquals(10813, saved.getCodigoBc());
        assertEquals(2024, saved.getAno());
        assertEquals(1, saved.getMes());
        assertEquals(0, BigDecimal.valueOf(5.15).compareTo(saved.getValorMedio()));
    }

    @Test
    @DisplayName("Deve atualizar agregado mensal existente")
    void deveAtualizarAgregadoMensalExistente() {
        // Arrange
        IndicadorAgregadoMensal existente = new IndicadorAgregadoMensal();
        existente.setId(1L);
        existente.setCodigoBc(10813);
        existente.setAno(2024);
        existente.setMes(1);

        ResultadoAgregacaoMensal resultado = new ResultadoAgregacaoMensal(
                10813, 2024, 1, 5.20,
                BigDecimal.valueOf(5.50), BigDecimal.valueOf(4.90)
        );

        when(indicadorRepository.calcularAgregadosMensais(any(), any()))
                .thenReturn(Arrays.asList(resultado));
        when(agregadoMensalRepository.findByCodigoBcAndAnoAndMes(10813, 2024, 1))
                .thenReturn(Optional.of(existente));

        // Act
        agregacaoService.calcularEsalvarAgregadosParaMes(mesReferencia);

        // Assert
        verify(agregadoMensalRepository).save(existente);
        assertEquals(0, BigDecimal.valueOf(5.20).compareTo(existente.getValorMedio()));
    }

    @Test
    @DisplayName("Deve calcular e salvar agregados anuais com sucesso")
    void deveCalcularESalvarAgregadosAnuaisComSucesso() {
        // Arrange
        ResultadoAgregacaoAnual resultado = new ResultadoAgregacaoAnual(
                10813, 2024, 5.15,
                BigDecimal.valueOf(5.45), BigDecimal.valueOf(4.89)
        );
        List<ResultadoAgregacaoAnual> resultados = Arrays.asList(resultado);

        when(indicadorRepository.calcularAgregadosAnuais(any(), any()))
                .thenReturn(resultados);
        when(agregadoAnualRepository.findByCodigoBcAndAno(anyInt(), anyInt()))
                .thenReturn(Optional.empty());

        // Act
        agregacaoService.calcularEsalvarAgregadosParaAno(anoReferencia);

        // Assert
        verify(indicadorRepository).calcularAgregadosAnuais(
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31)
        );

        ArgumentCaptor<IndicadorAgregadoAnual> captor =
                ArgumentCaptor.forClass(IndicadorAgregadoAnual.class);
        verify(agregadoAnualRepository).save(captor.capture());

        IndicadorAgregadoAnual saved = captor.getValue();
        assertEquals(10813, saved.getCodigoBc());
        assertEquals(2024, saved.getAno());
    }

    @Test
    @DisplayName("Deve processar múltiplos indicadores mensais")
    void deveProcessarMultiplosIndicadoresMensais() {
        // Arrange
        List<ResultadoAgregacaoMensal> resultados = Arrays.asList(
                new ResultadoAgregacaoMensal(10813, 2024, 1, 5.15,
                        BigDecimal.valueOf(5.45), BigDecimal.valueOf(4.89)),
                new ResultadoAgregacaoMensal(4390, 2024, 1, 11.75,
                        BigDecimal.valueOf(12.00), BigDecimal.valueOf(11.50))
        );

        when(indicadorRepository.calcularAgregadosMensais(any(), any()))
                .thenReturn(resultados);
        when(agregadoMensalRepository.findByCodigoBcAndAnoAndMes(anyInt(), anyInt(), anyInt()))
                .thenReturn(Optional.empty());

        // Act
        agregacaoService.calcularEsalvarAgregadosParaMes(mesReferencia);

        // Assert
        verify(agregadoMensalRepository, times(2)).save(any());
    }

    @Test
    @DisplayName("Deve processar lista vazia de agregações mensais")
    void deveProcessarListaVaziaDeAgregacoesMensais() {
        // Arrange
        when(indicadorRepository.calcularAgregadosMensais(any(), any()))
                .thenReturn(Arrays.asList());

        // Act
        agregacaoService.calcularEsalvarAgregadosParaMes(mesReferencia);

        // Assert
        verify(agregadoMensalRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve processar lista vazia de agregações anuais")
    void deveProcessarListaVaziaDeAgregacoesAnuais() {
        // Arrange
        when(indicadorRepository.calcularAgregadosAnuais(any(), any()))
                .thenReturn(Arrays.asList());

        // Act
        agregacaoService.calcularEsalvarAgregadosParaAno(anoReferencia);

        // Assert
        verify(agregadoAnualRepository, never()).save(any());
    }
}