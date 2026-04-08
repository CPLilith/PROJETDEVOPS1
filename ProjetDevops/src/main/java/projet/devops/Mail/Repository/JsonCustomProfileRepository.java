package projet.devops.Mail.Repository;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Repository
public class JsonCustomProfileRepository implements CustomProfileRepository {

    private final String storagePath;
    private final ObjectMapper mapper;

    public JsonCustomProfileRepository(
            @Value("${app.storage.custom-profiles:storage/custom_profiles.json}") String storagePath) {
        this.storagePath = storagePath;
        this.mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public List<String> loadProfiles() {
        try {
            File file = new File(storagePath);
            if (file.exists() && file.length() > 0) {
                return mapper.readValue(file, new TypeReference<List<String>>() {});
            }
        } catch (Exception e) {
            System.err.println("❌ [JsonProfileRepository] Erreur lecture : " + e.getMessage());
        }
        return new ArrayList<>();
    }

    @Override
    public void saveProfiles(List<String> profiles) {
        try {
            File file = new File(storagePath);
            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }
            mapper.writeValue(file, profiles);
        } catch (Exception e) {
            System.err.println("❌ [JsonProfileRepository] Erreur écriture : " + e.getMessage());
        }
    }
}
