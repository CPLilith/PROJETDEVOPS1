package projet.devops.Mail.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

import projet.devops.Mail.Model.CalendarIntent;
import projet.devops.Mail.Service.CalendarIntelligenceService;

@RestController
@RequestMapping("/api/calendar")
public class CalendarRestApiController {

    private final CalendarIntelligenceService calendarService;

    public CalendarRestApiController(CalendarIntelligenceService calendarService) {
        this.calendarService = calendarService;
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
}