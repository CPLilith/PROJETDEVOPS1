package projet.devops.Mail.Classifier;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class PersonaResourceService {

    private static final Persona DEFAULT_PERSONA = Persona.NEUTRE;

    // Chemin PHYSIQUE vers resources (DEV uniquement)
    private static final Path PERSONA_FILE =
            Path.of("src", "main", "resources", "persona.txt");

    private PersonaResourceService() {
    }

    public static Persona loadPersona() {
        try {
            if (!Files.exists(PERSONA_FILE)) {
                return DEFAULT_PERSONA;
            }

            String value = Files.readString(PERSONA_FILE, StandardCharsets.UTF_8).trim();

            if (value.isEmpty()) {
                return DEFAULT_PERSONA;
            }

            return Persona.valueOf(value.toUpperCase());

        } catch (Exception e) {
            return DEFAULT_PERSONA;
        }
    }

    public static void savePersona(Persona persona) throws IOException {
        Files.writeString(
                PERSONA_FILE,
                persona.name(),
                StandardCharsets.UTF_8
        );
    }
}