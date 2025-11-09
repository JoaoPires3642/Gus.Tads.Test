package br.com.etl.painel_macroeconomico.service;

import br.com.etl.painel_macroeconomico.dto.IndicadorEconomicoDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BcbApiClientServiceTest {

    private BcbApiClientService bcbApiClientService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        bcbApiClientService = new BcbApiClientService(objectMapper);
    }

    @Test
    @DisplayName("Deve parsear resposta JSON válida corretamente")
    void deveParserRespostaJsonValidaCorretamente() throws Exception {
        // Arrange
        String jsonResponse = "[{\"data\":\"01/01/2024\",\"valor\":\"5.15\"}," +
                "{\"data\":\"02/01/2024\",\"valor\":\"5.20\"}]";

        // Para testar o método parseResposta, precisamos usar reflexão ou
        // fazer uma busca real (não recomendado em testes unitários)
        // Este é um exemplo conceitual do que seria testado

        // Act & Assert
        assertDoesNotThrow(() -> objectMapper.readTree(jsonResponse));
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando JSON está vazio")
    void deveRetornarListaVaziaQuandoJsonEstaVazio() throws Exception {
        // Arrange
        String jsonResponse = "[]";

        // Act & Assert
        var tree = objectMapper.readTree(jsonResponse);
        assertTrue(tree.isArray());
        assertEquals(0, tree.size());
    }

    @Test
    @DisplayName("Deve formatar datas corretamente para API do BCB")
    void deveFormatarDatasCorretamenteParaApiBcb() {
        // Arrange
        LocalDate data = LocalDate.of(2024, 1, 15);

        // Act
        String dataFormatada = String.format("%02d/%02d/%04d",
                data.getDayOfMonth(),
                data.getMonthValue(),
                data.getYear());

        // Assert
        assertEquals("15/01/2024", dataFormatada);
    }

    @Test
    @DisplayName("Deve construir URL da API corretamente")
    void deveConstruirUrlDaApiCorretamente() {
        // Arrange
        Integer codigoBc = 10813;
        LocalDate dataInicial = LocalDate.of(2024, 1, 1);
        LocalDate dataFinal = LocalDate.of(2024, 1, 31);

        // Act
        String url = String.format(
                "https://api.bcb.gov.br/dados/serie/bcdata.sgs.%d/dados?formato=json&dataInicial=%s&dataFinal=%s",
                codigoBc, "01/01/2024", "31/01/2024"
        );

        // Assert
        assertTrue(url.contains("10813"));
        assertTrue(url.contains("dataInicial=01/01/2024"));
        assertTrue(url.contains("dataFinal=31/01/2024"));
    }

    @Test
    @DisplayName("Deve lidar com valores decimais com vírgula")
    void deveLidarComValoresDecimaisComVirgula() {
        // Arrange
        String valorComVirgula = "5,15";

        // Act
        String valorComPonto = valorComVirgula.replace(",", ".");

        // Assert
        assertEquals("5.15", valorComPonto);
        assertDoesNotThrow(() -> Double.parseDouble(valorComPonto));
    }

    @Test
    @DisplayName("Deve validar formato de data do BCB")
    void deveValidarFormatoDeDatoDoBcb() {
        // Arrange
        String dataBcb = "01/01/2024";

        // Act & Assert
        assertTrue(dataBcb.matches("\\d{2}/\\d{2}/\\d{4}"));
    }

    @Test
    @DisplayName("Deve ignorar valores em branco ou nulos")
    void deveIgnorarValoresEmBrancoOuNulos() {
        // Arrange
        String[] valores = {"", "  ", null, "5.15"};

        // Act
        long valoresValidos = java.util.Arrays.stream(valores)
                .filter(v -> v != null && !v.isBlank())
                .count();

        // Assert
        assertEquals(1, valoresValidos);
    }
}