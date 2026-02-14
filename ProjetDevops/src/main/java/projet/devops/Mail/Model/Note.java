package projet.devops.Mail.Model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Note {
    private String title;
    private String content;
    private String author; // Nouveau champ
    private String date;

    public Note(String title, String content, String author) {
        this.title = title;
        this.content = content;
        this.author = author;
        this.date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM, yyyy"));
    }

    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getAuthor() { return author; }
    public String getDate() { return date; }
}