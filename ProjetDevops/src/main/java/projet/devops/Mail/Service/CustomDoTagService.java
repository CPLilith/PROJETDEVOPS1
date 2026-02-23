package projet.devops.Mail.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import jakarta.annotation.PostConstruct;

@Service
public class CustomDoTagService {

    private final String storagePath;
    private final ObjectMapper mapper;
    private List<String> customTags = new ArrayList<>();

    public CustomDoTagService(
            @Value("${app.storage.custom-tags:storage/custom_do_tags.json}") String storagePath) {
        this.storagePath = storagePath;
        this.mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    }

    @PostConstruct
    public void init() {
        try {
            File file = new File(storagePath);
            if (file.exists() && file.length() > 0) {
                customTags = mapper.readValue(file, new TypeReference<List<String>>() {
                });
                System.out.println("✅ [CustomTags] " + customTags.size() + " tags chargés.");
            }
        } catch (Exception e) {
            System.err.println("❌ [CustomTags] Erreur : " + e.getMessage());
            customTags = new ArrayList<>();
        }
    }

    /** Tous les tags DO custom */
    public List<String> getCustomTags() {
        return List.copyOf(customTags);
    }

    /**
     * Crée un tag custom. "Formation RH" → "DO_FORMATION_RH"
     * 
     * @return le nom normalisé, ou null si doublon/invalide
     */
    public String createTag(String label) {
        if (label == null || label.isBlank())
            return null;

        String normalized = "DO_" + label.trim()
                .toUpperCase()
                .replaceAll("[^A-Z0-9]", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");

        if (customTags.contains(normalized))
            return null;

        customTags.add(normalized);
        persist();
        return normalized;
    }

    /** Supprime un tag custom */
    public boolean deleteTag(String tagName) {
        boolean removed = customTags.remove(tagName);
        if (removed)
            persist();
        return removed;
    }

    /**
     * Label lisible : "DO_FORMATION_RH" → "DO · Formation Rh"
     */
    public static String toLabel(String tagName) {
        if (tagName == null)
            return "";
        if (tagName.equals("DO"))
            return "DO";
        String suffix = tagName.startsWith("DO_") ? tagName.substring(3) : tagName;
        StringBuilder sb = new StringBuilder("DO · ");
        for (String w : suffix.split("_")) {
            if (!w.isEmpty()) {
                sb.append(Character.toUpperCase(w.charAt(0)));
                if (w.length() > 1)
                    sb.append(w.substring(1).toLowerCase());
                sb.append(" ");
            }
        }
        return sb.toString().trim();
    }

    private void persist() {
        try {
            File file = new File(storagePath);
            if (file.getParentFile() != null)
                file.getParentFile().mkdirs();
            mapper.writeValue(file, customTags);
        } catch (Exception e) {
            System.err.println("❌ [CustomTags] Erreur écriture : " + e.getMessage());
        }
    }
}