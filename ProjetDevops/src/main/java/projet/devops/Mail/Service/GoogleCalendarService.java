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
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, Collections.singletonList(CalendarScopes.CALENDAR_EVENTS))
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
            realDeadlineEvent.setStart(new EventDateTime().setDate(new DateTime(deadlineDate.toString())))
                             .setEnd(new EventDateTime().setDate(new DateTime(deadlineDate.plusDays(1).toString())));

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
}