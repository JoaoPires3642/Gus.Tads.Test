package br.com.etl.painel_macroeconomico.service;

import br.com.etl.painel_macroeconomico.dto.IndicadorEconomicoDTO;
import br.com.etl.painel_macroeconomico.model.IndicadorEconomico;
import br.com.etl.painel_macroeconomico.repository.IndicadorEconomicoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class ConsumerService {

    private final IndicadorEconomicoRepository repository;
    // 1. APENAS DECLARE O ObjectMapper
    private final ObjectMapper mapper;

    // 2. RECEBA O ObjectMapper GERENCIADO PELO SPRING VIA CONSTRUTOR
    public ConsumerService(IndicadorEconomicoRepository repository, ObjectMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @RabbitListener(queues = "painelQueue") // Escuta a fila
    public void consumirMensagem(String mensagemJson) {
        try {
            // O 'mapper' injetado sabe como lidar com LocalDate ao ler o JSON
            IndicadorEconomicoDTO dto = mapper.readValue(mensagemJson, IndicadorEconomicoDTO.class);
            System.out.println("INFO: Mensagem recebida -> " + dto);

            // Lógica para EVITAR DUPLICATAS
            repository.findByCodigoBcAndData(dto.getCodigoBc(), dto.getData())
                .ifPresentOrElse(
                    (indicadorExistente) -> {
                        System.out.println("INFO: Dado já existente para " + dto.getNome() + " na data " + dto.getData() + ". Ignorando.");
                    },
                    () -> {
                        IndicadorEconomico novaEntidade = toEntity(dto);
                        repository.save(novaEntidade);
                        System.out.println("SUCCESS: Novo dado salvo -> " + novaEntidade);
                    }
                );

        } catch (Exception e) {
            System.err.println("ERROR: Falha ao processar mensagem da fila.");
            e.printStackTrace();
        }
    }

    /**
     * Converte um objeto DTO para uma Entidade JPA salva no banco.
     */
    private IndicadorEconomico toEntity(IndicadorEconomicoDTO dto) {
        IndicadorEconomico entity = new IndicadorEconomico();
        entity.setNome(dto.getNome());
        entity.setCodigoBc(dto.getCodigoBc());
        entity.setValor(dto.getValor());
        entity.setData(dto.getData());
        entity.setFrequencia(dto.getFrequencia());
        entity.setCreatedAt(OffsetDateTime.now()); // Define a data de criação
        return entity;
    }
}
