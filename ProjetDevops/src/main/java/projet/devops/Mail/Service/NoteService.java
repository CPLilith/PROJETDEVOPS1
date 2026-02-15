package projet.devops.Mail.Service;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import jakarta.annotation.PostConstruct;
import projet.devops.Mail.Classifier.OllamaClient;
import projet.devops.Mail.Classifier.Persona;
import projet.devops.Mail.Model.Note;

@Service
public class NoteService {
    private final OllamaClient ollamaClient = new OllamaClient();
    private List<Note> notes = new ArrayList<>();
    private final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    
    // CORRECTION : On enregistre à la racine du projet pour que ce soit lu à chaque fois
    private final String STORAGE_PATH = "storage/notes_history.json";

    /**
     * CHARGEMENT AUTOMATIQUE : S'exécute dès que l'application démarre.
     */

    @PostConstruct
    public void init() {
        try {
            File file = new File(STORAGE_PATH);
            System.out.println("🔍 [Persistence] Recherche du fichier à : " + file.getAbsolutePath());

            if (file.exists() && file.length() > 0) {
                Note[] loadedNotes = mapper.readValue(file, Note[].class);
                this.notes = new ArrayList<>(Arrays.asList(loadedNotes));
                System.out.println("✅ [Persistence] " + notes.size() + " notes restaurées avec succès.");
            } else {
                System.out.println("ℹ️ [Persistence] Fichier vide ou inexistant. Liste initialisée à vide.");
                this.notes = new ArrayList<>();
            }
        } catch (Exception e) {
            System.err.println("❌ [Persistence] ERREUR DE LECTURE : " + e.getMessage());
            e.printStackTrace(); // Pour voir l'erreur exacte dans la console
            this.notes = new ArrayList<>();
        }
    }

    /**
     * DOUBLE ANALYSE IA : Synthèse comparative + Classification Eisenhower.
     */
    public void generateAiKnowledge(MultipartFile[] files, Persona persona) throws Exception {
        // 1. Extraction des données des fichiers .md
        String rawContext = extractRawData(files);
        if (rawContext.isEmpty()) return;

        // 2. PREMIER PASSAGE : Synthèse IA
        String synthesisPrompt = "En tant que " + persona.name() + ", fais une synthèse comparative intelligente de ces notes. " +
                                 "Identifie qui a écrit quoi et les points clés :\n" + rawContext;
        String aiSynthesis = ollamaClient.generateResponse("tinyllama", synthesisPrompt);

        // 3. DEUXIÈME PASSAGE : Classification Eisenhower
        String tagPrompt = "Sur la base de cette synthèse, choisis UN SEUL mot parmi : DO, PLAN, DELEGATE, DELETE.\nTexte : " + aiSynthesis;
        String eisenhowerTag = ollamaClient.generateResponse("tinyllama", tagPrompt).trim().toUpperCase();
        
        // Nettoyage au cas où l'IA ferait une phrase
        if (!Arrays.asList("DO", "PLAN", "DELEGATE", "DELETE").contains(eisenhowerTag)) {
            eisenhowerTag = "PENDING";
        }

        // 4. CRÉATION, AJOUT ET SAUVEGARDE
        Note newNote = new Note("Intelligence Collective (" + persona.name() + ")", aiSynthesis, "AI Orchestrator", eisenhowerTag);
        notes.add(0, newNote); 
        saveNoteToJson();
    }

    /**
     * SUPPRESSION : Retire une note et met à jour le fichier JSON.
     */
    public void deleteNote(int index) {
        if (index >= 0 && index < notes.size()) {
            notes.remove(index);
            saveNoteToJson(); 
            System.out.println("🗑️ [Persistence] Note supprimée et historique mis à jour.");
        }
    }

    /**
     * PERSISTANCE : Écrit la liste complète dans le fichier.
     */
    private void saveNoteToJson() {
        try {
            File file = new File(STORAGE_PATH);
            if (file.getParentFile() != null) file.getParentFile().mkdirs();
            
            System.out.println("DEBUG : Tentative d'écriture de " + notes.size() + " notes...");
            mapper.writeValue(file, notes);
            System.out.println("💾 [Persistence] Fichier écrit avec succès à : " + file.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("❌ ERREUR CRITIQUE ÉCRITURE : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * UTILITAIRE : Lit le contenu des fichiers Markdown.
     */
    private String extractRawData(MultipartFile[] files) {
        StringBuilder sb = new StringBuilder();
        for (MultipartFile f : files) {
            try {
                if (f.getOriginalFilename() != null && f.getOriginalFilename().endsWith(".md")) {
                    sb.append("\n--- SOURCE : ").append(f.getOriginalFilename()).append(" ---\n");
                    sb.append(new String(f.getBytes(), StandardCharsets.UTF_8)).append("\n");
                }
            } catch (Exception e) {
                sb.append("[Erreur de lecture sur ").append(f.getOriginalFilename()).append("]");
            }
        }
        return sb.toString();
    }

    public List<Note> getNotes() {
        return notes;
    }

    public void updateNoteTag(int index, String newTag) {
    if (index >= 0 && index < notes.size()) {
        Note note = notes.get(index);
        // On met à jour le tag (on s'assure qu'il est en majuscules)
        note.setAction(newTag.toUpperCase()); 
        saveNoteToJson(); // Persistance immédiate
        System.out.println("✅ [Persistence] Tag de la note " + index + " mis à jour en : " + newTag);
    }
}
}