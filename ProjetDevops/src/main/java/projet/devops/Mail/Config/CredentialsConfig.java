package projet.devops.Mail.Config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class CredentialsConfig {

    private final JsonNode root;

    public CredentialsConfig() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        this.root = mapper.readTree(new ClassPathResource("credentials.json").getInputStream());
    }

    @Bean
    public String notionToken() {
        return root.get("notion_token").asText();
    }
}