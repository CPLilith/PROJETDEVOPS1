package projet.devops; // <-- On met le bon package correspondant à ton dossier actuel

// On importe manuellement la classe et le modèle depuis leurs vrais dossiers
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import projet.devops.Mail.Model.Note;
import projet.devops.Mail.Service.ExternalNoteApiService;

@ExtendWith(MockitoExtension.class)
class ExternalNoteApiServiceTest {

    @Mock
    private RestTemplate mockRestTemplate;

    private ExternalNoteApiService service;

    private final String FAKE_PARENT_ID = "parent-123";
    private final String FAKE_URL = "https://api.notion.com/v1";

    @BeforeEach
    void setUp() {
        // 1. Instanciation du service avec un faux token
        service = new ExternalNoteApiService("fake-token");

        // 2. Injection des propriétés @Value via la Réflexion (car on ne lance pas le contexte Spring)
        ReflectionTestUtils.setField(service, "notionUrl", FAKE_URL);
        ReflectionTestUtils.setField(service, "notionVersion", "2022-06-28");
        ReflectionTestUtils.setField(service, "parentPageId", FAKE_PARENT_ID);

        // 3. Remplacement du RestTemplate créé dans le constructeur par notre Mock
        ReflectionTestUtils.setField(service, "restTemplate", mockRestTemplate);
    }

    @Test
    void testFetchExistingBreadcrumbs_ShouldReturnDefaultList() {
        // Act
        List<String> breadcrumbs = service.fetchExistingBreadcrumbs();

        // Assert
        assertNotNull(breadcrumbs);
        assertEquals(3, breadcrumbs.size());
        assertTrue(breadcrumbs.contains("Projets/DevOps"));
        assertTrue(breadcrumbs.contains("Notes/Réunions"));
    }

    @Test
    void testCreateNoteInExternalApp_Success_ShouldReturnTrue() {
        // Arrange
        String breadcrumb = "Notes/Test";
        String title = "Note de Test";
        String content = "Contenu de la note";

        // On simule une réponse 200 OK de Notion
        ResponseEntity<String> mockResponse = new ResponseEntity<>("Success", HttpStatus.OK);
        when(mockRestTemplate.exchange(
                eq(FAKE_URL + "/pages"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(mockResponse);

        // Act
        boolean isCreated = service.createNoteInExternalApp(breadcrumb, title, content, "16/06/2026");

        // Assert
        assertTrue(isCreated, "La création devrait réussir (retourner true)");
        verify(mockRestTemplate, times(1)).exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class));
    }

    @Test
    void testCreateNoteInExternalApp_Failure_ShouldReturnFalse() {
        // Arrange
        // On simule une erreur réseau ou API (Exception levée par RestTemplate)
        when(mockRestTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class)))
                .thenThrow(new RuntimeException("API Notion Injoignable"));

        // Act
        boolean isCreated = service.createNoteInExternalApp("Dossier", "Titre", "Contenu", "16/06/2026");

        // Assert
        assertFalse(isCreated, "La création devrait échouer (retourner false) suite à l'exception");
    }

    @Test
    void testPushNoteToNotion_ShouldSendCorrectRequest() throws Exception {
        // Arrange
        Note note = new Note();
        note.setTitle("Ma Synthèse");
        note.setContent("Voici le résumé de la réunion.");

        ResponseEntity<String> mockResponse = new ResponseEntity<>("Success", HttpStatus.OK);
        when(mockRestTemplate.exchange(
                eq(FAKE_URL + "/pages"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(mockResponse);

        // Act
        service.pushNoteToNotion(note);

        // Assert
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(mockRestTemplate).exchange(eq(FAKE_URL + "/pages"), eq(HttpMethod.POST), entityCaptor.capture(), eq(String.class));
        
        // On vérifie que le JSON envoyé contient bien notre titre et notre contenu
        String jsonBodySent = entityCaptor.getValue().getBody().toString();
        assertTrue(jsonBodySent.contains("Ma Synthèse"));
        assertTrue(jsonBodySent.contains("Voici le résumé de la réunion."));
    }

    @Test
    void testArchiveNotionPage_WhenIsParentPage_ShouldAbortAndNotThrowError() {
        // Arrange
        // On passe l'ID de la page parente pour déclencher le garde-fou
        String pageToArchive = FAKE_PARENT_ID;

        // Act
        // Comme on ne peut pas intercepter facilement l'HttpClient hardcodé, 
        // ce test s'assure simplement que la condition de sécurité bloque l'exécution
        // sans faire crasher le test.
        assertDoesNotThrow(() -> service.archiveNotionPage(pageToArchive));

        // Si le garde-fou ne fonctionnait pas, le HttpClient essaierait de s'exécuter 
        // et générerait une erreur réseau ou une Exception.
    }

    @Test
    @SuppressWarnings("unchecked")
    void testFetchAllNotionNotes_WhenApiReturnsEmpty_ShouldHandleGracefully() {
        // Arrange
        // On simule une réponse vide de Notion pour éviter les NullPointerExceptions
        ResponseEntity<Map> mockResponse = new ResponseEntity<>(Map.of(), HttpStatus.OK);
        
        when(mockRestTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(mockResponse);

        // Act
        List<Note> notes = service.fetchAllNotionNotes();

        // Assert
        // Si la réponse de l'API est vide, la méthode devrait au moins nous retourner 
        // une liste (peut-être contenant uniquement la page parente vide, ou complètement vide)
        assertNotNull(notes, "La liste de notes ne doit pas être null");
    }
}