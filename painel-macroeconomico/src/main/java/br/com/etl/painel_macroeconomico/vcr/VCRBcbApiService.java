package br.com.etl.painel_macroeconomico.vcr;

import br.com.etl.painel_macroeconomico.dto.IndicadorEconomicoDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class VCRBcbApiService {

    private final ObjectMapper mapper;
    private final VCRService vcrService;
    private final HttpClient client = HttpClient.newHttpClient();
    private static final DateTimeFormatter BCB_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public VCRBcbApiService(ObjectMapper mapper, VCRService vcrService) {
        this.mapper = mapper;
        this.vcrService = vcrService;
    }

    public List<IndicadorEconomicoDTO> buscarDadosDaSerieComVCR(String nome, Integer codigoBc, String frequencia,
                                                                LocalDate dataInicial, LocalDate dataFinal,
                                                                String cassetteName, boolean recordMode) {
        try {
            String dataInicialStr = dataInicial.format(BCB_DATE_FORMATTER);
            String dataFinalStr = dataFinal.format(BCB_DATE_FORMATTER);

            String url = String.format(
                    "https://api.bcb.gov.br/dados/serie/bcdata.sgs.%d/dados?formato=json&dataInicial=%s&dataFinal=%s",
                    codigoBc, dataInicialStr, dataFinalStr
            );

            if (recordMode) {
                HttpRequest request = HttpRequest.newBuilder().uri(new URI(url)).GET().build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                String responseBody = response.body();
                int statusCode = response.statusCode();

                VCRInteraction interaction = new VCRInteraction("GET", url, "", statusCode, responseBody);
                VCRRecording recording = vcrService.loadCassette(cassetteName);
                recording.addInteraction(interaction);
                vcrService.saveCassette(cassetteName, recording.getInteractions());

                return parseResposta(responseBody, nome, codigoBc, frequencia);
            } else {
                List<VCRInteraction> interactions = vcrService.getInteractionsForUrl(cassetteName, url, "GET");
                if (interactions.isEmpty()) {
                    throw new RuntimeException("Nenhuma interação gravada encontrada para URL: " + url);
                }
                String recordedBody = interactions.get(0).getResponseBody();
                return parseResposta(recordedBody, nome, codigoBc, frequencia);
            }
        } catch (Exception e) {
            System.err.println("ERROR: [VCRBcbApiService] Falha ao processar VCR para código " + codigoBc);
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private List<IndicadorEconomicoDTO> parseResposta(String jsonBody, String nome, Integer codigoBc, String frequencia) throws Exception {
        JsonNode root = mapper.readTree(jsonBody);
        List<IndicadorEconomicoDTO> resultados = new ArrayList<>();

        if (root != null && root.isArray()) {
            for (JsonNode node : root) {
                if (node.has("valor") && node.has("data")) {
                    String valorStr = node.get("valor").asText();
                    if (valorStr == null || valorStr.isBlank()) {
                        continue;
                    }
                    resultados.add(new IndicadorEconomicoDTO(
                            nome,
                            codigoBc,
                            new BigDecimal(valorStr.replace(",", ".")),
                            LocalDate.parse(node.get("data").asText(), BCB_DATE_FORMATTER),
                            frequencia
                    ));
                }
            }
        }
        return resultados;
    }
}