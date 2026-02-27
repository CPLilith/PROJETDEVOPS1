package projet.devops.Mail.Classifier;

import java.util.List;

import projet.devops.Mail.Model.EisenhowerAction;
import projet.devops.Mail.Model.Mail;
import projet.devops.Mail.Model.Persona;

public interface ClassificationStrategy extends Nameable {
    EisenhowerAction classify(Mail mail, Persona persona, List<String> customTags);
}