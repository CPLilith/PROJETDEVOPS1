package projet.devops.Mail.Repository;

import java.util.List;

public interface CustomTagRepository {
    List<String> loadTags();

    void saveTags(List<String> tags);
}