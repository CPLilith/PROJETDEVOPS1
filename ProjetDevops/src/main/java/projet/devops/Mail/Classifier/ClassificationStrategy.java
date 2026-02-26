package projet.devops.Mail.Classifier;

import java.util.List;
import projet.devops.Mail.Mail;

public interface ClassificationStrategy extends Nameable {
    EisenhowerAction classify(Mail mail, Persona persona, List<String> customTags);
}