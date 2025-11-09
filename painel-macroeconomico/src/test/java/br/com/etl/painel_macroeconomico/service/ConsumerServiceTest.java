package br.com.etl.painel_macroeconomico.service;

import br.com.etl.painel_macroeconomico.dto.IndicadorEconomicoDTO;
import br.com.etl.painel_macroeconomico.model.IndicadorEconomico;
import br.com.etl.painel_macroeconomico.repository.IndicadorEconomicoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsumerServiceTest {

    @Mock
    private IndicadorEconomicoRepository repository;

    @Mock
    private ObjectMapper mapper;

    @InjectMocks
    private ConsumerService consumerService;

    private IndicadorEconomicoDTO dto;
    private String mensagemJson;

    @BeforeEach
    void setUp() {
        dto = new IndicadorEconomicoDTO(
                "Dólar Americano",
                10813,
                BigDecimal.valueOf(5.15),
                LocalDate.of(2024, 1, 15),
                "Diária"
        );
        mensagemJson = "{\"nome\":\"Dólar Americano\",\"codigoBc\":10813," +
                "\"valor\":5.15,\"data\":\"2024-01-15\",\"frequencia\":\"Diária\"}";
    }

    @Test
    @DisplayName("Deve consumir e salvar nova mensagem com sucesso")
    void deveConsumirESalvarNovaMensagemComSucesso() throws Exception {
        // Arrange
        when(mapper.readValue(mensagemJson, IndicadorEconomicoDTO.class))
                .thenReturn(dto);
        when(repository.findByCodigoBcAndData(dto.getCodigoBc(), dto.getData()))
                .thenReturn(Optional.empty());

        // Act
        consumerService.consumirMensagem(mensagemJson);

        // Assert
        verify(repository).findByCodigoBcAndData(10813, LocalDate.of(2024, 1, 15));

        ArgumentCaptor<IndicadorEconomico> captor =
                ArgumentCaptor.forClass(IndicadorEconomico.class);
        verify(repository).save(captor.capture());

        IndicadorEconomico saved = captor.getValue();
        assertEquals("Dólar Americano", saved.getNome());
        assertEquals(10813, saved.getCodigoBc());
        assertEquals(0, BigDecimal.valueOf(5.15).compareTo(saved.getValor()));
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    @DisplayName("Deve ignorar mensagem duplicada")
    void deveIgnorarMensagemDuplicada() throws Exception {
        // Arrange
        IndicadorEconomico existente = new IndicadorEconomico();
        existente.setId(1L);
        existente.setCodigoBc(10813);
        existente.setData(LocalDate.of(2024, 1, 15));

        when(mapper.readValue(mensagemJson, IndicadorEconomicoDTO.class))
                .thenReturn(dto);
        when(repository.findByCodigoBcAndData(dto.getCodigoBc(), dto.getData()))
                .thenReturn(Optional.of(existente));

        // Act
        consumerService.consumirMensagem(mensagemJson);

        // Assert
        verify(repository).findByCodigoBcAndData(10813, LocalDate.of(2024, 1, 15));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lidar com erro de parsing JSON")
    void deveLidarComErroDeParsingJson() throws Exception {
        // Arrange
        String jsonInvalido = "{invalid json}";
        when(mapper.readValue(jsonInvalido, IndicadorEconomicoDTO.class))
                .thenThrow(new RuntimeException("JSON inválido"));

        // Act & Assert
        assertDoesNotThrow(() -> consumerService.consumirMensagem(jsonInvalido));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve converter DTO para Entidade corretamente")
    void deveConverterDtoParaEntidadeCorretamente() throws Exception {
        // Arrange
        when(mapper.readValue(mensagemJson, IndicadorEconomicoDTO.class))
                .thenReturn(dto);
        when(repository.findByCodigoBcAndData(any(), any()))
                .thenReturn(Optional.empty());

        // Act
        consumerService.consumirMensagem(mensagemJson);

        // Assert
        ArgumentCaptor<IndicadorEconomico> captor =
                ArgumentCaptor.forClass(IndicadorEconomico.class);
        verify(repository).save(captor.capture());

        IndicadorEconomico entity = captor.getValue();
        assertEquals(dto.getNome(), entity.getNome());
        assertEquals(dto.getCodigoBc(), entity.getCodigoBc());
        assertEquals(dto.getValor(), entity.getValor());
        assertEquals(dto.getData(), entity.getData());
        assertEquals(dto.getFrequencia(), entity.getFrequencia());
    }

    @Test
    @DisplayName("Deve processar múltiplas mensagens sequencialmente")
    void deveProcessarMultiplasMensagensSequencialmente() throws Exception {
        // Arrange
        when(mapper.readValue(anyString(), eq(IndicadorEconomicoDTO.class)))
                .thenReturn(dto);
        when(repository.findByCodigoBcAndData(any(), any()))
                .thenReturn(Optional.empty());

        // Act
        consumerService.consumirMensagem(mensagemJson);
        consumerService.consumirMensagem(mensagemJson);
        consumerService.consumirMensagem(mensagemJson);

        // Assert
        verify(repository, times(3)).findByCodigoBcAndData(any(), any());
    }

    @Test
    @DisplayName("Deve definir createdAt ao salvar nova entidade")
    void deveDefinirCreatedAtAoSalvarNovaEntidade() throws Exception {
        // Arrange
        when(mapper.readValue(mensagemJson, IndicadorEconomicoDTO.class))
                .thenReturn(dto);
        when(repository.findByCodigoBcAndData(any(), any()))
                .thenReturn(Optional.empty());

        // Act
        consumerService.consumirMensagem(mensagemJson);

        // Assert
        ArgumentCaptor<IndicadorEconomico> captor =
                ArgumentCaptor.forClass(IndicadorEconomico.class);
        verify(repository).save(captor.capture());

        assertNotNull(captor.getValue().getCreatedAt());
    }
}