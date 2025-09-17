package br.com.etl.painel_macroeconomico.service;

import static br.com.etl.painel_macroeconomico.config.RabbitConfig.EXCHANGE;
import static br.com.etl.painel_macroeconomico.config.RabbitConfig.ROUTING_KEY;
import br.com.etl.painel_macroeconomico.dto.IndicadorEconomicoDTO;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class PublisherService {

 private final ObjectMapper mapper;
    private final RabbitTemplate rabbitTemplate;
    private final BcbApiClientService bcbApiClient;
    private static final long MAX_YEARS_INTERVAL = 10; // tempo maximo aceito pelo BCB

    // 2. RECEBA O ObjectMapper GERENCIADO PELO SPRING VIA CONSTRUTOR
     public PublisherService(RabbitTemplate rabbitTemplate, ObjectMapper mapper, BcbApiClientService bcbApiClient) {
        this.rabbitTemplate = rabbitTemplate;
        this.mapper = mapper; 
        this.bcbApiClient = bcbApiClient;
    }

   
    public void publicarUltimosDezAnos(String nome, Integer codigoBc, String frequencia) {
        LocalDate dataFinal = LocalDate.now();
        LocalDate dataInicial = dataFinal.minusYears(MAX_YEARS_INTERVAL);
        // Chama o método principal com o período calculado
        this.publicarIndicador(nome, codigoBc, frequencia, dataInicial, dataFinal);
    }

    /**
     * Busca os dados de indicadores econômicos do BCB para um período específico e publica cada entrada na fila.
     * O período é limitado a no máximo 10 anos que é o maximo que o BCB aceita .
     */
    public void publicarIndicador(String nome, Integer codigoBc, String frequencia, LocalDate dataInicial, LocalDate dataFinal) {
        try {
            // Validação e ajuste do período para no máximo 10 anos
            if (dataInicial.isAfter(dataFinal)) {
                System.err.println("ERROR:[PublisherService] A data inicial não pode ser posterior à data final.");
                return;
            }

            if (ChronoUnit.YEARS.between(dataInicial, dataFinal) > MAX_YEARS_INTERVAL) {
                dataInicial = dataFinal.minusYears(MAX_YEARS_INTERVAL);
                System.out.println("INFO:[PublisherService] O período solicitado excedia 10 anos. A data inicial foi ajustada.");
            }
                
            List<IndicadorEconomicoDTO> dados = bcbApiClient.buscarDadosDaSerie(nome, codigoBc, frequencia, dataInicial, dataFinal);

            
            System.out.println("INFO: [PublisherService] Publicando " + dados.size() + " mensagens para o indicador '" + nome + "'");
            for (IndicadorEconomicoDTO dto : dados) {
                String mensagemJson = mapper.writeValueAsString(dto);
                rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, mensagemJson);
            }
            System.out.println("INFO: [PublisherService] Publicação concluída para o indicador '" + nome + "'");

        } catch (Exception e) {
            System.err.println("ERROR: [PublisherService] Falha ao orquestrar a publicação do indicador " + nome);
            e.printStackTrace();
        }
    }
}