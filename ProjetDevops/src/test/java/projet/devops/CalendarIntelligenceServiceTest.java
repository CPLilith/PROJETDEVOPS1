package projet.devops; // Le package correspond au dossier actuel du test

// Tu dois rajouter cette ligne pour importer ton Service qui est dans un autre dossier
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse; // N'oublie pas le modèle aussi
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import projet.devops.Mail.Model.CalendarIntent;
import projet.devops.Mail.Service.CalendarIntelligenceService;


class CalendarIntelligenceServiceTest {

    private CalendarIntelligenceService service;

    @BeforeEach
    void setUp() {
        // On réinitialise le service avant chaque test pour avoir une Map vide
        service = new CalendarIntelligenceService();
    }

    @Test
    void testAnalyzeAndStoreIntent_ShouldParseAndStoreCorrectly() {
        // Arrange
        String messageId = "msg-999";
        String subject = "Point Synchro Projet";
        String content = "Bonjour, on se fait un point demain ?";

        // Act
        CalendarIntent intent = service.analyzeAndStoreIntent(messageId, subject, content);

        // Assert - Vérification du parsing (basé sur la réponse "vraieReponseIA" hardcodée dans ton code)
        assertNotNull(intent, "L'objet CalendarIntent ne doit pas être null");
        assertEquals("PLAN", intent.getStrategy(), "La stratégie doit être PLAN");
        assertEquals("INCONNUE", intent.getDeadline(), "La date doit être INCONNUE");
        assertFalse(intent.isDateDetected(), "La détection de date doit être false si INCONNUE");
        assertEquals(subject, intent.getTitle(), "Le titre doit correspondre au sujet injecté");
        assertEquals(content, intent.getFullMailContent(), "Le contenu complet du mail doit être bien sauvegardé");

        // Assert - Vérification du stockage en mémoire
        CalendarIntent storedIntent = service.getIntent(messageId);
        assertNotNull(storedIntent, "L'intention doit être retrouvée via son messageId");
        assertEquals(intent, storedIntent, "L'objet récupéré doit être l'instance exacte qui a été stockée");
    }

    @Test
    void testGetIntent_WhenMessageIdDoesNotExist_ShouldReturnEmptyIntent() {
        // Act
        CalendarIntent result = service.getIntent("id-fantome");

        // Assert
        assertNotNull(result, "Doit retourner une nouvelle instance vide au lieu de null");
        assertNull(result.getStrategy(), "Une instance fraîchement créée ne doit pas avoir de stratégie par défaut assignée par la Map");
    }

    @SpringBootTest // Charge tout le contexte Spring
class CalendarIntelligenceServiceIntegrationTest {

    @Autowired
    private CalendarIntelligenceService service;

    @Test
    void testFullWorkflow() {
        // Ici, on vérifie que le service fonctionne réellement avec le vrai modèle
        CalendarIntent intent = service.analyzeAndStoreIntent("ID-1", "RDV", "Demain 10h");
        
        assertNotNull(service.getIntent("ID-1"));
        assertEquals("RDV", intent.getTitle());
    }
}
}