package br.com.etl.painel_macroeconomico.service;

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
public class BcbApiClientService {

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper;
    private static final DateTimeFormatter BCB_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    public BcbApiClientService(ObjectMapper mapper) {
        this.mapper = mapper;
    }

   
     // Busca os dados de uma série na API do BCB Conforme o Enum Indicador.
 
    public List<IndicadorEconomicoDTO> buscarDadosDaSerie(String nome, Integer codigoBc, String frequencia, LocalDate dataInicial, LocalDate dataFinal) {
        try {
            String dataInicialStr = dataInicial.format(BCB_DATE_FORMATTER);
            String dataFinalStr = dataFinal.format(BCB_DATE_FORMATTER);

            String url = String.format(
                    "https://api.bcb.gov.br/dados/serie/bcdata.sgs.%d/dados?formato=json&dataInicial=%s&dataFinal=%s",
                    codigoBc, dataInicialStr, dataFinalStr
            );
            System.out.println("INFO: [BcbApiClientService] Consultando URL: " + url);

            HttpRequest request = HttpRequest.newBuilder().uri(new URI(url)).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            return parseResposta(response.body(), nome, codigoBc, frequencia);
            
        } catch (Exception e) {
            System.err.println("ERROR: [BcbApiClientService] Falha ao consultar API do BCB para o código " + codigoBc);
            e.printStackTrace();
            return Collections.emptyList(); 
        }
    }

    private List<IndicadorEconomicoDTO> parseResposta(String jsonBody, String nome, Integer codigoBc, String frequencia) throws Exception {
        JsonNode root = mapper.readTree(jsonBody);
        List<IndicadorEconomicoDTO> resultados = new ArrayList<>();

        if (root != null && root.isArray()) {
            System.out.println("INFO: [BcbApiClientService] " + root.size() + " registros encontrados para o código " + codigoBc);
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
