package br.com.etl.painel_macroeconomico;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import br.com.etl.painel_macroeconomico.service.PublisherService;

@SpringBootApplication
@EnableBatchProcessing
@EnableScheduling 
public class PainelMacroeconomicoApplication {
  
    public static void main(String[] args) {
        SpringApplication.run(PainelMacroeconomicoApplication.class, args);
        /*   var context = SpringApplication.run(PainelMacroeconomicoApplication.class, args);
        PublisherService publisherService = context.getBean(PublisherService.class);
        publisherService.publicarUltimosDezAnos("IPCA", 10844, "Mensal");
        // Usar para testar a publicação inicial, quando for  "diario"  usar o "publicarIndicador" inves do "publicarUltimosDezAnos"
        */
    }

}
