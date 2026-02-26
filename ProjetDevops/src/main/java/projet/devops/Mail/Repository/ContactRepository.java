package projet.devops.Mail.Repository;

import java.util.Map;

public interface ContactRepository {
    Map<String, String> loadContacts();

    void saveContacts(Map<String, String> contacts);
}