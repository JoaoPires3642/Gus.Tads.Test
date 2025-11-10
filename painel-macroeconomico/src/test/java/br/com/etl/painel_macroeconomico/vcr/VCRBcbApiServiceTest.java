package br.com.etl.painel_macroeconomico.vcr;

import br.com.etl.painel_macroeconomico.dto.IndicadorEconomicoDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ContextConfiguration(classes = TestVCRConfiguration.class)
class VCRBcbApiServiceTest {

    @Autowired
    private VCRBcbApiService vcrBcbApiService;

    @Autowired
    private VCRService vcrService;

    @Test
    @DisplayName("Deve gravar e reproduzir dados da API do BCB usando VCR")
    void testRecordAndPlaybackSelicMonthly() throws IOException {
        String cassetteName = "bcb_selic_jan_2024";
        Path cassettePath = Paths.get("src/test/resources/vcr_cassettes/" + cassetteName + ".json");

        // Limpa cassete se existir para forçar nova gravação
        if (Files.exists(cassettePath)) {
            Files.delete(cassettePath);
        }

        // GRAVAÇÃO: Faz chamada REAL à API do BCB
        List<IndicadorEconomicoDTO> recorded = vcrBcbApiService.buscarDadosDaSerieComVCR(
                "Taxa Selic",
                4390,
                "Mensal",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 1, 31),
                cassetteName,
                true  // true = modo gravação (chama API real)
        );

        // Validações da gravação
        assertNotNull(recorded, "Dados gravados não devem ser nulos");
        assertFalse(recorded.isEmpty(), "Deve retornar dados reais da API do BCB em modo gravação");
        assertTrue(vcrService.cassetteExists(cassetteName), "Cassete deve ser criado após gravação");

        VCRRecording recording = vcrService.loadCassette(cassetteName);
        assertFalse(recording.getInteractions().isEmpty(), "Cassete deve possuir pelo menos uma interação registrada");

        // REPRODUÇÃO: Usa dados do cassete (sem chamar API)
        List<IndicadorEconomicoDTO> playback = vcrBcbApiService.buscarDadosDaSerieComVCR(
                "Taxa Selic",
                4390,
                "Mensal",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 1, 31),
                cassetteName,
                false  // false = modo reprodução (usa cassete)
        );

        // Validações da reprodução
        assertNotNull(playback, "Dados reproduzidos não devem ser nulos");
        assertFalse(playback.isEmpty(), "Deve retornar dados do cassete em modo reprodução");
        assertEquals(recorded.size(), playback.size(), "Gravação e reprodução devem ter mesma quantidade de dados");
    }

    @Test
    @DisplayName("Deve usar cassete existente sem fazer nova chamada")
    void testUsarCassetteExistente() throws IOException {
        String cassetteName = "bcb_selic_jan_2024";

        // Verifica se cassete existe (deve existir do teste anterior)
        if (!vcrService.cassetteExists(cassetteName)) {
            // Se não existe, pula este teste
            System.out.println("Cassete não existe. Execute o teste de gravação primeiro.");
            return;
        }

        // Usa apenas dados do cassete (sem chamar API)
        List<IndicadorEconomicoDTO> playback = vcrBcbApiService.buscarDadosDaSerieComVCR(
                "Taxa Selic",
                4390,
                "Mensal",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 1, 31),
                cassetteName,
                false
        );

        assertNotNull(playback);
        assertFalse(playback.isEmpty(), "Deve retornar dados do cassete");
    }
}