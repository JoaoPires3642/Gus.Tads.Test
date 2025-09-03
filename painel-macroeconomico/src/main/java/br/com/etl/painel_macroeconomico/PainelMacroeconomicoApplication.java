package br.com.etl.painel_macroeconomico;
import br.com.etl.painel_macroeconomico.service.SupabaseService;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@SpringBootApplication
@EnableBatchProcessing
public class PainelMacroeconomicoApplication implements CommandLineRunner {

    @Autowired
    private SupabaseService supabaseService;

    public static void main(String[] args) {
        SpringApplication.run(PainelMacroeconomicoApplication.class, args);
    }

    public void run(String... args) throws Exception {
        ObjectNode node = new ObjectMapper().createObjectNode();
        node.put("teste", "Conexão Supabase OK");
        supabaseService.uploadJson(node);
    }
}



