package projet.devops.Mail.Service;

import org.springframework.stereotype.Service;
import projet.devops.Mail.Model.CalendarIntent;
import java.util.HashMap;
import java.util.Map;

@Service
public class CalendarIntelligenceService {

    // Stockage temporaire pour faire le lien entre la détection et la confirmation
    private final Map<String, CalendarIntent> intentStorage = new HashMap<>();

    /**
     * Récupère une intention précédemment analysée
     */
    public CalendarIntent getIntent(String messageId) {
        return intentStorage.getOrDefault(messageId, new CalendarIntent());
    }

    /**
     * Analyse le mail et prépare l'objet d'intention
     */
    public CalendarIntent analyzeAndStoreIntent(String messageId, String subject, String content) {
        System.out.println("[AgendaObserver] Déclenchement de l'IA pour : " + subject);
        
        // 1. On récupère la vraie date d'aujourd'hui
        String dateDuJour = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        
        // 2. Le prompt complet avec les règles strictes
        String prompt = "Tu es un assistant d'agenda ultra-précis.\n" +
                        "⚠️ INFORMATION CRUCIALE : Aujourd'hui, nous sommes le " + dateDuJour + ".\n\n" +
                        "Analyse ce mail et extrais les informations demandées. Respecte CES RÈGLES À LA LETTRE :\n" +
                        "- Si le mail dit 'demain', 'lundi prochain', etc., calcule la date exacte en te basant sur la date d'aujourd'hui.\n" +
                        "- Format obligatoire : JJ/MM/AAAA.\n" +
                        "- 🚨 RÈGLE D'OR : Si AUCUNE date n'est mentionnée, NE DEDUIS RIEN ET N'INVENTE RIEN. Écris exactement : INCONNUE\n\n" +
                        "Sujet du mail : " + subject + "\n" +
                        "Contenu du mail : \n" + content;
                        
        System.out.println("Prompt prêt pour l'IA :\n" + prompt);
        
        // 🚨 3. C'EST ICI QU'ON BRANCHE LE VRAI CERVEAU 🚨
        // Remplace cette ligne par l'appel à ta vraie classe qui gère Ollama/l'IA !
        String vraieReponseIA = "[STRATEGIE] = PLAN\n" +
                                "[DATE] = INCONNUE\n" +
                                "[RELANCE] = \n" +
                                "[TITRE] = " + subject;
        
        System.out.println("🕵️ REPONSE BRUTE IA :\n" + vraieReponseIA);
        
        // 4. On transforme le vrai texte de l'IA en objet Java
        CalendarIntent intent = parseResponseToIntent(vraieReponseIA);
        
        // 5. On injecte le contenu original du mail (Pour le bug du 'null')
        intent.setFullMailContent(content); 
        
        // 6. On stocke en mémoire pour la confirmation Web
        intentStorage.put(messageId, intent);

        return intent;
    }

    /**
     * Parseur de la réponse brute de l'IA
     */
    private CalendarIntent parseResponseToIntent(String rawResponse) {
        CalendarIntent intent = new CalendarIntent();
        
        // Valeurs par défaut sécurisées
        intent.setDateDetected(false);
        intent.setDeadline("INCONNUE");
        intent.setStrategy("PLAN");
        intent.setAssignee("");
        intent.setConfidence("AUCUNE");
        intent.setFollowUpDate("");
        intent.setTitle("Nouveau RDV IA");

        try {
            String[] lines = rawResponse.split("\n");
            for (String line : lines) {
                line = line.trim();
                
                if (line.startsWith("[STRATEGIE] =") || line.startsWith("[STRATEGIE]=")) {
                    intent.setStrategy(line.substring(line.indexOf("=") + 1).trim());
                } 
                else if (line.startsWith("[DATE] =") || line.startsWith("[DATE]=")) {
                    String dateStr = line.substring(line.indexOf("=") + 1).trim();
                    // On s'assure de ne pas valider une date "INCONNUE"
                    if (!dateStr.equalsIgnoreCase("INCONNUE") && !dateStr.isEmpty()) {
                        intent.setDeadline(dateStr);
                        intent.setDateDetected(true);
                    } else {
                        intent.setDeadline("INCONNUE");
                        intent.setDateDetected(false);
                    }
                } 
                else if (line.startsWith("[RELANCE] =") || line.startsWith("[RELANCE]=")) {
                    intent.setFollowUpDate(line.substring(line.indexOf("=") + 1).trim());
                }
                else if (line.startsWith("[ASSIGNE] =") || line.startsWith("[ASSIGNE]=")) {
                    intent.setAssignee(line.substring(line.indexOf("=") + 1).trim());
                }
                else if (line.startsWith("[TITRE] =") || line.startsWith("[TITRE]=")) {
                    intent.setTitle(line.substring(line.indexOf("=") + 1).trim());
                }
                else if (line.startsWith("[CONFIANCE] =") || line.startsWith("[CONFIANCE]=")) {
                    intent.setConfidence(line.substring(line.indexOf("=") + 1).trim());
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur de parsing IA : " + e.getMessage());
        }
        
        return intent;
    }
}