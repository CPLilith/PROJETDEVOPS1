package projet.devops.Mail.Service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.*;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import projet.devops.Mail.Model.CalendarIntent;
import projet.devops.Mail.Strategy.*;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;

@Service
public class GoogleCalendarService {
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "storage/google_tokens";

    @Autowired
    private JavaMailSender mailSender;

    @Value("${google.client.id}")
    private String clientId;

    @Value("${google.client.secret}")
    private String clientSecret;

    @Value("${spring.mail.username}")
    private String senderEmail;

    private Calendar getCalendarClient() throws Exception {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        String credentialsJson = String.format(
            "{\"installed\":{\"client_id\":\"%s\",\"project_id\":\"eisenflow\",\"auth_uri\":\"https://accounts.google.com/o/oauth2/auth\",\"token_uri\":\"https://oauth2.googleapis.com/token\",\"client_secret\":\"%s\",\"redirect_uris\":[\"http://localhost:8888/Callback\"]}}", 
            clientId, clientSecret
        );
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8))));
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, Collections.singletonList(CalendarScopes.CALENDAR))
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline").build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
        return new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName("EisenFlow").build();
    }

    private DelegationStrategy getStrategyAlgo(String confidence) {
        if (confidence == null) return new MajeureStrategy(); 
        switch (confidence.toUpperCase()) {
            case "MINEUR": return new MineureStrategy();
            case "MOYEN":  return new MoyenneStrategy();
            case "MAJEUR":
            default:       return new MajeureStrategy();
        }
    }

    public String insertEvent(CalendarIntent intent) {
        try {
            Calendar service = getCalendarClient();
            String mainSummary = (intent.getTitle() != null && !intent.getTitle().isEmpty()) ? intent.getTitle() : "Tâche déléguée";
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate deadlineDate = LocalDate.parse(intent.getDeadline(), inputFormatter);
            LocalDate today = LocalDate.now();

            // 1. ENVOI DU MAIL "FAÇON DOCTOLIB" AVEC INVITATION AUTOMATIQUE (.ICS)
            if ("BOOMERANG".equalsIgnoreCase(intent.getStrategy()) && intent.getAssignee().contains("@")) {
                sendIcsEmail(intent, mainSummary, deadlineDate);
            }

            // 2. TES RELANCES STRATEGY (Uniquement chez toi)
            // Note : On laisse ça en événement "Toute la journée" car c'est juste un pense-bête
            if ("BOOMERANG".equalsIgnoreCase(intent.getStrategy())) {
                DelegationStrategy strategyAlgo = getStrategyAlgo(intent.getConfidence());
                List<LocalDate> reminderDates = strategyAlgo.calculateReminderDates(deadlineDate, today);
                for (LocalDate rDate : reminderDates) {
                    Event rEvent = new Event().setSummary("⏳ Relance : " + mainSummary);
                    rEvent.setStart(new EventDateTime().setDate(new DateTime(rDate.toString())))
                          .setEnd(new EventDateTime().setDate(new DateTime(rDate.plusDays(1).toString())));
                    service.events().insert("primary", rEvent).execute();
                }
            }

            // 3. TA DEADLINE RÉELLE (Jour J - Seulement pour toi)
            Event realDeadlineEvent = new Event()
                .setSummary("🚨 DEADLINE RÉELLE : " + mainSummary)
                .setDescription("Fin de la tâche déléguée à " + intent.getAssignee());

            // --- GESTION TOUTE LA JOURNÉE VS HEURE PRÉCISE ---
            if (intent.isAllDay()) {
                // Création en mode "Toute la journée" (All Day)
                realDeadlineEvent.setStart(new EventDateTime().setDate(new DateTime(deadlineDate.toString())));
                realDeadlineEvent.setEnd(new EventDateTime().setDate(new DateTime(deadlineDate.plusDays(1).toString())));
            } else {
                // Création en mode "Heure précise"
                String startTimeStr = (intent.getStartTime() != null && !intent.getStartTime().isEmpty()) ? intent.getStartTime() : "09:00";
                int duration = intent.getDurationMinutes() > 0 ? intent.getDurationMinutes() : 60;

                LocalTime startTime = LocalTime.parse(startTimeStr);
                LocalDateTime startDateTime = LocalDateTime.of(deadlineDate, startTime);
                LocalDateTime endDateTime = startDateTime.plusMinutes(duration);
                ZoneId zoneId = ZoneId.of("Europe/Paris");

                realDeadlineEvent.setStart(new EventDateTime()
                    .setDateTime(new DateTime(startDateTime.atZone(zoneId).toInstant().toEpochMilli()))
                    .setTimeZone("Europe/Paris"));

                realDeadlineEvent.setEnd(new EventDateTime()
                    .setDateTime(new DateTime(endDateTime.atZone(zoneId).toInstant().toEpochMilli()))
                    .setTimeZone("Europe/Paris"));
            }
            // --- FIN DE LA NOUVELLE GESTION ---

            Event result = service.events().insert("primary", realDeadlineEvent).execute();
            return result.getHtmlLink();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // NOUVELLE MÉTHODE : Le mail avec fichier .ics (Le Standard de l'Industrie)
    private void sendIcsEmail(CalendarIntent intent, String title, LocalDate deadline) {
        try {
            LocalDate jMoinsUn = deadline.minusDays(1);
            
            // On crée le mail enrichi (MimeMessage)
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom(senderEmail);
            helper.setTo(intent.getAssignee().trim());
            helper.setSubject("Invitation : " + title);
            helper.setText("Bonjour,\n\nCette tâche vous a été déléguée. Vous trouverez l'invitation pour la veille de l'échéance en pièce jointe (qui s'ajoute souvent automatiquement à votre calendrier).\n\n" +
                           "--- CONTENU DU MAIL ---\n" + intent.getFullMailContent());

            // On génère le fichier iCalendar (Le format lu par Gmail, Outlook, Apple)
            String icsContent = "BEGIN:VCALENDAR\n" +
                                "VERSION:2.0\n" +
                                "PRODID:-//EisenFlow//FR\n" +
                                "METHOD:REQUEST\n" +
                                "BEGIN:VEVENT\n" +
                                "DTSTART;VALUE=DATE:" + jMoinsUn.format(DateTimeFormatter.BASIC_ISO_DATE) + "\n" +
                                "DTEND;VALUE=DATE:" + deadline.format(DateTimeFormatter.BASIC_ISO_DATE) + "\n" +
                                "SUMMARY:📌 À FAIRE : " + title + "\n" +
                                "DESCRIPTION:Tâche déléguée via EisenFlow.\\n\\n" + intent.getFullMailContent().replace("\n", "\\n") + "\n" +
                                "END:VEVENT\n" +
                                "END:VCALENDAR";

            // On l'attache au mail
            helper.addAttachment("invitation.ics", new ByteArrayResource(icsContent.getBytes(StandardCharsets.UTF_8)), "text/calendar");

            mailSender.send(mimeMessage);
            System.out.println("✅ Email .ics envoyé avec succès !");
            
        } catch (Exception e) {
            System.err.println("❌ Erreur d'envoi du mail .ics : " + e.getMessage());
        }
    }

    /**
     * Interroge Google Calendar pour lister TOUS les créneaux d'une journée (libres et occupés).
     * @param dateStr La date cible au format "dd/MM/yyyy"
     * @param durationMinutes La durée de la tâche (ex: 60)
     * @return Une liste de dictionnaires (Map) contenant l'heure ("time") et l'état de disponibilité ("available").
     */
    public List<Map<String, Object>> getAvailableSlots(String dateStr, int durationMinutes) throws Exception {
        
        // 1. Initialisation du client Google Calendar
        Calendar service = getCalendarClient(); 

        // 2. Définir la plage horaire de travail (09h00 à 18h00)
        LocalDate targetDate = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        LocalDateTime startOfDay = targetDate.atTime(9, 0);
        LocalDateTime endOfDay = targetDate.atTime(18, 0);

        DateTime timeMin = new DateTime(startOfDay.atZone(ZoneId.of("Europe/Paris")).toInstant().toEpochMilli());
        DateTime timeMax = new DateTime(endOfDay.atZone(ZoneId.of("Europe/Paris")).toInstant().toEpochMilli());

        // 3. Préparer la requête FreeBusy de Google
        FreeBusyRequest request = new FreeBusyRequest();
        request.setTimeMin(timeMin);
        request.setTimeMax(timeMax);
        request.setItems(Collections.singletonList(new FreeBusyRequestItem().setId("primary"))); // "primary" = agenda principal

        // 4. Récupérer les événements existants (les conflits)
        FreeBusyResponse response = service.freebusy().query(request).execute();
        List<TimePeriod> busyPeriods = response.getCalendars().get("primary").getBusy();
        if (busyPeriods == null) {
            busyPeriods = new ArrayList<>();
        }

        // ==========================================
        //         🔍 BLOC DE LOGS DE DEBUG 🔍
        // ==========================================
        System.out.println("\n--- 🛠️ DEBUG FREEBUSY ---");
        System.out.println("📅 Date demandée : " + dateStr);
        System.out.println("⏳ Nombre d'événements bloquants trouvés : " + busyPeriods.size());
        for(TimePeriod tp : busyPeriods) {
            System.out.println("   -> Occupé de " + tp.getStart() + " à " + tp.getEnd());
        }
        System.out.println("-------------------------\n");
        // ==========================================

        // 5. L'algorithme de balayage : on cherche TOUS les créneaux
        List<Map<String, Object>> allSlots = new ArrayList<>();
        LocalTime pointer = LocalTime.of(9, 0); // On commence à scanner à 09h00
        LocalTime endDay = LocalTime.of(18, 0); // On s'arrête à 18h00

        // Tant que le créneau tient avant 18h00
        while (!pointer.plusMinutes(durationMinutes).isAfter(endDay)) {
            LocalTime slotStart = pointer;
            LocalTime slotEnd = pointer.plusMinutes(durationMinutes);
            boolean isConflict = false;

            // Vérifier s'il chevauche un événement existant
            for (TimePeriod busy : busyPeriods) {
                LocalTime busyStart = LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(busy.getStart().getValue()), ZoneId.of("Europe/Paris")).toLocalTime();
                LocalTime busyEnd = LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(busy.getEnd().getValue()), ZoneId.of("Europe/Paris")).toLocalTime();

                // Logique de collision de temps
                if (slotStart.isBefore(busyEnd) && slotEnd.isAfter(busyStart)) {
                    isConflict = true;
                    break; // Conflit trouvé, on arrête de vérifier
                }
            }

            // On crée un petit objet pour ce créneau : { "time": "10:30", "available": true/false }
            Map<String, Object> slotData = new java.util.HashMap<>();
            slotData.put("time", slotStart.toString());
            slotData.put("available", !isConflict); // S'il n'y a PAS de conflit, il est disponible
            
            allSlots.add(slotData);

            // On avance notre "scanner" de 30 minutes
            pointer = pointer.plusMinutes(30);
        }

        return allSlots;
    }
}