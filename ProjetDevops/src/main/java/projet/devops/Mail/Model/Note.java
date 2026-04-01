package projet.devops.Mail.Model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Note {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private String id;
    private String title;
    private String author;

    @JsonProperty("content")
    private String content;

    private String action;

    // "IMPORT" ou "MERGE"
    private String sourceType;

    // Date de soumission au format "dd/MM/yyyy HH:mm"
    private String submittedAt;

    public Note() {
        this.id = UUID.randomUUID().toString();
    }

    public Note(String title, String author, String content, String action, String sourceType) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.author = author;
        this.content = content;
        this.action = action;
        this.sourceType = sourceType;
        this.submittedAt = LocalDateTime.now().format(FORMATTER);
    }

    // Ancien constructeur conservé pour compatibilité (notes existantes sans date)
    public Note(String title, String author, String content, String action) {
        this(title, author, content, action, "IMPORT");
    }

    public String getId() {
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

    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }

    public String getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(String submittedAt) { this.submittedAt = submittedAt; }
}