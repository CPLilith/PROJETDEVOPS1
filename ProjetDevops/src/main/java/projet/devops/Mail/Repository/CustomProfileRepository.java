package projet.devops.Mail.Repository;

import java.util.List;

public interface CustomProfileRepository {
    List<String> loadProfiles();
    void saveProfiles(List<String> profiles);
}
