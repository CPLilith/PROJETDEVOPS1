package projet.devops.Mail.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List; // On réutilise ton client
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import projet.devops.Mail.Classifier.OllamaClient;
import projet.devops.Mail.Classifier.Persona;
import projet.devops.Mail.Model.Note;

@Service
public class NoteService {
    private final OllamaClient ollamaClient = new OllamaClient();
    private List<Note> notes = new ArrayList<>();

    public void generateAiSynthesis(MultipartFile[] files, Persona persona) throws Exception {
        // 1. Concaténation brute pour le contexte de l'IA
        String rawData = Arrays.stream(files)
            .filter(f -> f.getOriginalFilename().endsWith(".md"))
            .map(f -> {
                try {
                    String author = f.getOriginalFilename().split("/")[0];
                    return "AUTEUR: " + author + "\nCONTENU: " + new String(f.getBytes(), StandardCharsets.UTF_8);
                } catch (Exception e) { return ""; }
            })
            .collect(Collectors.joining("\n\n---\n\n"));

        // 2. Le Prompt "Stratégique" (Modifié pour ton Master)
        String prompt = """
            Tu es un assistant de recherche expert. Voici des notes de différents auteurs.
            Fais une synthèse intelligente en suivant ces règles :
            1. Identifie qui a dit quoi (ex: "Alice note que...", "Bob souligne plutôt...").
            2. Regroupe par thématiques communes.
            3. Adopte un ton adapté au profil : %s.
            
            NOTES À TRAITER :
            %s
            
            SYNTHÈSE COMPARATIVE :
            """.formatted(persona.name(), rawData);

        // 3. Appel à Ollama (Modèle llama3.2 ou celui que tu utilises)
        String aiResponse = ollamaClient.generateResponse("tinyllama", prompt);

        // 4. Création de la note finale
        notes.add(0, new Note("Synthèse IA Comparative", aiResponse, "AI Orchestrator"));
    }

    public List<Note> getNotes() { return notes; }
}