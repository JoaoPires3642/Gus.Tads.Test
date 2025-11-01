package br.com.etl.painel_macroeconomico.vcr;

import br.com.etl.painel_macroeconomico.dto.IndicadorEconomicoDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class VCRBcbApiServiceTest {

    @Autowired
    private VCRBcbApiService vcrBcbApiService;

    @Autowired
    private VCRService vcrService;

    @Test
    void testRecordAndPlaybackSelicMonthly() throws IOException {
        String cassetteName = "bcb_selic_jan_2024";
        Path cassettePath = Paths.get("src/test/resources/vcr_cassettes/" + cassetteName + ".json");

       
        if (Files.exists(cassettePath)) {
            Files.delete(cassettePath);
        }

       
        List<IndicadorEconomicoDTO> recorded = vcrBcbApiService.buscarDadosDaSerieComVCR(
                "Taxa Selic",
                4390,
                "Mensal",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 1, 31),
                cassetteName,
                true
        );

        assertNotNull(recorded);
        assertFalse(recorded.isEmpty(), "Deve retornar dados reais da API do BCB em modo gravação");
        assertTrue(vcrService.cassetteExists(cassetteName), "Cassete deve ser criado após gravação");

        VCRRecording recording = vcrService.loadCassette(cassetteName);
        assertFalse(recording.getInteractions().isEmpty(), "Cassete deve possuir pelo menos uma interação registrada");

      
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
        assertFalse(playback.isEmpty(), "Deve retornar dados do cassete em modo reprodução");
    }
}