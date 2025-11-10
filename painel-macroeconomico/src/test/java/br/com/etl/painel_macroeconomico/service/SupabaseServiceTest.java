package br.com.etl.painel_macroeconomico.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SupabaseServiceTest {

    private SupabaseService supabaseService;
    private HttpClient mockHttpClient;
    private HttpResponse<String> mockResponse;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        supabaseService = new SupabaseService();

        // Injeta valores simulados (já que @Value não é carregado fora do Spring)
        supabaseService.supabaseUrl = "https://fake.supabase.co";
        supabaseService.supabaseKey = "fake-key";
        supabaseService.bucket = "test-bucket";

        // Mock do HttpClient e HttpResponse
        mockHttpClient = mock(HttpClient.class);
        mockResponse = mock(HttpResponse.class);

        // Substitui o HttpClient real por um mock
        supabaseService.setHttpClient(mockHttpClient);
    }

    @Test
    @DisplayName("Deve fazer upload com sucesso (status 200)")
    void deveFazerUploadComSucesso() throws Exception {
        JsonNode json = objectMapper.readTree("{\"teste\":\"ok\"}");

        when(mockResponse.statusCode()).thenReturn(200);
        when(mockHttpClient.send(
                ArgumentMatchers.any(HttpRequest.class),
                ArgumentMatchers.<HttpResponse.BodyHandler<String>>any())
        ).thenReturn(mockResponse);

        assertDoesNotThrow(() -> supabaseService.uploadJson(json));
    }

    @Test
    @DisplayName("Deve lançar exceção ao falhar upload (status 500)")
    void deveLancarExcecaoAoFalharUpload() throws Exception {
        JsonNode json = objectMapper.readTree("{\"erro\":\"simulado\"}");

        when(mockResponse.statusCode()).thenReturn(500);
        when(mockResponse.body()).thenReturn("Erro interno");
        when(mockHttpClient.send(
                ArgumentMatchers.any(HttpRequest.class),
                ArgumentMatchers.<HttpResponse.BodyHandler<String>>any())
        ).thenReturn(mockResponse);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> supabaseService.uploadJson(json));
        assertTrue(ex.getMessage().contains("Upload Supabase falhou"));
    }

    @Test
    @DisplayName(" Deve construir corretamente o URI de upload")
    void deveConstruirCorretamenteOUri() throws Exception {
        JsonNode json = objectMapper.readTree("{\"teste\":\"uri\"}");

        when(mockResponse.statusCode()).thenReturn(200);
        when(mockHttpClient.send(any(), any())).thenAnswer(invocation -> {
            HttpRequest req = invocation.getArgument(0);
            URI uri = req.uri();

            assertTrue(uri.toString().startsWith("https://fake.supabase.co/storage/v1/object/test-bucket/indicadores/"));
            assertTrue(uri.toString().endsWith(".json"));

            return mockResponse;
        });

        assertDoesNotThrow(() -> supabaseService.uploadJson(json));
    }
}
