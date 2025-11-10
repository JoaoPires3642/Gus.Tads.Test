package br.com.etl.painel_macroeconomico.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Service
public class SupabaseService {

    @Value("${supabase.url}")
    protected String supabaseUrl;

    @Value("${supabase.service_key}")
    protected String supabaseKey;

    @Value("${supabase.bucket}")
    protected String bucket;

    private HttpClient http = HttpClient.newHttpClient();

    // ✅ permite injetar um mock no teste
    public void setHttpClient(HttpClient httpClient) {
        this.http = httpClient;
    }

    // ✅ getter pra teste (se quiser usar)
    public HttpClient getHttpClient() {
        return http;
    }

    public void uploadJson(JsonNode json) throws Exception {
        String path = "indicadores/" + System.currentTimeMillis() + ".json";
        URI uri = new URI(supabaseUrl + "/storage/v1/object/" + bucket + "/" + path);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("apikey", supabaseKey)
                .header("Authorization", "Bearer " + supabaseKey)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json.toString(), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> resp = http.send(request, HttpResponse.BodyHandlers.ofString());

        if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
            System.out.println("Upload Supabase OK: " + path);
        } else {
            System.err.println("Falha no upload: " + resp.statusCode() + " -> " + resp.body());
            throw new RuntimeException("Upload Supabase falhou");
        }
    }
}
