package br.com.etl.painel_macroeconomico.service;

import static br.com.etl.painel_macroeconomico.config.RabbitConfig.EXCHANGE;
import static br.com.etl.painel_macroeconomico.config.RabbitConfig.ROUTING_KEY;
import br.com.etl.painel_macroeconomico.dto.IndicadorEconomicoDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Service
public class PublisherService {

    private final HttpClient client = HttpClient.newHttpClient();
    // 1. APENAS DECLARE O ObjectMapper, NÃO CRIE UMA NOVA INSTÂNCIA
    private final ObjectMapper mapper;
    private final RabbitTemplate rabbitTemplate;

    private static final DateTimeFormatter BCB_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final long MAX_YEARS_INTERVAL = 10;

    // 2. RECEBA O ObjectMapper GERENCIADO PELO SPRING VIA CONSTRUTOR
    public PublisherService(RabbitTemplate rabbitTemplate, ObjectMapper mapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.mapper = mapper; // O Spring injetará a instância configurada corretamente
    }

    /**
     * Busca os dados de uma série econômica do Banco Central para os últimos 10 anos e os publica na fila.
     * Este é o método que o seu AgendadorCapturaService deve chamar.
     */
    public void publicarUltimosDezAnos(String nome, Integer codigoBc, String frequencia) {
        LocalDate dataFinal = LocalDate.now();
        LocalDate dataInicial = dataFinal.minusYears(MAX_YEARS_INTERVAL);
        // Chama o método principal com o período calculado
        this.publicarIndicador(nome, codigoBc, frequencia, dataInicial, dataFinal);
    }

    /**
     * Busca os dados de uma série econômica do BCB para um período específico e publica cada entrada na fila.
     * O período é limitado a no máximo 10 anos.
     */
    public void publicarIndicador(String nome, Integer codigoBc, String frequencia, LocalDate dataInicial, LocalDate dataFinal) {
        try {
            // Validação e ajuste do período para no máximo 10 anos
            if (dataInicial.isAfter(dataFinal)) {
                System.err.println("ERROR: A data inicial não pode ser posterior à data final.");
                return;
            }

            if (ChronoUnit.YEARS.between(dataInicial, dataFinal) > MAX_YEARS_INTERVAL) {
                dataInicial = dataFinal.minusYears(MAX_YEARS_INTERVAL);
                System.out.println("INFO: O período solicitado excedia 10 anos. A data inicial foi ajustada para " + dataInicial.format(BCB_DATE_FORMATTER));
            }

            String dataInicialStr = dataInicial.format(BCB_DATE_FORMATTER);
            String dataFinalStr = dataFinal.format(BCB_DATE_FORMATTER);

            String url = String.format(
                    "https://api.bcb.gov.br/dados/serie/bcdata.sgs.%d/dados?formato=json&dataInicial=%s&dataFinal=%s",
                    codigoBc, dataInicialStr, dataFinalStr
            );

            System.out.println("INFO: Consultando URL: " + url);

            HttpRequest request = HttpRequest.newBuilder().uri(new URI(url)).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JsonNode root = mapper.readTree(response.body());

            if (root != null && root.isArray()) {
                System.out.println("INFO: " + root.size() + " registros encontrados para o código " + codigoBc);
                for (JsonNode node : root) {
                    if (node.has("valor") && node.has("data")) {
                        String valorStr = node.get("valor").asText();

                        if (valorStr == null || valorStr.isBlank()) {
                            System.err.println("WARN: Nó JSON com valor vazio ignorado: " + node);
                            continue;
                        }

                        IndicadorEconomicoDTO dto = new IndicadorEconomicoDTO(
                                nome,
                                codigoBc,
                                new BigDecimal(valorStr.replace(",", ".")),
                                LocalDate.parse(node.get("data").asText(), BCB_DATE_FORMATTER),
                                frequencia
                        );

                        // A mágica acontece aqui: o 'mapper' injetado sabe como lidar com LocalDate
                        String mensagemJson = mapper.writeValueAsString(dto);
                        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, mensagemJson);
                    } else {
                        System.err.println("WARN: Nó JSON ignorado por não conter 'valor' ou 'data': " + node);
                    }
                }
                System.out.println("INFO: Publicação concluída para o código " + codigoBc);
            } else {
                System.err.println("WARN: Resposta inesperada ou vazia do BCB para o código " + codigoBc + ": " + response.body());
            }
        } catch (Exception e) {
            System.err.println("ERROR: Falha ao publicar indicador " + nome + " (codigo " + codigoBc + ")");
            e.printStackTrace();
        }
    }
}
