package projet.devops.Mail.Repository;

import java.util.List;
import projet.devops.Mail.Model.Note;

public interface NoteRepository {
    List<Note> loadNotes();

    void saveNotes(List<Note> notes);
}