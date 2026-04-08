package projet.devops.Mail.Repository;

import projet.devops.Mail.Model.Persona;

public interface PersonaRepository {
    Persona load();
    void save(Persona persona);

    default String loadRaw() { return load().name(); }
    default void saveRaw(String raw) {
        try { save(Persona.valueOf(raw.toUpperCase())); }
        catch (IllegalArgumentException e) { save(Persona.NEUTRE); }
    }
}