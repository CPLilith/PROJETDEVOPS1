package projet.devops.Mail.Model;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

// Cette annotation empêche le plantage si le JSON contient des champs inconnus
@JsonIgnoreProperties(ignoreUnknown = true)
public class Note {

    private String id;
    private String title;
    private String author;
    
    // On s'assure que Jackson map bien le champ "content" du JSON vers cette variable
    @JsonProperty("content") 
    private String content;
    
    private String action;

    // Constructeur vide OBLIGATOIRE pour Jackson (lecture du JSON)
    public Note() {
        // Si l'ID n'existe pas dans le JSON (vieille note), on en génère un à la volée
        this.id = UUID.randomUUID().toString();
    }

    // Constructeur complet
    public Note(String title, String author, String content, String action) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.author = author;
        this.content = content;
        this.action = action;
    }

    // --- Getters & Setters ---
    public String getId() {
        // Sécurité : si l'ID est null (vieux JSON), on en donne un
        if (id == null) id = UUID.randomUUID().toString();
        return id;
    }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
}