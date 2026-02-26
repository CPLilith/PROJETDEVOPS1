package projet.devops.Mail.Service;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import projet.devops.Mail.Classifier.OllamaClient;
import projet.devops.Mail.Classifier.Persona;
import projet.devops.Mail.Model.Note;
import projet.devops.Mail.Repository.NoteRepository;

@Service
public class NoteService {

    private final OllamaClient ollamaClient;
    private final NoteRepository noteRepository; // DIP : On dépend de l'interface, pas du disque dur !

    private List<Note> notes;

    public NoteService(OllamaClient ollamaClient, NoteRepository noteRepository) {
        this.ollamaClient = ollamaClient;
        this.noteRepository = noteRepository;
    }

    @PostConstruct
    public void init() {
        // Le service ne se soucie pas de savoir d'où viennent les notes
        this.notes = noteRepository.loadNotes();
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
        noteRepository.saveNotes(notes); // Délégation de la sauvegarde
    }

    public void deleteNote(int index) {
        if (index >= 0 && index < notes.size()) {
            notes.remove(index);
            noteRepository.saveNotes(notes);
        }
    }

    public void updateNoteTag(int index, String newTag) {
        if (index >= 0 && index < notes.size()) {
            notes.get(index).setAction(newTag.toUpperCase());
            noteRepository.saveNotes(notes);
        }
    }

    public List<Note> getNotes() {
        return notes;
    }

    // SRP : Cette méthode pourrait même, à terme, être extraite dans un
    // "MarkdownParserService"
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
}