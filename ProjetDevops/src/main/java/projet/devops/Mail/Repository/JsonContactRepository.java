package projet.devops.Mail.Repository;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Repository // SRP : Son unique rôle est de gérer la persistance des contacts
public class JsonContactRepository implements ContactRepository {

    private final String filePath;
    private final ObjectMapper objectMapper;

    // DRY : Injection du chemin et de l'ObjectMapper global
    public JsonContactRepository(
            @Value("${app.storage.contacts:storage/contacts.json}") String filePath) {
        this.filePath = filePath;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Map<String, String> loadContacts() {
        try {
            File file = new File(filePath);
            if (file.exists() && file.length() > 0) {
                Map<String, String> contacts = objectMapper.readValue(file, new TypeReference<Map<String, String>>() {
                });
                System.out.println("✅ [Contacts] " + contacts.size() + " profils chargés depuis le JSON.");
                return contacts;
            }
        } catch (Exception e) {
            System.err.println("❌ [JsonContactRepository] Erreur lecture : " + e.getMessage());
        }
        return new HashMap<>(); // Ne jamais renvoyer null
    }

    @Override
    public void saveContacts(Map<String, String> contacts) {
        try {
            File file = new File(filePath);
            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs(); // Crée le dossier storage si besoin
            }
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, contacts);
        } catch (Exception e) {
            System.err.println("❌ [JsonContactRepository] Erreur sauvegarde contacts.json : " + e.getMessage());
        }
    }
}