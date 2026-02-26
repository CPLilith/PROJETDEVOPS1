package projet.devops.Mail.Service;

import org.springframework.stereotype.Service;

import projet.devops.Mail.Classifier.OllamaClient;
import projet.devops.Mail.Classifier.TextCleaner;
import projet.devops.Mail.Mail;
import projet.devops.Mail.Repository.ContactRepository;

import java.util.List;
import java.util.Map;

import jakarta.annotation.PostConstruct;

@Service
public class ContactService {

    private final OllamaClient ollamaClient;
    private final ContactRepository contactRepository; // DIP : On dépend de l'interface

    private Map<String, String> contactsCache;

    public ContactService(OllamaClient ollamaClient, ContactRepository contactRepository) {
        this.ollamaClient = ollamaClient;
        this.contactRepository = contactRepository;
    }

    @PostConstruct
    public void init() {
        // Le chargement est délégué au repository au démarrage
        this.contactsCache = contactRepository.loadContacts();
    }

    public void analyzeAndSaveNewContacts(List<Mail> recentMails) {
        boolean isUpdated = false;
        int limit = Math.min(30, recentMails.size());

        System.out.println("[IA] Vérification des contacts (Filtre anti-spam actif)...");

        for (int i = 0; i < limit; i++) {
            Mail mail = recentMails.get(i);
            String emailAddress = extractEmail(mail.getFrom());

            // 🛡️ Filtre : on n'analyse que les vrais humains
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
            contactRepository.saveContacts(contactsCache); // Délégation de la sauvegarde
            System.out.println("💾 [Contacts] Dictionnaire mis à jour.");
        }
    }

    private String guessExpertise(String content) {
        String lowerContent = content.toLowerCase();

        // --- SÉCURITÉ 1 : Mots-clés prioritaires ---
        if (lowerContent.contains("docker") || lowerContent.contains("ci/cd") || lowerContent.contains("git")) {
            return "Expert DevOps / Infrastructure";
        }
        if (lowerContent.contains("ia") || lowerContent.contains("ollama") || lowerContent.contains("backend")) {
            return "Développeur Backend / IA";
        }
        if (lowerContent.contains("css") || lowerContent.contains("html") || lowerContent.contains("frontend")
                || lowerContent.contains("ui")) {
            return "Designer UI / Frontend";
        }
        if (lowerContent.contains("sql") || lowerContent.contains("base de données") || lowerContent.contains("test")) {
            return "Expert BDD / QA";
        }

        // --- SÉCURITÉ 2 : L'APPEL IA ---
        try {
            String cleanContent = TextCleaner.cleanEmailText(content, 200);
            String prompt = "Analyse ce mail et donne le métier de l'expéditeur en 3 mots.\n" +
                    "Mail : " + cleanContent + "\n" +
                    "Métier :";

            String response = ollamaClient.generateResponse("tinyllama", prompt).trim();

            if (response.length() < 3 || response.contains("Exemple") || response.contains("Attendu")) {
                return "Membre équipe projet";
            }

            if (response.contains("\n")) {
                response = response.split("\n")[0];
            }

            return response.replace("\"", "").trim();

        } catch (Exception e) {
            return "Membre équipe projet";
        }
    }

    private boolean isRealHumanContact(String email) {
        if (email == null || email.isEmpty())
            return false;

        String lowerEmail = email.toLowerCase();
        String[] spamKeywords = {
                "no-reply", "noreply", "newsletter", "info@", "contact@",
                "marketing", "hello@", "support@", "stories", "posts",
                "update", "service.", "news", "market", "advertising", "shein", "instagram"
        };

        for (String keyword : spamKeywords) {
            if (lowerEmail.contains(keyword))
                return false;
        }

        return lowerEmail.endsWith("@parisnanterre.fr") || lowerEmail.contains(".");
    }

    private String extractEmail(String fromRaw) {
        if (fromRaw == null)
            return "";
        if (fromRaw.contains("<") && fromRaw.contains(">")) {
            return fromRaw.substring(fromRaw.indexOf("<") + 1, fromRaw.indexOf(">")).trim();
        }
        return fromRaw.trim();
    }

    public Map<String, String> getAllContacts() {
        return contactsCache;
    }
}