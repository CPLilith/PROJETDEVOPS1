package projet.devops.Mail.Repository;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import projet.devops.Mail.Model.Persona;

@Repository
public class JsonPersonaRepository implements PersonaRepository {

    private static final Persona DEFAULT_PERSONA = Persona.NEUTRE;

    private final String filePath;

    public JsonPersonaRepository(
            @Value("${app.storage.persona:storage/persona.txt}") String filePath) {
        this.filePath = filePath;
    }

    @Override
    public Persona load() {
        String raw = loadRaw();
        try {
            return Persona.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException e) {
            return DEFAULT_PERSONA;
        }
    }

    @Override
    public String loadRaw() {
        try {
            File file = new File(filePath);
            if (!file.exists() || file.length() == 0) return DEFAULT_PERSONA.name();
            String value = Files.readString(file.toPath(), StandardCharsets.UTF_8).trim();
            return value.isEmpty() ? DEFAULT_PERSONA.name() : value;
        } catch (Exception e) {
            System.err.println("[JsonPersonaRepository] Erreur lecture : " + e.getMessage());
            return DEFAULT_PERSONA.name();
        }
    }

    @Override
    public void save(Persona persona) {
        saveRaw(persona.name());
    }

    @Override
    public void saveRaw(String raw) {
        try {
            File file = new File(filePath);
            if (file.getParentFile() != null) file.getParentFile().mkdirs();
            Files.writeString(file.toPath(), raw, StandardCharsets.UTF_8);
            System.out.println("[JsonPersonaRepository] Profil sauvegardé : " + raw);
        } catch (IOException e) {
            System.err.println("[JsonPersonaRepository] Erreur sauvegarde : " + e.getMessage());
        }
    }
}