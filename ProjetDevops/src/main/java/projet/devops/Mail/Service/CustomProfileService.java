package projet.devops.Mail.Service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import projet.devops.Mail.Repository.CustomProfileRepository;

@Service
public class CustomProfileService {

    private final CustomProfileRepository profileRepository;

    public CustomProfileService(CustomProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    public List<String> getProfiles() {
        return profileRepository.loadProfiles();
    }

    /**
     * Crée un profil personnalisé et retourne son nom interne (ex: "PROFIL_Dev"),
     * ou null si invalide/doublon.
     */
    public String createProfile(String label) {
        if (label == null || label.isBlank()) return null;
        String cleaned = label.trim().replaceAll("[^a-zA-Z0-9 _-]", "");
        if (cleaned.isBlank()) return null;
        String internal = "PROFIL_" + cleaned.toUpperCase().replace(" ", "_");

        List<String> profiles = new ArrayList<>(profileRepository.loadProfiles());
        if (profiles.contains(internal)) return null;
        profiles.add(internal);
        profileRepository.saveProfiles(profiles);
        return internal;
    }

    public void deleteProfile(String profileName) {
        List<String> profiles = new ArrayList<>(profileRepository.loadProfiles());
        profiles.remove(profileName);
        profileRepository.saveProfiles(profiles);
    }

    /**
     * Converts internal name like "PROFIL_DEV_SENIOR" to "DEV SENIOR"
     */
    public static String toLabel(String internal) {
        if (internal == null) return "";
        return internal.replace("PROFIL_", "").replace("_", " ");
    }
}
