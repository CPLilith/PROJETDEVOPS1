package projet.devops.Mail.Service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import projet.devops.Mail.Model.CalendarIntent;

@Service
public class CalendarIntelligenceService {

    // Stockage temporaire pour faire le lien entre la détection et la confirmation
    private final Map<String, CalendarIntent> intentStorage = new HashMap<>();

    /**
     * Récupère une intention précédemment analysée
     */
    public CalendarIntent getIntent(String messageId) {
        System.out.println("🔍 [DEBUG] Recherche de l'intention pour le messageId : " + messageId);
        CalendarIntent intent = intentStorage.getOrDefault(messageId, new CalendarIntent());
        System.out.println("🔍 [DEBUG] Résultat de la recherche : " + (intentStorage.containsKey(messageId) ? "Trouvé en mémoire" : "Non trouvé (Création d'une nouvelle instance vide)"));
        return intent;
    }

    /**
     * Analyse le mail et prépare l'objet d'intention
     */
    public CalendarIntent analyzeAndStoreIntent(String messageId, String subject, String content) {
        System.out.println("\n==================================================");
        System.out.println("[AgendaObserver] Déclenchement de l'IA pour : " + subject);
        System.out.println("⚙️ [DEBUG] ID du message en cours de traitement : " + messageId);
        
        // 1. On récupère la vraie date d'aujourd'hui
        String dateDuJour = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        System.out.println("⚙️ [DEBUG] Date du jour injectée : " + dateDuJour);
        
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
        System.out.println("⚙️ [DEBUG] Simulation de l'appel à l'IA en cours...");
        String vraieReponseIA = "[STRATEGIE] = PLAN\n" +
                                "[DATE] = INCONNUE\n" +
                                "[RELANCE] = \n" +
                                "[TITRE] = " + subject;
        
        System.out.println("🕵️ REPONSE BRUTE IA :\n" + vraieReponseIA);
        
        // 4. On transforme le vrai texte de l'IA en objet Java
        System.out.println("⚙️ [DEBUG] Envoi de la réponse brute vers le parseur...");
        CalendarIntent intent = parseResponseToIntent(vraieReponseIA);
        
        // 5. On injecte le contenu original du mail (Pour le bug du 'null')
        System.out.println("⚙️ [DEBUG] Injection du contenu original du mail dans l'objet Intent.");
        intent.setFullMailContent(content); 
        
        // 6. On stocke en mémoire pour la confirmation Web
        System.out.println("⚙️ [DEBUG] Sauvegarde de l'Intent dans la Map 'intentStorage' avec la clé : " + messageId);
        intentStorage.put(messageId, intent);
        System.out.println("==================================================\n");

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
            System.out.println("🛠️ [DEBUG-PARSER] Début de l'analyse ligne par ligne (" + lines.length + " lignes à traiter)");
            
            for (String line : lines) {
                line = line.trim();
                
                if (line.startsWith("[STRATEGIE] =") || line.startsWith("[STRATEGIE]=")) {
                    String value = line.substring(line.indexOf("=") + 1).trim();
                    intent.setStrategy(value);
                    System.out.println("   -> [STRATEGIE] extraite : '" + value + "'");
                } 
                else if (line.startsWith("[DATE] =") || line.startsWith("[DATE]=")) {
                    String dateStr = line.substring(line.indexOf("=") + 1).trim();
                    // On s'assure de ne pas valider une date "INCONNUE"
                    if (!dateStr.equalsIgnoreCase("INCONNUE") && !dateStr.isEmpty()) {
                        intent.setDeadline(dateStr);
                        intent.setDateDetected(true);
                        System.out.println("   -> [DATE] valide détectée : '" + dateStr + "' (DateDetected = true)");
                    } else {
                        intent.setDeadline("INCONNUE");
                        intent.setDateDetected(false);
                        System.out.println("   -> [DATE] non valide ou INCONNUE (DateDetected = false)");
                    }
                } 
                else if (line.startsWith("[RELANCE] =") || line.startsWith("[RELANCE]=")) {
                    String value = line.substring(line.indexOf("=") + 1).trim();
                    intent.setFollowUpDate(value);
                    System.out.println("   -> [RELANCE] extraite : '" + value + "'");
                }
                else if (line.startsWith("[ASSIGNE] =") || line.startsWith("[ASSIGNE]=")) {
                    String value = line.substring(line.indexOf("=") + 1).trim();
                    intent.setAssignee(value);
                    System.out.println("   -> [ASSIGNE] extrait : '" + value + "'");
                }
                else if (line.startsWith("[TITRE] =") || line.startsWith("[TITRE]=")) {
                    String value = line.substring(line.indexOf("=") + 1).trim();
                    intent.setTitle(value);
                    System.out.println("   -> [TITRE] extrait : '" + value + "'");
                }
                else if (line.startsWith("[CONFIANCE] =") || line.startsWith("[CONFIANCE]=")) {
                    String value = line.substring(line.indexOf("=") + 1).trim();
                    intent.setConfidence(value);
                    System.out.println("   -> [CONFIANCE] extraite : '" + value + "'");
                }
            }
            System.out.println("🛠️ [DEBUG-PARSER] Fin du parsing avec succès.");
            
        } catch (Exception e) {
            System.err.println("❌ Erreur de parsing IA : " + e.getMessage());
            e.printStackTrace(); // Ajout de la stacktrace complète pour le debug
        }
        
        return intent;
    }
}