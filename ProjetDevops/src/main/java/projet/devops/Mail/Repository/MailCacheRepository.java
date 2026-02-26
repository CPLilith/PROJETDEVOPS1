package projet.devops.Mail.Repository;

import java.util.List;
import projet.devops.Mail.Mail;

public interface MailCacheRepository {
    void saveCache(List<Mail> mails);

    List<Mail> loadCache();
}