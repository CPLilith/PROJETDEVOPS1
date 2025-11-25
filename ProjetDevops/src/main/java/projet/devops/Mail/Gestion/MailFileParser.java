package projet.devops.Mail.Gestion;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

public class MailFileParser {

    private final ObjectMapper mapper = new ObjectMapper();

    public List<Map<String, Object>> readMails(String filePath) throws Exception {

        List<Map<String, Object>> mails = new ArrayList<>();
        StringBuilder buffer = new StringBuilder();

        for (String line : Files.readAllLines(Paths.get(filePath))) {
            buffer.append(line);

            if (line.contains("}")) {
                String json = buffer.toString();
                mails.add(mapper.readValue(json, Map.class));
                buffer = new StringBuilder();
            }
        }

        return mails;
    }
}
