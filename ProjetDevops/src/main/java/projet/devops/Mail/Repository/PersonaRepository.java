package projet.devops.Mail.Repository;

import projet.devops.Mail.Classifier.Persona;

public interface PersonaRepository {
    Persona load();
    void save(Persona persona);
}