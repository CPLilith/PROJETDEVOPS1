package projet.devops.Mail.Model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Note {
    private String title;
    private String content;
    private String author;
    private String action; // Nouveau champ pour le tag (DO, PLAN, etc.)
    private String date;

    public Note() {}

    public Note(String title, String content, String author, String action) {
        this.title = title;
        this.content = content;
        this.author = author;
        this.action = action;
        this.date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM, yyyy"));
    }

    // Getters
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getAuthor() { return author; }
    public String getAction() { return action; }
    public String getDate() { return date; }
}