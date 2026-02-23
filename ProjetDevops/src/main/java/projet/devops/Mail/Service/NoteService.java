package projet.devops.Mail.Service;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
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

    private final OllamaClient ollamaClient;
    private final String storagePath;

    private List<Note> notes = new ArrayList<>();
    private final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    // Injection de l'IA ET du chemin de fichier (avec une valeur par défaut de
    // sécurité)
    public NoteService(OllamaClient ollamaClient,
            @Value("${app.storage.notes:storage/notes_history.json}") String storagePath) {
        this.ollamaClient = ollamaClient;
        this.storagePath = storagePath;
    }

    @PostConstruct
    public void init() {
        try {
            File file = new File(storagePath);
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
            this.notes = new ArrayList<>();
        }
    }

    public void generateAiKnowledge(MultipartFile[] files, Persona persona) throws Exception {
        String rawContext = extractRawData(files);
        if (rawContext.isEmpty())
            return;

        String synthesisPrompt = "En tant que " + persona.name()
                + ", fais une synthèse comparative intelligente de ces notes. " +
                "Identifie qui a écrit quoi et les points clés :\n" + rawContext;
        String aiSynthesis = ollamaClient.generateResponse("tinyllama", synthesisPrompt);

        String tagPrompt = "Sur la base de cette synthèse, choisis UN SEUL mot parmi : DO, PLAN, DELEGATE, DELETE.\nTexte : "
                + aiSynthesis;
        String eisenhowerTag = ollamaClient.generateResponse("tinyllama", tagPrompt).trim().toUpperCase();

        if (!Arrays.asList("DO", "PLAN", "DELEGATE", "DELETE").contains(eisenhowerTag)) {
            eisenhowerTag = "PENDING";
        }

        Note newNote = new Note("Intelligence Collective (" + persona.name() + ")", aiSynthesis, "AI Orchestrator",
                eisenhowerTag);
        notes.add(0, newNote);
        saveNoteToJson();
    }

    public void deleteNote(int index) {
        if (index >= 0 && index < notes.size()) {
            notes.remove(index);
            saveNoteToJson();
            System.out.println("🗑️ [Persistence] Note supprimée et historique mis à jour.");
        }
    }

    private void saveNoteToJson() {
        try {
            File file = new File(storagePath);
            if (file.getParentFile() != null)
                file.getParentFile().mkdirs();

            mapper.writeValue(file, notes);
            System.out.println("💾 [Persistence] Fichier écrit avec succès à : " + file.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("❌ ERREUR CRITIQUE ÉCRITURE : " + e.getMessage());
        }
    }

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
            note.setAction(newTag.toUpperCase());
            saveNoteToJson();
            System.out.println("✅ [Persistence] Tag de la note " + index + " mis à jour en : " + newTag);
        }
    }
}