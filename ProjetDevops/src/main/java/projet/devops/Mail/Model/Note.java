package projet.devops.Mail.Model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Note {
    private String title;
    private String content;
    private String author;
    private String action;
    private String date;

    /**
     * CONSTRUCTEUR VIDE : Indispensable pour que Jackson 
     * puisse recréer l'objet depuis le JSON.
     */
    public Note() {}

    /**
     * CONSTRUCTEUR COMPLET : Utilisé pour créer les nouvelles synthèses.
     */
    public Note(String title, String content, String author, String action) {
        this.title = title;
        this.content = content;
        this.author = author;
        this.action = action;
        this.date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM, yyyy"));
    }

    // --- GETTERS (Pour l'affichage Thymeleaf) ---
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getAuthor() { return author; }
    public String getAction() { return action; }
    public String getDate() { return date; }

    // --- SETTERS (Requis pour Jackson et la modification manuelle) ---
    
    // Cette méthode règle ton erreur "cannot find symbol"
    public void setAction(String action) { 
        this.action = action; 
    }

    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
    public void setAuthor(String author) { this.author = author; }
    public void setDate(String date) { this.date = date; }
    
}