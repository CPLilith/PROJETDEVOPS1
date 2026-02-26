package projet.devops.Mail.Repository;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository; // Utilisation de @Repository

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Repository
public class JsonCustomTagRepository implements CustomTagRepository {

    private final String storagePath;
    private final ObjectMapper mapper;

    public JsonCustomTagRepository(
            @Value("${app.storage.custom-tags:storage/custom_do_tags.json}") String storagePath) {
        this.storagePath = storagePath;
        // DRY : On utilise l'ObjectMapper fourni par Spring Boot, on l'adapte juste
        // pour notre besoin
        this.mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public List<String> loadTags() {
        try {
            File file = new File(storagePath);
            if (file.exists() && file.length() > 0) {
                return mapper.readValue(file, new TypeReference<List<String>>() {
                });
            }
        } catch (Exception e) {
            System.err.println("❌ [JsonTagRepository] Erreur lecture : " + e.getMessage());
        }
        return new ArrayList<>(); // Retourne une liste vide par défaut si erreur ou fichier absent
    }

    @Override
    public void saveTags(List<String> tags) {
        try {
            File file = new File(storagePath);
            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }
            mapper.writeValue(file, tags);
        } catch (Exception e) {
            System.err.println("❌ [JsonTagRepository] Erreur écriture : " + e.getMessage());
        }
    }
}