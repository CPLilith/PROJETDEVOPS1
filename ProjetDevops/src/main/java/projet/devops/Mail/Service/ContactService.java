package projet.devops.Mail.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import projet.devops.Mail.Classifier.OllamaClient;
import projet.devops.Mail.Classifier.TextCleaner;
import projet.devops.Mail.Mail;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ContactService {

    private final OllamaClient ollamaClient;
    private final ObjectMapper objectMapper;
    private final String FILE_PATH = "storage/contacts.json";
    private Map<String, String> contactsCache = new HashMap<>();

    public ContactService(OllamaClient ollamaClient) {
        this.ollamaClient = ollamaClient;
        this.objectMapper = new ObjectMapper();
        
        File directory = new File("storage");
        if (!directory.exists()) {
            directory.mkdir();
        }
        loadContacts();
    }

    private void loadContacts() {
        try {
            File file = new File(FILE_PATH);
            if (file.exists()) {
                contactsCache = objectMapper.readValue(file, new TypeReference<Map<String, String>>() {});
                System.out.println("✅ [Contacts] " + contactsCache.size() + " profils chargés.");
            }
        } catch (Exception e) {
            contactsCache = new HashMap<>();
        }
    }

    private void saveContacts() {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(FILE_PATH), contactsCache);
        } catch (Exception e) {
            System.err.println("❌ Erreur sauvegarde contacts.json");
        }
    }

    public void analyzeAndSaveNewContacts(List<Mail> recentMails) {
        boolean isUpdated = false;
        int limit = Math.min(30, recentMails.size());
        
        System.out.println("[IA] Vérification des contacts (Filtre anti-spam actif)...");

        for (int i = 0; i < limit; i++) {
            Mail mail = recentMails.get(i);
            String emailAddress = extractEmail(mail.getFrom());

            // 🛡️ Filtre : on n'analyse que les vrais humains (ex: Nanterre ou format prenom.nom)
            if (emailAddress.isEmpty() || !isRealHumanContact(emailAddress)) {
                continue; 
            }

            if (!contactsCache.containsKey(emailAddress)) {
                System.out.println("🔍 Nouveau contact pro détecté : " + emailAddress);
                String expertise = guessExpertise(mail.getContent());
                
                contactsCache.put(emailAddress, expertise);
                isUpdated = true;
            }
        }

        if (isUpdated) {
            saveContacts();
            System.out.println("💾 [Contacts] Dictionnaire mis à jour.");
        }
    }

    private String guessExpertise(String content) {
        String lowerContent = content.toLowerCase();
        
        // --- SÉCURITÉ 1 : LE PLAN B (Mots-clés prioritaires) ---
        // Si ces mots sont dans le mail, on ne fait même pas confiance à l'IA
        if (lowerContent.contains("docker") || lowerContent.contains("ci/cd") || lowerContent.contains("git")) {
            return "Expert DevOps / Infrastructure";
        }
        if (lowerContent.contains("ia") || lowerContent.contains("ollama") || lowerContent.contains("backend")) {
            return "Développeur Backend / IA";
        }
        if (lowerContent.contains("css") || lowerContent.contains("html") || lowerContent.contains("frontend") || lowerContent.contains("ui")) {
            return "Designer UI / Frontend";
        }
        if (lowerContent.contains("sql") || lowerContent.contains("base de données") || lowerContent.contains("test")) {
            return "Expert BDD / QA";
        }

        // --- SÉCURITÉ 2 : L'APPEL IA (Si aucun mot-clé n'a matché) ---
        try {
            String cleanContent = TextCleaner.cleanEmailText(content, 200);
            
            // Prompt simplifié à l'extrême pour éviter que l'IA ne recopie les exemples
            String prompt = "Analyse ce mail et donne le métier de l'expéditeur en 3 mots.\n" +
                            "Mail : " + cleanContent + "\n" +
                            "Métier :";

            String response = ollamaClient.generateResponse("tinyllama", prompt).trim();
            
            // Si l'IA recopie encore "Exemples attendus" ou est vide
            if (response.length() < 3 || response.contains("Exemple") || response.contains("Attendu")) {
                return "Membre équipe projet";
            }

            // On ne garde que la première ligne
            if (response.contains("\n")) response = response.split("\n")[0];
            
            return response.replace("\"", "").trim();
            
        } catch (Exception e) {
            return "Membre équipe projet";
        }
    }

    private boolean isRealHumanContact(String email) {
        if (email == null || email.isEmpty()) return false;
        String lowerEmail = email.toLowerCase();
        
        // Liste noire des domaines et mots-clés de robots/newsletters
        String[] spamKeywords = {
            "no-reply", "noreply", "newsletter", "info@", "contact@", 
            "marketing", "hello@", "support@", "stories", "posts", 
            "update", "service.", "news", "market", "advertising", "shein", "instagram"
        };
        
        for (String keyword : spamKeywords) {
            if (lowerEmail.contains(keyword)) return false;
        }
        
        // Priorité aux emails de l'université ou aux formats nominatifs (contenant un point)
        return lowerEmail.endsWith("@parisnanterre.fr") || lowerEmail.contains(".");
    }

    private String extractEmail(String fromRaw) {
        if (fromRaw == null) return "";
        if (fromRaw.contains("<") && fromRaw.contains(">")) {
            return fromRaw.substring(fromRaw.indexOf("<") + 1, fromRaw.indexOf(">")).trim();
        }
        return fromRaw.trim();
    }

    public Map<String, String> getAllContacts() {
        return contactsCache;
    }
}