package projet.devops.Mail.Gestion;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Component
public class FichierTemp {

    private static final String FILE_PATH = "src/main/resources/mails_temp.txt";
    private final ObjectMapper objectMapper;

    public FichierTemp() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public void sauvegarderMails(List<Map<String, String>> mails) throws IOException {
        try (FileWriter writer = new FileWriter(FILE_PATH, false)) {
            String json = objectMapper.writeValueAsString(mails);
            writer.write(json);
        }
    }

    public List<Map<String, String>> lireMails() throws IOException {
        if (Files.exists(Paths.get(FILE_PATH))) {
            String content = new String(Files.readAllBytes(Paths.get(FILE_PATH)));
            return objectMapper.readValue(content,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
        }
        return null;
    }

    public void supprimerFichier() throws IOException {
        Files.deleteIfExists(Paths.get(FILE_PATH));
    }
}