package br.com.etl.painel_macroeconomico;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import br.com.etl.painel_macroeconomico.service.AgregacaoService;

@SpringBootApplication
@EnableBatchProcessing
@EnableScheduling 
public class PainelMacroeconomicoApplication {
  
    public static void main(String[] args) {
            var context = SpringApplication.run(PainelMacroeconomicoApplication.class, args);
            /* 
        PublisherService publisherService = context.getBean(PublisherService.class);
        publisherService.publicarUltimosDezAnos("Taxa Selic", 4390, "Mensal");*/

        // Usar para testar a publicação inicial, quando for  "diario"  usar o "publicarIndicador" inves do "publicarUltimosDezAnos"
       // publisherService.publicarIndicador("Taxa Selic", 11, "Diaria", java.time.LocalDate.of(2025, 1, 1), java.time.LocalDate.now()); */

        /* Usar para testar o agendamento de agregação */
        AgregacaoService agregacaoService = context.getBean(AgregacaoService.class);
        agregacaoService.calcularEsalvarAgregadosParaAno(java.time.LocalDate.now().minusYears(1)); // Testa para o ano anterior
       // agregacaoService.calcularEsalvarAgregadosParaMes(java.time.LocalDate.now().minusMonths(1)); // Testa para o mês anterior 
    }

}


