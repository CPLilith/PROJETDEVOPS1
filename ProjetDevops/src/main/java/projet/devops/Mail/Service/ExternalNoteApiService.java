package projet.devops.Mail.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import projet.devops.Mail.Model.Note;

@Service
public class ExternalNoteApiService {

    private final RestTemplate restTemplate;
    
    // On passe le token en final car il est injecté par le constructeur
    private final String notionToken; 

    @Value("${notion.api.url}")
    private String notionUrl;

    @Value("${notion.api.version}")
    private String notionVersion;

    @Value("${notion.parent.page.id}")
    private String parentPageId;

    /**
     * Le constructeur utilise @Qualifier("notionToken") pour dire à Spring :
     * "Va chercher le Bean String que nous avons créé dans CredentialsConfig"
     */
    public ExternalNoteApiService(@Qualifier("notionToken") String notionToken) {
        this.notionToken = notionToken;
        this.restTemplate = new RestTemplate();
    }

    // --- HELPER : HEADERS ---
    private HttpHeaders getNotionHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(notionToken);
        headers.set("Notion-Version", notionVersion);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    // --- FEATURE : LISTER LES DOSSIERS (BREADCRUMBS) ---
    public List<String> fetchExistingBreadcrumbs() {
        List<String> breadcrumbs = new ArrayList<>();
        try {
            breadcrumbs.add("Projets/DevOps");
            breadcrumbs.add("Notes/Réunions");
            breadcrumbs.add("Veille/IA");
            return breadcrumbs;
        } catch (Exception e) {
            System.err.println("❌ Erreur lecture catégories Notion : " + e.getMessage());
            return breadcrumbs;
        }
    }

    // --- FEATURE : CRÉER UNE NOTE SUR NOTION ---
    public boolean createNoteInExternalApp(String breadcrumb, String title, String content, String date) {
        try {
            String createUrl = notionUrl + "/pages";

            String sanitizedContent = content.replace("\"", "\\\"").replace("\n", "\\n");

            String jsonBody = """
            {
              "parent": { "page_id": "%s" },
              "properties": {
                "title": [
                  { "text": { "content": "%s" } }
                ]
              },
              "children": [
                {
                  "object": "block",
                  "type": "heading_2",
                  "heading_2": {
                    "rich_text": [ { "type": "text", "text": { "content": "📍 Emplacement : %s" } } ]
                  }
                },
                {
                  "object": "block",
                  "type": "paragraph",
                  "paragraph": {
                    "rich_text": [ { "type": "text", "text": { "content": "%s" } } ]
                  }
                }
              ]
            }
            """.formatted(parentPageId, title, breadcrumb, sanitizedContent);

            HttpEntity<String> entity = new HttpEntity<>(jsonBody, getNotionHeaders());
            ResponseEntity<String> response = restTemplate.exchange(createUrl, HttpMethod.POST, entity, String.class);

            return response.getStatusCode().is2xxSuccessful();

        } catch (Exception e) {
            System.err.println("❌ Erreur création page Notion : " + e.getMessage());
            return false;
        }
    }

