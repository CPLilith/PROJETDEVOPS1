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
        try {
            File file = new File(filePath);
            if (!file.exists() || file.length() == 0) {
                return DEFAULT_PERSONA;
            }
            String value = Files.readString(file.toPath(), StandardCharsets.UTF_8).trim();
            if (value.isEmpty()) return DEFAULT_PERSONA;
            return Persona.valueOf(value.toUpperCase());
        } catch (Exception e) {
            System.err.println("[JsonPersonaRepository] Erreur lecture : " + e.getMessage());
            return DEFAULT_PERSONA;
        }
    }

    @Override
    public void save(Persona persona) {
        try {
            File file = new File(filePath);
            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }
            Files.writeString(file.toPath(), persona.name(), StandardCharsets.UTF_8);
            System.out.println("[JsonPersonaRepository] Persona sauvegardé : " + persona.name());
        } catch (IOException e) {
            System.err.println("[JsonPersonaRepository] Erreur sauvegarde : " + e.getMessage());
        }
    }
}