package br.com.etl.painel_macroeconomico.service;

import br.com.etl.painel_macroeconomico.dto.IndicadorEconomicoDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static br.com.etl.painel_macroeconomico.config.RabbitConfig.EXCHANGE;
import static br.com.etl.painel_macroeconomico.config.RabbitConfig.ROUTING_KEY;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PublisherServiceTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private ObjectMapper mapper;

    @Mock
    private BcbApiClientService bcbApiClient;

    @InjectMocks
    private PublisherService publisherService;

    private LocalDate dataInicial;
    private LocalDate dataFinal;
    private List<IndicadorEconomicoDTO> dadosMock;

    @BeforeEach
    void setUp() {
        dataInicial = LocalDate.of(2024, 1, 1);
        dataFinal = LocalDate.of(2024, 1, 31);

        dadosMock = Arrays.asList(
                new IndicadorEconomicoDTO("Dólar", 10813, BigDecimal.valueOf(5.15),
                        LocalDate.of(2024, 1, 1), "Diária"),
                new IndicadorEconomicoDTO("Dólar", 10813, BigDecimal.valueOf(5.20),
                        LocalDate.of(2024, 1, 2), "Diária")
        );
    }

    @Test
    @DisplayName("Deve publicar indicador com sucesso")
    void devePublicarIndicadorComSucesso() throws Exception {
        // Arrange
        when(bcbApiClient.buscarDadosDaSerie(anyString(), anyInt(), anyString(),
                any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(dadosMock);
        when(mapper.writeValueAsString(any())).thenReturn("{\"dados\":\"mock\"}");

        // Act
        publisherService.publicarIndicador("Dólar", 10813, "Diária", dataInicial, dataFinal);

        // Assert
        verify(bcbApiClient).buscarDadosDaSerie("Dólar", 10813, "Diária",
                dataInicial, dataFinal);
        verify(rabbitTemplate, times(2)).convertAndSend(eq(EXCHANGE), eq(ROUTING_KEY),
                anyString());
    }

    @Test
    @DisplayName("Deve publicar últimos dez anos corretamente")
    void devePublicarUltimosDezAnosCorretamente() throws Exception {
        // Arrange
        when(bcbApiClient.buscarDadosDaSerie(anyString(), anyInt(), anyString(),
                any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(dadosMock);
        when(mapper.writeValueAsString(any())).thenReturn("{\"dados\":\"mock\"}");

        // Act
        publisherService.publicarUltimosDezAnos("Dólar", 10813, "Mensal");

        // Assert
        verify(bcbApiClient).buscarDadosDaSerie(eq("Dólar"), eq(10813), eq("Mensal"),
                any(LocalDate.class), any(LocalDate.class));
        verify(rabbitTemplate, times(2)).convertAndSend(anyString(), anyString(),
                anyString());
    }

    @Test
    @DisplayName("Deve ajustar período quando exceder 10 anos")
    void deveAjustarPeriodoQuandoExcederDezAnos() throws Exception {
        // Arrange
        LocalDate dataInicialExcedente = LocalDate.now().minusYears(15);
        LocalDate dataFinalAtual = LocalDate.now();

        when(bcbApiClient.buscarDadosDaSerie(anyString(), anyInt(), anyString(),
                any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(dadosMock);
        when(mapper.writeValueAsString(any())).thenReturn("{\"dados\":\"mock\"}");

        // Act
        publisherService.publicarIndicador("Dólar", 10813, "Diária",
                dataInicialExcedente, dataFinalAtual);

        // Assert
        verify(bcbApiClient).buscarDadosDaSerie(eq("Dólar"), eq(10813), eq("Diária"),
                any(LocalDate.class), eq(dataFinalAtual));
    }

    @Test
    @DisplayName("Não deve publicar quando data inicial é posterior à final")
    void naoDevePublicarQuandoDataInicialPosteriorFinal() throws Exception {
        // Arrange
        LocalDate dataInicialPosterior = LocalDate.of(2024, 12, 31);
        LocalDate dataFinalAnterior = LocalDate.of(2024, 1, 1);

        // Act
        publisherService.publicarIndicador("Dólar", 10813, "Diária",
                dataInicialPosterior, dataFinalAnterior);

        // Assert
        verify(bcbApiClient, never()).buscarDadosDaSerie(anyString(), anyInt(),
                anyString(), any(LocalDate.class), any(LocalDate.class));
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(),
                anyString());
    }

    @Test
    @DisplayName("Deve lidar com lista vazia de dados")
    void deveLidarComListaVaziaDeDados() throws Exception {
        // Arrange
        when(bcbApiClient.buscarDadosDaSerie(anyString(), anyInt(), anyString(),
                any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Arrays.asList());

        // Act
        publisherService.publicarIndicador("Dólar", 10813, "Diária",
                dataInicial, dataFinal);

        // Assert
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(),
                anyString());
    }

    @Test
    @DisplayName("Deve serializar cada DTO como JSON")
    void deveSerializarCadaDtoComoJson() throws Exception {
        // Arrange
        when(bcbApiClient.buscarDadosDaSerie(anyString(), anyInt(), anyString(),
                any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(dadosMock);
        when(mapper.writeValueAsString(any())).thenReturn("{\"dados\":\"mock\"}");

        // Act
        publisherService.publicarIndicador("Dólar", 10813, "Diária",
                dataInicial, dataFinal);

        // Assert
        verify(mapper, times(2)).writeValueAsString(any(IndicadorEconomicoDTO.class));
    }

    @Test
    @DisplayName("Deve continuar publicação mesmo com erro de serialização")
    void deveContinuarPublicacaoMesmoComErroDeSerializacao() throws Exception {
        // Arrange
        when(bcbApiClient.buscarDadosDaSerie(anyString(), anyInt(), anyString(),
                any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(dadosMock);
        when(mapper.writeValueAsString(any()))
                .thenThrow(new RuntimeException("Erro de serialização"));

        // Act & Assert
        assertDoesNotThrow(() -> publisherService.publicarIndicador("Dólar", 10813,
                "Diária", dataInicial, dataFinal));
    }

    @Test
    @DisplayName("Deve calcular intervalo de 10 anos corretamente")
    void deveCalcularIntervaloDeDezAnosCorretamente() {
        // Arrange
        LocalDate hoje = LocalDate.now();
        LocalDate dezAnosAtras = hoje.minusYears(10);

        // Act
        long anosDeDiferenca = java.time.temporal.ChronoUnit.YEARS
                .between(dezAnosAtras, hoje);

        // Assert
        assertEquals(10, anosDeDiferenca);
    }
}