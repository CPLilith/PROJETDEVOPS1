package projet.devops.Mail.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    private final String notionToken;

    @Value("${notion.api.url}")
    private String notionUrl;

    @Value("${notion.api.version}")
    private String notionVersion;

    @Value("${notion.parent.page.id}")
    private String parentPageId;

    public ExternalNoteApiService(String notionToken) {
        this.restTemplate = new RestTemplate();
        this.notionToken = notionToken;
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

                    if (typeObject != null && typeObject.containsKey("rich_text")) {
                        List<Map<String, Object>> richTexts = (List<Map<String, Object>>) typeObject.get("rich_text");
                        for (Map<String, Object> textObj : richTexts) {
                            String plainText = (String) textObj.get("plain_text");
                            if (plainText != null) {
                                contentBuilder.append(plainText);
                            }
                        }
                        contentBuilder.append("\n\n");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur lecture contenu de la page " + pageId + " : " + e.getMessage());
        }
        return contentBuilder.toString().trim();
    }

    // --- FEATURE : RÉCUPÉRER TOUTES LES NOTES DE NOTION ---
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
}