package projet.devops.Mail.Classifier;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import projet.devops.Mail.Mail;

public class MailProcessor {

    public enum EisenhowerTag {
        URGENT_IMPORTANT,              // Q1
        IMPORTANT,                     // Q2
        URGENT,                        // Q3
        NON_URGENT_NON_IMPORTANT      // Q4
    }

    public static Mail tagMail(Mail mail, List<String> importanceWords, List<String> urgencyWords) {
        String text = (mail.getFrom() + " " + mail.getSubject() + " " + mail.getContent()).toLowerCase();
        
        try {
            EisenhowerTag tag = callOllamaForTag(text);
            return mail.withTag(tag);
        } catch (Exception e) {
            // Fallback sur l'ancienne méthode si Ollama échoue
            System.err.println("Erreur Ollama, utilisation du fallback: " + e.getMessage());
            
            boolean importance = importanceWords.stream().anyMatch(text::contains);
            boolean urgency = urgencyWords.stream().anyMatch(text::contains);

            EisenhowerTag tag;
            if (importance && urgency) tag = EisenhowerTag.URGENT_IMPORTANT;
            else if (importance) tag = EisenhowerTag.IMPORTANT;
            else if (urgency) tag = EisenhowerTag.URGENT;
            else tag = EisenhowerTag.NON_URGENT_NON_IMPORTANT;

            return mail.withTag(tag);
        }
    }

    private static EisenhowerTag callOllamaForTag(String mailText) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        
        // Nettoyer le texte de l'email
        String cleanedText = mailText
            .replaceAll("\\r\\n", " ")  // Remplacer \r\n par espace
            .replaceAll("\\n", " ")     // Remplacer \n par espace
            .replaceAll("\\r", " ")     // Remplacer \r par espace
            .replaceAll("\"", "'")      // Remplacer " par '
            .replaceAll("\\s+", " ")    // Réduire les espaces multiples
            .substring(0, Math.min(500, mailText.length())); // Limiter à 500 caractères
        
        String prompt = "Analyse cet email et détermine son quadrant Eisenhower. "
            + "Réponds UNIQUEMENT par: URGENT_IMPORTANT, IMPORTANT, URGENT ou NON_URGENT_NON_IMPORTANT\\n"
            + "URGENT_IMPORTANT = Urgent et Important (à faire immédiatement)\\n"
            + "IMPORTANT = Important mais pas Urgent (à planifier)\\n"
            + "URGENT = Urgent mais pas Important (à déléguer)\\n"
            + "NON_URGENT_NON_IMPORTANT = Ni Urgent ni Important (à éliminer)\\n\\n"
            + "Email: " + cleanedText;
        
        // Construire le JSON manuellement pour éviter les problèmes d'échappement
        ObjectMapper mapper = new ObjectMapper();
        String escapedPrompt = mapper.writeValueAsString(prompt);
        
        String jsonBody = String.format(
            "{\"model\": \"tinyllama:latest\", \"prompt\": %s, \"stream\": false}",
            escapedPrompt
        );
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:11434/api/generate"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        // Debug: afficher la réponse brute
        System.out.println("Réponse Ollama brute: " + response.body());
        
        JsonNode json = mapper.readTree(response.body());
        
        // Vérifier si c'est une erreur
        if (json.has("error")) {
            throw new Exception("Erreur Ollama: " + json.get("error").asText());
        }
        
        // Vérifier si le champ "response" existe
        if (json.has("response")) {
            String ollamaResponse = json.get("response").asText().trim().toUpperCase();
            System.out.println("Réponse parsée: " + ollamaResponse);
            
            if (ollamaResponse.contains("URGENT_IMPORTANT")) return EisenhowerTag.URGENT_IMPORTANT;
            if (ollamaResponse.contains("NON_URGENT_NON_IMPORTANT")) return EisenhowerTag.NON_URGENT_NON_IMPORTANT;
            if (ollamaResponse.contains("IMPORTANT")) return EisenhowerTag.IMPORTANT;
            if (ollamaResponse.contains("URGENT")) return EisenhowerTag.URGENT;
        } else {
            System.err.println("Le champ 'response' est absent du JSON: " + response.body());
            throw new Exception("Format de réponse Ollama invalide");
        }
        
        return EisenhowerTag.NON_URGENT_NON_IMPORTANT;
    }
}