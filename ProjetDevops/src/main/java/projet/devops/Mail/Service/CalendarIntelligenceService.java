package projet.devops.Mail.Service;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import projet.devops.Mail.Classifier.OllamaClient;
import projet.devops.Mail.Model.CalendarIntent;

@Service
public class CalendarIntelligenceService extends AiServiceInterface {

    private final Map<String, CalendarIntent> intentCache = new ConcurrentHashMap<>();

    // Nouveaux Patterns Regex ultra-stricts
    private static final Pattern STRATEGY_PATTERN = Pattern.compile("\\[STRATEGIE\\]\\s*=\\s*(PLAN|BOOMERANG|TOTAL)");
    private static final Pattern DATE_PATTERN = Pattern.compile("\\d{2}/\\d{2}/\\d{4}");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}");

    public CalendarIntelligenceService(OllamaClient client) {
        super(client);
    }

    public void analyzeAndStoreIntent(String messageId, String subject, String content) {
        String inputContext = "Sujet: " + subject + "\nContenu: " + content;
        String rawResponse = execute(inputContext);
        
        System.out.println("🕵️ REPONSE BRUTE IA :\n" + rawResponse);
        System.out.println("-------------------------");
        
        CalendarIntent intent = parseResponseToIntent(rawResponse, subject);
        intentCache.put(messageId, intent);
        
        System.out.println("🗓️ [Calendar IA] Intention générée pour : " + subject + " -> " + intent.getStrategy() + " | Date: " + intent.getDeadline());
    }

    public CalendarIntent getIntent(String messageId) {
        return intentCache.get(messageId);
    }

    @Override
    protected String buildPrompt(String input) {
        String cleanText = TextCleaner.cleanEmailText(input, 400);
        // Prompt format "Formulaire à trous" pour forcer l'IA à être brève
        return """
            Lis ce mail et remplis CE formulaire. Ne fais AUCUNE phrase. 
            Réponds uniquement avec les valeurs.

            [STRATEGIE] = (Choisis un seul mot : PLAN, BOOMERANG, ou TOTAL)
            [ECHEANCE] = (Date au format JJ/MM/AAAA ou INCONNUE)
            [CIBLE] = (Email ou INCONNU)
            
            Mail : "%s"
            """.formatted(cleanText);
    }

    @Override
    protected String parseResult(String rawResponse) {
        return rawResponse; 
    }

    @Override
    protected String getDefaultResult() {
        return "[STRATEGIE] = PLAN";
    }

    private CalendarIntent parseResponseToIntent(String aiResponse, String subject) {
        CalendarIntent intent = new CalendarIntent();
        intent.setTitle("Action : " + subject);
        intent.setDeadline("INCONNUE");
        intent.setAssignee("");

        String upperResp = aiResponse.toUpperCase();

        // 1. Extraction de la stratégie avec Regex
        Matcher stratMatcher = STRATEGY_PATTERN.matcher(upperResp);
        if (stratMatcher.find()) {
            String match = stratMatcher.group(1);
            if (match.equals("BOOMERANG")) intent.setStrategy(CalendarIntent.Strategy.BOOMERANG);
            else if (match.equals("TOTAL")) intent.setStrategy(CalendarIntent.Strategy.TOTAL_DELEGATION);
            else intent.setStrategy(CalendarIntent.Strategy.SIMPLE_PLAN);
        } else {
            // ASTUCE DE SURVIE : Si l'IA a fait des phrases malgré nos ordres, 
            // on cherche quel mot-clé elle a prononcé en DERNIER !
            int idxPlan = Math.max(upperResp.lastIndexOf("PLAN"), upperResp.lastIndexOf("SIMPLE_PLAN"));
            int idxBoom = upperResp.lastIndexOf("BOOMERANG");
            int idxTotal = Math.max(upperResp.lastIndexOf("TOTAL"), upperResp.lastIndexOf("TOTAL_DELEGATION"));
            
            int maxIdx = Math.max(idxPlan, Math.max(idxBoom, idxTotal));
            
            if (maxIdx == idxBoom && idxBoom > -1) {
                intent.setStrategy(CalendarIntent.Strategy.BOOMERANG);
            } else if (maxIdx == idxTotal && idxTotal > -1) {
                intent.setStrategy(CalendarIntent.Strategy.TOTAL_DELEGATION);
            } else {
                intent.setStrategy(CalendarIntent.Strategy.SIMPLE_PLAN); // Par défaut
            }
        }

        // 2. Extraction de la date (inchangé, ça marchait super bien !)
        Matcher dateMatcher = DATE_PATTERN.matcher(aiResponse);
        if (dateMatcher.find()) {
            intent.setDeadline(dateMatcher.group());
        }

        // 3. Extraction de l'email
        Matcher emailMatcher = EMAIL_PATTERN.matcher(aiResponse);
        if (emailMatcher.find()) {
            intent.setAssignee(emailMatcher.group());
        }

        // --- LOGIQUE METIER ---
        intent.setDateDetected(!intent.getDeadline().equals("INCONNUE"));
        
        if (intent.isDateDetected()) {
            if (intent.getStrategy() == CalendarIntent.Strategy.BOOMERANG) {
                intent.setFollowUpDate(intent.getDeadline() + " (Rappel à anticiper)"); 
            } else {
                intent.setFollowUpDate(intent.getDeadline());
            }
        } else {
            intent.setFollowUpDate("");
        }

        return intent;
    }
}