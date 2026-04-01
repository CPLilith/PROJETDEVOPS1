package projet.devops.Mail.Service;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import projet.devops.Mail.Classifier.OllamaClient;
import projet.devops.Mail.Model.Note;
import projet.devops.Mail.Model.Persona;
import projet.devops.Mail.Repository.NoteRepository;

@Service
public class NoteService {

    private final OllamaClient ollamaClient;
    private final NoteRepository noteRepository;

    private List<Note> notes;

    public NoteService(OllamaClient ollamaClient, NoteRepository noteRepository) {
        this.ollamaClient = ollamaClient;
        this.noteRepository = noteRepository;
    }

    @PostConstruct
    public void init() {
        this.notes = noteRepository.loadNotes();
    }

    /**
     * Importe des fichiers .md directement comme notes brutes.
     * Chaque fichier devient une note séparée, sans traitement IA.
     */
    public void importMarkdownFiles(MultipartFile[] files) {
        for (MultipartFile f : files) {
            try {
                if (f.getOriginalFilename() != null && f.getOriginalFilename().endsWith(".md")) {
                    String content = new String(f.getBytes(), StandardCharsets.UTF_8);
                    String title = f.getOriginalFilename().replace(".md", "");
                    Note note = new Note(title, "Import", content, "PENDING");
                    notes.add(0, note);
                }
            } catch (Exception e) {
                System.err.println("Erreur import fichier : " + f.getOriginalFilename());
            }
        }
        noteRepository.saveNotes(notes);
    }

    /**
     * Fusionne les notes sélectionnées (index de la vue inversée) via l'IA.
     */
    public void mergeSelectedNotes(List<Integer> reversedIndexes, Persona persona) throws Exception {
        int total = notes.size();
        List<Note> selectedNotes = reversedIndexes.stream()
                .map(i -> (total - 1) - i)
                .filter(i -> i >= 0 && i < total)
                .map(notes::get)
                .collect(Collectors.toList());

        if (selectedNotes.isEmpty()) return;

        StringBuilder rawContext = new StringBuilder();
        for (Note n : selectedNotes) {
            rawContext.append("\n--- NOTE : ").append(n.getTitle()).append(" ---\n");
            rawContext.append(n.getContent()).append("\n");
        }

        String synthesisPrompt = "En tant que " + persona.name()
                + ", fais une synthèse comparative intelligente de ces notes. "
                + "Identifie les points clés et les liens entre elles :\n" + rawContext;

        String aiSynthesis = ollamaClient.generateResponse("tinyllama", synthesisPrompt);

        String tagPrompt = "Sur la base de cette synthèse, choisis UN SEUL mot parmi : DO, PLAN, DELEGATE, DELETE.\nTexte : "
                + aiSynthesis;
        String eisenhowerTag = ollamaClient.generateResponse("tinyllama", tagPrompt).trim().toUpperCase();

        if (!Arrays.asList("DO", "PLAN", "DELEGATE", "DELETE").contains(eisenhowerTag)) {
            eisenhowerTag = "PENDING";
        }

        String mergedTitles = selectedNotes.stream().map(Note::getTitle).collect(Collectors.joining(", "));
        Note newNote = new Note("Synthèse IA : " + mergedTitles, "AI Orchestrator", aiSynthesis, eisenhowerTag);
        notes.add(0, newNote);
        noteRepository.saveNotes(notes);
    }

    // Ancienne méthode conservée pour compatibilité
    public void generateAiKnowledge(MultipartFile[] files, Persona persona) throws Exception {
        importMarkdownFiles(files);
        int count = files.length;
        List<Integer> reversedIndexes = java.util.stream.IntStream.range(0, count)
                .boxed().collect(Collectors.toList());
        mergeSelectedNotes(reversedIndexes, persona);
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
}