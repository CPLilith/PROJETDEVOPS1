package projet.devops.Mail.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projet.devops.Mail.Model.CalendarIntent;
import projet.devops.Mail.Service.CalendarIntelligenceService;
import projet.devops.Mail.Service.GoogleCalendarService;

import java.util.Map;

@RestController
@RequestMapping("/api/calendar")
public class CalendarRestApiController {

    private final CalendarIntelligenceService calendarService;
    private final GoogleCalendarService googleCalendarService;

    public CalendarRestApiController(CalendarIntelligenceService calendarService, GoogleCalendarService googleCalendarService) {
        this.calendarService = calendarService;
        this.googleCalendarService = googleCalendarService;
    }

    @GetMapping("/intent/{messageId}")
    public ResponseEntity<?> getCalendarIntent(@PathVariable String messageId) {
        CalendarIntent intent = calendarService.getIntent(messageId);
        
        if (intent == null) {
            return ResponseEntity.status(404).body(Map.of(
                "error", "Aucune intention d'agenda trouvée pour ce mail",
                "status", "NOT_FOUND"
            ));
        }

        // Renvoie l'objet pur qui sera transformé en JSON automatiquement par Spring
        return ResponseEntity.ok(intent);
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirmAndCreateEvent(@RequestBody CalendarIntent validatedIntent) {
        if (validatedIntent.getDeadline() == null || validatedIntent.getDeadline().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Une date valide est requise."));
        }

        String eventLink = googleCalendarService.insertEvent(validatedIntent);
        
        if (eventLink != null) {
            return ResponseEntity.ok(Map.of(
                "status", "success", 
                "message", "Événement ajouté à l'agenda",
                "link", eventLink
            ));
        } else {
            return ResponseEntity.status(500).body(Map.of("error", "Échec de la création dans Google Calendar. Regarde la console Java."));
        }
    }
}