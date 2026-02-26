package projet.devops.Mail.Service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import projet.devops.Mail.Repository.CustomTagRepository;

/**
 * Service gérant le cycle de vie des tags personnalisés de la matrice
 * d'Eisenhower.
 * Permet de créer, supprimer et formater des sous-catégories pour l'action
 * "DO".
 */
@Service
public class CustomDoTagService {

    private static final String PREFIX = "DO_";

    private final CustomTagRepository repository;
    private List<String> customTags = new ArrayList<>();

    public CustomDoTagService(CustomTagRepository repository) {
        this.repository = repository;
    }

    /**
     * Charge les tags existants depuis la persistance au démarrage de
     * l'application.
     */
    @PostConstruct
    public void init() {
        this.customTags = repository.loadTags();
    }

    /**
     * Retourne une copie immuable de la liste des tags pour protéger l'intégrité de
     * la donnée.
     */
    public List<String> getCustomTags() {
        return List.copyOf(customTags);
    }

    /**
     * Crée un nouveau tag à partir d'un libellé utilisateur.
     * Le libellé est normalisé (Majuscules, remplacement des caractères spéciaux
     * par des underscores).
     * * @param label Le nom du tag (ex: "Urgence Client")
     * 
     * @return Le tag normalisé (ex: "DO_URGENCE_CLIENT") ou null si
     *         invalide/doublon.
     */
    public String createTag(String label) {
        if (label == null || label.isBlank()) {
            return null;
        }

        // Normalisation : On ne garde que l'essentiel pour garantir la compatibilité
        // des IDs CSS/HTML
        String normalized = PREFIX + label.trim()
                .toUpperCase()
                .replaceAll("[^A-Z0-9]", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");

        if (customTags.contains(normalized)) {
            return null;
        }

        // Mise à jour immédiate du cache mémoire pour garantir la réactivité de l'UI
        customTags.add(normalized);
        repository.saveTags(customTags);

        return normalized;
    }

    /**
     * Supprime un tag et met à jour la persistance.
     */
    public boolean deleteTag(String tagName) {
        boolean removed = customTags.remove(tagName);
        if (removed) {
            repository.saveTags(customTags);
        }
        return removed;
    }

    /**
     * Convertit un nom technique de tag en libellé élégant pour l'utilisateur.
     * Exemple : "DO_FORMATION_RH" -> "DO · Formation Rh"
     */
    public static String toLabel(String tagName) {
        if (tagName == null)
            return "";
        if (tagName.equals("DO"))
            return "DO";

        String suffix = tagName.startsWith(PREFIX) ? tagName.substring(PREFIX.length()) : tagName;
        StringBuilder sb = new StringBuilder("DO · ");

        for (String w : suffix.split("_")) {
            if (!w.isEmpty()) {
                sb.append(Character.toUpperCase(w.charAt(0)));
                if (w.length() > 1) {
                    sb.append(w.substring(1).toLowerCase());
                }
                sb.append(" ");
            }
        }
        return sb.toString().trim();
    }
}