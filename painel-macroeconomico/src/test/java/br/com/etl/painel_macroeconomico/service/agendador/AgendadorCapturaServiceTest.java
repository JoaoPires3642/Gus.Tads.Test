package br.com.etl.painel_macroeconomico.service.agendador;

import br.com.etl.painel_macroeconomico.model.Indicador;
import br.com.etl.painel_macroeconomico.service.PublisherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgendadorCapturaServiceTest {

    @Mock
    private PublisherService publisherService;

    @InjectMocks
    private AgendadorCapturaService agendadorCapturaService;

    @Test
    @DisplayName("Deve chamar publisher para todos os indicadores diários")
    void deveChamarPublisherParaTodosIndicadoresDiarios() {
        // Act
        agendadorCapturaService.capturarIndicadoresAgendados();

        // Assert
        // Conta quantos indicadores são diários
        long indicadoresDiarios = java.util.Arrays.stream(Indicador.values())
                .filter(i -> "Diária".equalsIgnoreCase(i.getFrequencia()))
                .count();

        verify(publisherService, atLeast((int) indicadoresDiarios))
                .publicarIndicador(anyString(), anyInt(), anyString(),
                        any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    @DisplayName("Deve processar indicadores sem lançar exceção")
    void deveProcessarIndicadoresSemLancarExcecao() {
        // Arrange
        doNothing().when(publisherService)
                .publicarIndicador(anyString(), anyInt(), anyString(),
                        any(LocalDate.class), any(LocalDate.class));

        // Act & Assert
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(
                () -> agendadorCapturaService.capturarIndicadoresAgendados()
        );
    }

    @Test
    @DisplayName("Deve continuar processamento mesmo com erro em um indicador")
    void deveContinuarProcessamentoMesmoComErroEmUmIndicador() {
        // Arrange
        doThrow(new RuntimeException("Erro simulado"))
                .doNothing()
                .when(publisherService)
                .publicarIndicador(anyString(), anyInt(), anyString(),
                        any(LocalDate.class), any(LocalDate.class));

        // Act & Assert
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(
                () -> agendadorCapturaService.capturarIndicadoresAgendados()
        );
    }
}