package projet.devops.Mail.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Repository;

import projet.devops.Mail.Mail;
import projet.devops.Mail.Gestion.FichierTemp;

@Repository // SRP : Son seul rôle est de traduire les Mails pour FichierTemp
public class LocalMailCacheRepository implements MailCacheRepository {

    private final FichierTemp fichierTemp;

    public LocalMailCacheRepository(FichierTemp fichierTemp) {
        this.fichierTemp = fichierTemp;
    }

    @Override
    public void saveCache(List<Mail> mails) {
        try {
            List<Map<String, String>> dataToSave = new ArrayList<>();
            for (Mail m : mails) {
                Map<String, String> map = new HashMap<>();
                map.put("messageId", m.getMessageId());
                map.put("subject", m.getSubject());
                map.put("from", m.getFrom());
                map.put("content", m.getContent());
                map.put("date", m.getDate());
                map.put("action", m.getAction().name());
                map.put("status", m.getStatus() != null ? m.getStatus() : "");
                dataToSave.add(map);
            }
            fichierTemp.sauvegarderMails(dataToSave);
        } catch (Exception ignored) {
            // Log erreur si besoin
        }
    }

    @Override
    public List<Mail> loadCache() {
        List<Mail> loadedMails = new ArrayList<>();
        try {
            List<Map<String, String>> loadedData = fichierTemp.lireMails();
            if (loadedData != null && !loadedData.isEmpty()) {
                for (Map<String, String> map : loadedData) {
                    Mail m = new Mail(map.get("messageId"), map.get("date"), map.get("subject"),
                            map.get("from"), map.get("content"));
                    m.setAction(map.get("action"));
                    m.setStatus(map.get("status"));
                    loadedMails.add(m);
                }
            }
        } catch (Exception ignored) {
            // Log erreur si besoin
        }
        return loadedMails;
    }
}