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
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import projet.devops.Mail.Model.CalendarIntent;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

@Service
public class GoogleCalendarService {
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "storage/google_tokens";

    @Value("${google.client.id}")
    private String clientId;

    @Value("${google.client.secret}")
    private String clientSecret;

    /**
     * Initialise le client Google Calendar avec l'authentification OAuth2.
     */
    private Calendar getCalendarClient() throws Exception {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        // On construit le JSON de credentials dynamiquement avec tes clés du .properties
        String credentialsJson = String.format(
            "{\"installed\":{\"client_id\":\"%s\",\"project_id\":\"eisenflow\",\"auth_uri\":\"https://accounts.google.com/o/oauth2/auth\",\"token_uri\":\"https://oauth2.googleapis.com/token\",\"client_secret\":\"%s\",\"redirect_uris\":[\"http://localhost:8888/Callback\"]}}", 
            clientId, clientSecret
        );

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
            JSON_FACTORY, 
            new InputStreamReader(new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8)))
        );

        // Configuration du flux d'autorisation
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, Collections.singletonList(CalendarScopes.CALENDAR_EVENTS))
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();

        // On utilise le port 8888 (celui que tu as libéré avec le "kill")
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");

        return new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName("EisenFlow")
                .build();
    }

    /**
     * Insère un événement dans Google Calendar basé sur l'analyse de l'IA.
     */
    public String insertEvent(CalendarIntent intent) {
        try {
            Calendar service = getCalendarClient();
            
            // 1. Préparation du texte
            String summary = (intent.getTitle() != null && !intent.getTitle().isEmpty()) ? intent.getTitle() : "Tâche IA détectée";
            String description = "🤖 Stratégie IA : " + intent.getStrategy() + "\n👤 Assigné à : " + intent.getAssignee();

            Event event = new Event()
                .setSummary(summary)
                .setDescription(description);

            // 2. Gestion stricte des dates pour éviter l'erreur 400
            // Format d'entrée attendu de l'IA : dd/MM/yyyy
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate startDate = LocalDate.parse(intent.getDeadline(), inputFormatter);
            
            // IMPORTANT : Pour Google, un événement "All Day" doit finir le LENDEMAIN du début.
            // Si l'événement est le 28/03/2026, la fin doit être le 29/03/2026.
            LocalDate endDate = startDate.plusDays(1);

            // On utilise le format YYYY-MM-DD exigé par Google pour les dates sans heure
            EventDateTime start = new EventDateTime().setDate(new DateTime(startDate.toString()));
            EventDateTime end = new EventDateTime().setDate(new DateTime(endDate.toString()));

            event.setStart(start);
            event.setEnd(end);

            // 3. Exécution de la requête
            Event createdEvent = service.events().insert("primary", event).execute();
            
            System.out.println("✅ [Google API] Succès ! Événement créé : " + createdEvent.getHtmlLink());
            return createdEvent.getHtmlLink();

        } catch (Exception e) {
            System.err.println("❌ [Google API] Échec de la création : " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}