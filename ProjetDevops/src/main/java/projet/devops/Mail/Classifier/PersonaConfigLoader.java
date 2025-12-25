package projet.devops.Mail.Classifier;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public final class PersonaConfigLoader {

    private static final String PERSONA_FILE = "persona.txt";
    private static final Persona DEFAULT_PERSONA = Persona.NEUTRE;

    private PersonaConfigLoader() {
        // utilitaire
    }

    public static Persona loadPersona() {
        try (InputStream is = PersonaConfigLoader.class
                .getClassLoader()
                .getResourceAsStream(PERSONA_FILE)) {

            if (is == null) {
                return DEFAULT_PERSONA;
            }

            try (BufferedReader reader =
                     new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

                String line = reader.readLine();

                if (line == null || line.isBlank()) {
                    return DEFAULT_PERSONA;
                }

                return Persona.valueOf(line.trim().toUpperCase());
            }

        } catch (Exception e) {
            return DEFAULT_PERSONA;
        }
    }
}
