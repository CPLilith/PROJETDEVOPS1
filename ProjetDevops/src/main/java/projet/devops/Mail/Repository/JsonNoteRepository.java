package projet.devops.Mail.Repository;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import projet.devops.Mail.Model.Note;

@Repository // SRP : Son seul rôle est de lire/écrire le fichier JSON des notes
public class JsonNoteRepository implements NoteRepository {

    private final String storagePath;
    private final ObjectMapper mapper;

    public JsonNoteRepository(
            @Value("${app.storage.notes:storage/notes_history.json}") String storagePath) {
        this.storagePath = storagePath;
        this.mapper = new ObjectMapper().copy().enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public List<Note> loadNotes() {
        try {
            File file = new File(storagePath);
            if (file.exists() && file.length() > 0) {
                Note[] loadedNotes = mapper.readValue(file, Note[].class);
                System.out.println("✅ [JsonNoteRepository] " + loadedNotes.length + " notes restaurées.");
                return new ArrayList<>(Arrays.asList(loadedNotes));
            }
        } catch (Exception e) {
            System.err.println("❌ [JsonNoteRepository] ERREUR DE LECTURE : " + e.getMessage());
        }
        return new ArrayList<>(); // Toujours renvoyer une liste vide, jamais null
    }

    @Override
    public void saveNotes(List<Note> notes) {
        try {
            File file = new File(storagePath);
            if (file.getParentFile() != null)
                file.getParentFile().mkdirs();
            mapper.writeValue(file, notes);
        } catch (Exception e) {
            System.err.println("❌ [JsonNoteRepository] ERREUR CRITIQUE ÉCRITURE : " + e.getMessage());
        }
    }
}