package projet.devops.Mail.Service;

import projet.devops.Mail.Model.Persona;

public final class PersonaPromptProvider {

    private PersonaPromptProvider() {
        // utilitaire, pas d'instanciation
    }

    public static String getPersonaSection(Persona persona) {
        return getPersonaSectionFromRaw(persona.name());
    }

    public static String getPersonaSectionFromRaw(String raw) {
        if (raw == null || raw.isBlank()) return "";
        String upper = raw.trim().toUpperCase();

        return switch (upper) {
            case "ETUDIANT" -> """
                    Tu es un étudiant.
                    Tu privilégies les emails liés aux examens, aux devoirs,
                    aux délais universitaires et aux communications pédagogiques.
                    """;

            case "DEVELOPPEUR" -> """
                    Tu es un développeur.
                    Tu privilégies les incidents techniques, les bugs bloquants,
                    les délais de livraison et les demandes liées au code.
                    """;

            case "PROFESSEUR" -> """
                    Tu es un professeur.
                    Tu privilégies les obligations d'enseignement,
                    les évaluations, les réunions académiques
                    et les communications institutionnelles.
                    """;

            case "NEUTRE" -> "";

            default -> {
                String displayName = upper
                        .replace("PROFIL_", "")
                        .replace("_", " ")
                        .toLowerCase();
                yield String.format("""
                    Tu analyses les emails en adoptant le profil personnalisé "%s".
                    Classe chaque email selon les priorités et les responsabilités
                    typiques de ce profil.
                    """, displayName);
            }
        };
    }
}