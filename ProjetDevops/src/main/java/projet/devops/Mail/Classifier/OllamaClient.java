package projet.devops.Mail.Classifier;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OllamaClient {
    
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    
    public OllamaClient() {
        this("http://localhost:11434");
    }
    
    public OllamaClient(String baseUrl) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.baseUrl = baseUrl;
    }
    
    public String generateResponse(String model, String prompt) throws Exception {
        String jsonBody = String.format(
            "{\"model\": \"%s\", \"prompt\": %s, \"stream\": false}",
            model,
            objectMapper.writeValueAsString(prompt)
        );
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/api/generate"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        JsonNode json = objectMapper.readTree(response.body());
        
        if (json.has("error")) {
            throw new Exception("Erreur Ollama: " + json.get("error").asText());
        }
        
        if (!json.has("response")) {
            throw new Exception("Format de réponse Ollama invalide: champ 'response' absent");
        }
        
        return json.get("response").asText().trim();
    }
}