package br.com.etl.painel_macroeconomico.vcr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**Configuração para testes VCR*/
@TestConfiguration
public class TestVCRConfiguration {

    @Bean
    @Primary
    public ObjectMapper testObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    @Bean
    public VCRService vcrService() {
        return new VCRService();
    }

    @Bean
    public VCRBcbApiService vcrBcbApiService(ObjectMapper objectMapper, VCRService vcrService) {
        return new VCRBcbApiService(objectMapper, vcrService);
    }
}