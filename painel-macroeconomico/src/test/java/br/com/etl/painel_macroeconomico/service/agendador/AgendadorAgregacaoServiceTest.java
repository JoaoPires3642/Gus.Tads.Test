package br.com.etl.painel_macroeconomico.service.agendador;

import br.com.etl.painel_macroeconomico.model.Indicador;
import br.com.etl.painel_macroeconomico.service.AgregacaoService;
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
class AgendadorAgregacaoServiceTest {

    @Mock
    private AgregacaoService agregacaoService;

    @InjectMocks
    private AgendadorAgregacaoService agendadorAgregacaoService;

    @Test
    @DisplayName("Deve executar agregação mensal")
    void deveExecutarAgregacaoMensal() {
        // Arrange
        doNothing().when(agregacaoService)
                .calcularEsalvarAgregadosParaMes(any(LocalDate.class));

        // Act
        agendadorAgregacaoService.executarAgregacaoDiaria();

        // Assert
        verify(agregacaoService, atLeastOnce())
                .calcularEsalvarAgregadosParaMes(any(LocalDate.class));
    }

    @Test
    @DisplayName("Deve processar agregação sem erros")
    void deveProcessarAgregacaoSemErros() {
        // Arrange
        doNothing().when(agregacaoService)
                .calcularEsalvarAgregadosParaMes(any(LocalDate.class));

        // Act & Assert
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(
                () -> agendadorAgregacaoService.executarAgregacaoDiaria()
        );
    }

    @Test
    @DisplayName("Deve lidar com erro durante agregação")
    void deveLidarComErroDuranteAgregacao() {
        // Arrange
        doThrow(new RuntimeException("Erro na agregação"))
                .when(agregacaoService)
                .calcularEsalvarAgregadosParaMes(any(LocalDate.class));

        // Act & Assert
        org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> agendadorAgregacaoService.executarAgregacaoDiaria()
        );
    }
}