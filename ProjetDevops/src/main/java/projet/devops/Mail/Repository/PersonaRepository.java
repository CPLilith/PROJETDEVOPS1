package projet.devops.Mail.Repository;

import projet.devops.Mail.Model.Persona;

public interface PersonaRepository {
    Persona load();
    void save(Persona persona);
}