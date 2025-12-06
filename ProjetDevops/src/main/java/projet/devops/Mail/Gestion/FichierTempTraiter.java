package projet.devops.Mail.Gestion;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import projet.devops.Mail.Classifier.MailProcessor;
import projet.devops.Mail.Mail;

@Component
public class FichierTempTraiter {

    private static final String FILE_PATH = "src/main/resources/mails_traites.txt";
    private final ObjectMapper objectMapper;

    public FichierTempTraiter() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Traite les mails (ajoute les tags) et les sauvegarde
     */
    public void traiterEtSauvegarder(List<Map<String, String>> mailsOriginaux) throws IOException {
        // Listes de mots-clés pour le fallback
        List<String> importanceWords = Arrays.asList("important", "urgent", "critique", "prioritaire", "essentiel");
        List<String> urgencyWords = Arrays.asList("asap", "immédiat", "aujourd'hui", "deadline", "rapidement");

        List<Map<String, String>> mailsTraites = new ArrayList<>();

        for (Map<String, String> mailMap : mailsOriginaux) {
            String from = mailMap.get("from");
            String subject = mailMap.get("subject");
            String content = mailMap.get("content");
            String date = mailMap.get("date");

            // Créer l'objet Mail
            Mail mailObj = new Mail(date, subject, from, content, null);

            // Classifier avec Ollama (ou fallback)
            Mail taggedMail = MailProcessor.tagMail(mailObj, importanceWords, urgencyWords);

            // Ajouter le tag à la map
            mailMap.put("tag", taggedMail.getTag().toString());
            mailsTraites.add(mailMap);
        }

        // Sauvegarder
        try (FileWriter writer = new FileWriter(FILE_PATH, false)) {
            String json = objectMapper.writeValueAsString(mailsTraites);
            writer.write(json);
            System.out.println("✅ " + mailsTraites.size() + " mails traités et sauvegardés dans " + FILE_PATH);
        }
    }

    /**
     * Lit les mails traités depuis le fichier
     */
    public List<Map<String, String>> lireMailsTraites() throws IOException {
        if (Files.exists(Paths.get(FILE_PATH))) {
            String content = new String(Files.readAllBytes(Paths.get(FILE_PATH)));
            List<Map<String, String>> mails = objectMapper.readValue(content,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
            System.out.println("✅ " + mails.size() + " mails traités chargés depuis " + FILE_PATH);
            return mails;
        }
        System.err.println("⚠️ Fichier introuvable: " + FILE_PATH);
        return List.of();
    }

    /**
     * Vérifie si le fichier traité existe
     */
    public boolean fichierExiste() {
        return Files.exists(Paths.get(FILE_PATH));
    }

    public void supprimerFichier() throws IOException {
        Files.deleteIfExists(Paths.get(FILE_PATH));
        System.out.println("🗑️ Fichier traité supprimé: " + FILE_PATH);
    }
}