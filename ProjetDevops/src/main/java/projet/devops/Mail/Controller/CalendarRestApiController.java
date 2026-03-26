package projet.devops.Mail.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projet.devops.Mail.Model.CalendarIntent;
import projet.devops.Mail.Service.CalendarIntelligenceService;
import projet.devops.Mail.Service.GoogleCalendarService;

import java.util.Map;

@RestController
@RequestMapping("/api/calendar")
@CrossOrigin(origins = "*") // Important pour le Front-end
public class CalendarRestApiController {

    private final CalendarIntelligenceService intelligenceService;
    private final GoogleCalendarService googleCalendarService;

    public CalendarRestApiController(CalendarIntelligenceService intelligenceService, GoogleCalendarService googleCalendarService) {
        this.intelligenceService = intelligenceService;
        this.googleCalendarService = googleCalendarService;
    }

    // ON GARDE CETTE MÉTHODE (Pour charger la modale)
    @GetMapping("/intent/{messageId}")
    public ResponseEntity<?> getCalendarIntent(@PathVariable String messageId) {
        CalendarIntent intent = intelligenceService.getIntent(messageId);
        if (intent == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Intention non trouvée"));
        }
        return ResponseEntity.ok(intent);
    }

    // ON AMÉLIORE CELLE-CI (Pour fixer le bug du mail vide)
    @PostMapping("/confirm")
    public ResponseEntity<?> confirmAndCreateEvent(
            @RequestBody CalendarIntent validatedIntent, 
            @RequestParam("messageId") String messageId) {
        
        System.out.println("\n=== 🛠️ DÉBUT DE LA CONFIRMATION ===");
        System.out.println("ID reçu du Web : " + messageId);

        // 1. On cherche en mémoire
        CalendarIntent storedIntent = intelligenceService.getIntent(messageId);
        
        // 2. Vérification et Fusion
        if (storedIntent != null && storedIntent.getFullMailContent() != null) {
            System.out.println("✅ TROP BIEN : Le contenu du mail a été trouvé en mémoire !");
            validatedIntent.setFullMailContent(storedIntent.getFullMailContent());
        } else {
            System.out.println("❌ AÏE : Aucun contenu trouvé pour cet ID. La mémoire a-t-elle été vidée ?");
            // LE FILET DE SÉCURITÉ (Fini le 'null' moche !)
            validatedIntent.setFullMailContent("Le contenu original du mail n'est plus en mémoire. Veuillez vérifier votre boîte de réception principale.");
        }

        System.out.println("=== FIN DE LA VÉRIFICATION ===\n");

        // 3. Création
        String eventLink = googleCalendarService.insertEvent(validatedIntent);
        
        if (eventLink != null) {
            return ResponseEntity.ok(Map.of("status", "success", "link", eventLink));
        } else {
            return ResponseEntity.status(500).body(Map.of("error", "Erreur Google Calendar"));
        }
    }
}