package br.com.etl.painel_macroeconomico.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

class IndicadorTest {

    @Test
    @DisplayName("Deve conter todos os indicadores esperados")
    void deveConterTodosIndicadoresEsperados() {
        // Assert
        assertTrue(Indicador.values().length >= 8,
                "Deve haver pelo menos 8 indicadores cadastrados");
    }

    @ParameterizedTest
    @EnumSource(Indicador.class)
    @DisplayName("Cada indicador deve ter nome amigável não nulo")
    void cadaIndicadorDeveTerNomeAmigavelNaoNulo(Indicador indicador) {
        assertNotNull(indicador.getNomeAmigavel());
        assertFalse(indicador.getNomeAmigavel().isBlank());
    }

    @ParameterizedTest
    @EnumSource(Indicador.class)
    @DisplayName("Cada indicador deve ter código SGS válido")
    void cadaIndicadorDeveTerCodigoSgsValido(Indicador indicador) {
        assertTrue(indicador.getCodigoSgs() > 0,
                "Código SGS deve ser positivo para " + indicador.name());
    }

    @ParameterizedTest
    @EnumSource(Indicador.class)
    @DisplayName("Cada indicador deve ter frequência válida")
    void cadaIndicadorDeveTerFrequenciaValida(Indicador indicador) {
        String frequencia = indicador.getFrequencia();
        assertNotNull(frequencia);
        assertTrue(
                frequencia.equals("Diária") ||
                        frequencia.equals("Mensal") ||
                        frequencia.equals("Anual"),
                "Frequência inválida para " + indicador.name()
        );
    }

    @Test
    @DisplayName("Deve ter indicador DOLAR com código correto")
    void deveTerIndicadorDolarComCodigoCorreto() {
        assertEquals(10813, Indicador.DOLAR.getCodigoSgs());
        assertEquals("Diária", Indicador.DOLAR.getFrequencia());
    }

    @Test
    @DisplayName("Deve ter indicador SELIC com código correto")
    void deveTerIndicadorSelicComCodigoCorreto() {
        assertEquals(4390, Indicador.SELIC.getCodigoSgs());
        assertEquals("Diária", Indicador.SELIC.getFrequencia());
    }

    @Test
    @DisplayName("Deve ter indicador IPCA com código correto")
    void deveTerIndicadorIpcaComCodigoCorreto() {
        assertEquals(10844, Indicador.IPCA.getCodigoSgs());
        assertEquals("Mensal", Indicador.IPCA.getFrequencia());
    }

    @Test
    @DisplayName("Códigos SGS devem ser únicos")
    void codigosSgsDevemSerUnicos() {
        long codigosUnicos = java.util.Arrays.stream(Indicador.values())
                .map(Indicador::getCodigoSgs)
                .distinct()
                .count();

        assertEquals(Indicador.values().length, codigosUnicos,
                "Todos os códigos SGS devem ser únicos");
    }

    @Test
    @DisplayName("Deve ter indicadores de câmbio")
    void deveTerIndicadoresDeCambio() {
        boolean temDolar = java.util.Arrays.stream(Indicador.values())
                .anyMatch(i -> i == Indicador.DOLAR);
        boolean temEuro = java.util.Arrays.stream(Indicador.values())
                .anyMatch(i -> i == Indicador.EURO);

        assertTrue(temDolar, "Deve ter indicador de Dólar");
        assertTrue(temEuro, "Deve ter indicador de Euro");
    }

    @Test
    @DisplayName("Deve ter indicadores de juros e inflação")
    void deveTerIndicadoresDeJurosEInflacao() {
        boolean temSelic = java.util.Arrays.stream(Indicador.values())
                .anyMatch(i -> i == Indicador.SELIC);
        boolean temIpca = java.util.Arrays.stream(Indicador.values())
                .anyMatch(i -> i == Indicador.IPCA);

        assertTrue(temSelic, "Deve ter indicador Selic");
        assertTrue(temIpca, "Deve ter indicador IPCA");
    }

    @Test
    @DisplayName("Deve ter indicadores de atividade econômica")
    void deveTerIndicadoresDeAtividadeEconomica() {
        boolean temIbcBr = java.util.Arrays.stream(Indicador.values())
                .anyMatch(i -> i == Indicador.IBC_BR);
        boolean temCaged = java.util.Arrays.stream(Indicador.values())
                .anyMatch(i -> i == Indicador.CAGED);

        assertTrue(temIbcBr, "Deve ter indicador IBC-Br");
        assertTrue(temCaged, "Deve ter indicador CAGED");
    }

    @Test
    @DisplayName("Nomes amigáveis devem ser descritivos")
    void nomesAmigaveisDevemSerDescritivos() {
        for (Indicador indicador : Indicador.values()) {
            assertTrue(indicador.getNomeAmigavel().length() > 5,
                    "Nome amigável de " + indicador.name() + " deve ser descritivo");
        }
    }
}