    // --- HELPER : EXTRAIRE LE TEXTE D'UNE PAGE OU SOUS-PAGE ---
    @SuppressWarnings("unchecked")
    private String fetchPageContent(String pageId) {
        StringBuilder contentBuilder = new StringBuilder();
        try {
            String url = notionUrl + "/blocks/" + pageId + "/children";
            HttpEntity<String> entity = new HttpEntity<>(getNotionHeaders());
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            if (response.getBody() != null && response.getBody().containsKey("results")) {
                List<Map<String, Object>> blocks = (List<Map<String, Object>>) response.getBody().get("results");

                for (Map<String, Object> block : blocks) {
                    String type = (String) block.get("type");
                    if (type == null) continue;

                    Map<String, Object> typeObject = (Map<String, Object>) block.get(type);

                    if (typeObject == null) continue;

                    switch (type) {
                        case "paragraph":
                        case "heading_1":
                        case "heading_2":
                        case "heading_3":
                        case "quote":
                        case "callout":
                        case "code":
                            appendRichText(contentBuilder, typeObject, "rich_text");
                            // Ajout de <br> pour les retours à la ligne en HTML si besoin, 
                            // sinon on garde \n\n (certains clients mails gèrent bien les \n dans un <pre> ou si tu convertis plus tard)
                            contentBuilder.append("\n\n");
                            break;

                        case "bulleted_list_item":
                        case "numbered_list_item":
                            contentBuilder.append("- ");
                            appendRichText(contentBuilder, typeObject, "rich_text");
                            contentBuilder.append("\n");
                            break;

                        case "image": {
                            String imageUrl = null;
                            String imageKind = (String) typeObject.get("type");
                            
                            // On extrait l'URL selon le type d'image (externe ou uploadée)
                            if ("external".equals(imageKind)) {
                                Map<String, Object> external = (Map<String, Object>) typeObject.get("external");
                                if (external != null) imageUrl = (String) external.get("url");
                            } else if ("file".equals(imageKind)) {
                                Map<String, Object> file = (Map<String, Object>) typeObject.get("file");
                                if (file != null) imageUrl = (String) file.get("url");
                            }
                            
                            String caption = extractPlainTextFromRichText((List<Map<String, Object>>) typeObject.get("caption"));
                            
                            // ✅ MODIFICATION ICI : Création d'une balise HTML <img> au lieu de Markdown
                            if (imageUrl != null) {
                                String altText = caption != null ? caption : "Image Notion";
                                contentBuilder.append("\n<br>\n")
                                              .append("<img src=\"").append(imageUrl).append("\" ")
                                              .append("alt=\"").append(altText).append("\" ")
                                              .append("style=\"max-width: 100%; height: auto; border-radius: 8px; margin: 10px 0;\" />")
                                              .append("\n<br>\n\n");
                            }
                        }
                        break;

                        case "child_page": {
                            Map<String, Object> childPage = (Map<String, Object>) block.get("child_page");
                            if (childPage != null && childPage.containsKey("title")) {
                                contentBuilder.append("# ").append((String) childPage.get("title")).append("\n\n");
                            }
                        }
                        break;

                        default:
                            if (typeObject.containsKey("rich_text")) {
                                appendRichText(contentBuilder, typeObject, "rich_text");
                                contentBuilder.append("\n\n");
                            } else if (typeObject.containsKey("caption")) {
                                appendRichText(contentBuilder, typeObject, "caption");
                                contentBuilder.append("\n\n");
                            }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur lecture contenu de la page " + pageId + " : " + e.getMessage());
        }
        return contentBuilder.toString().trim();
    }

    // Helper pour concaténer le plain_text depuis un champ rich_text ou caption
    @SuppressWarnings("unchecked")
    private void appendRichText(StringBuilder sb, Map<String, Object> typeObject, String fieldName) {
        try {
            if (typeObject == null || !typeObject.containsKey(fieldName)) return;
            List<Map<String, Object>> richTexts = (List<Map<String, Object>>) typeObject.get(fieldName);
            for (Map<String, Object> textObj : richTexts) {
                if (textObj == null) continue;
                String plainText = (String) textObj.get("plain_text");
                if (plainText != null) sb.append(plainText);
                else {
                    // Parfois le texte est imbriqué sous "text" -> "content"
                    Map<String, Object> inner = (Map<String, Object>) textObj.get("text");
                    if (inner != null && inner.containsKey("content")) {
                        sb.append((String) inner.get("content"));
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    @SuppressWarnings("unchecked")
    private String extractPlainTextFromRichText(List<Map<String, Object>> richTexts) {
        if (richTexts == null) return null;
        StringBuilder sb = new StringBuilder();
        for (Map<String, Object> t : richTexts) {
            if (t == null) continue;
            String plain = (String) t.get("plain_text");
            if (plain != null) sb.append(plain);
            else {
                Map<String, Object> inner = (Map<String, Object>) t.get("text");
                if (inner != null && inner.containsKey("content")) sb.append((String) inner.get("content"));
            }
        }
        return sb.length() == 0 ? null : sb.toString();
    }

    // --- FEATURE : RÉCUPÉRER TOUTES LES NOTES DE NOTION ---
    @SuppressWarnings("unchecked")
    public List<Note> fetchAllNotionNotes() {
        List<Note> notionNotes = new ArrayList<>();
        try {
            String parentContent = fetchPageContent(parentPageId);
            if (parentContent != null && !parentContent.isEmpty()) {
                Note parentNote = new Note();
                parentNote.setId(parentPageId);
                parentNote.setTitle("Notes Générales (Page Principale)");
                parentNote.setContent(parentContent);
                parentNote.setBreadcrumb("Notion/Cloud");
                parentNote.setAction("DO");
                notionNotes.add(parentNote);
            }

            String url = notionUrl + "/blocks/" + parentPageId + "/children";
            HttpEntity<String> entity = new HttpEntity<>(getNotionHeaders());
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            if (response.getBody() != null && response.getBody().containsKey("results")) {
                List<Map<String, Object>> results = (List<Map<String, Object>>) response.getBody().get("results");

                for (Map<String, Object> block : results) {
                    if ("child_page".equals(block.get("type"))) {
                        Map<String, Object> childPage = (Map<String, Object>) block.get("child_page");
                        String pageId = (String) block.get("id");

                        Note n = new Note();
                        n.setId(pageId);
                        n.setTitle((String) childPage.get("title"));
                        n.setContent(fetchPageContent(pageId));
                        n.setBreadcrumb("Notion/Cloud");
                        n.setAction("DO");

                        notionNotes.add(n);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur fetch Notion : " + e.getMessage());
        }
        return notionNotes;
    }

    // --- FEATURE : POUSSER LA SYNTHÈSE VERS NOTION ---
    public void pushNoteToNotion(Note note) throws Exception {
        String safeContent = note.getContent() != null ? note.getContent() : "Contenu vide";
        if (safeContent.length() > 2000) {
            safeContent = safeContent.substring(0, 1995) + "...";
        }

        String safeTitle = note.getTitle() != null ? note.getTitle().replace("\"", "\\\"").replace("\n", " ") : "Nouvelle Synthèse IA";
        String escapedContent = safeContent.replace("\"", "\\\"").replace("\n", "\\n");

        String jsonBody = """
            {
                "parent": { "page_id": "%s" },
                "properties": {
                    "title": [
                        {
                            "text": { "content": "%s" }
                        }
                    ]
                },
                "children": [
                    {
                        "object": "block",
                        "type": "paragraph",
                        "paragraph": {
                            "rich_text": [
                                {
                                    "type": "text",
                                    "text": {
                                        "content": "%s"
                                    }
                                }
                            ]
                        }
                    }
                ]
            }
            """.formatted(parentPageId, safeTitle, escapedContent);

        HttpEntity<String> request = new HttpEntity<>(jsonBody, getNotionHeaders());
        
        restTemplate.exchange(
                notionUrl + "/pages",
                HttpMethod.POST,
                request,
                String.class
        );
    }

    // --- FEATURE : ARCHIVER UNE PAGE NOTION ---
    public void archiveNotionPage(String pageId) {
        
        // 🛡️ AJOUT DU GARDE-FOU ICI
        // Si l'ID qu'on essaie de supprimer est celui de la page principale (parentPageId)
        if (pageId.equals(this.parentPageId)) {
            System.err.println("☁️❌ Sécurité : Tentative d'archivage de la page racine Notion (" + pageId + ") bloquée par le backend.");
            return; // On coupe l'exécution ici, la requête HTTP vers Notion n'est pas envoyée.
        }

        try {
            String url = notionUrl + "/pages/" + pageId;
            String body = "{\"archived\": true}";

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + this.notionToken)
                    .header("Notion-Version", this.notionVersion) 
                    .header("Content-Type", "application/json")
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println("☁️✅ Page Notion archivée avec succès (ID: " + pageId + ")");
            } else {
                System.err.println("☁️❌ Erreur Notion : Code " + response.statusCode() + " - " + response.body());
            }
            
        } catch (Exception e) {
            System.err.println("☁️❌ Erreur lors de l'archivage sur Notion : " + e.getMessage());
        }
    }
}
