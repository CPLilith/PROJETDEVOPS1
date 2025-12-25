package projet.devops.Mail.Classifier;

public final class PersonaPromptProvider {

    private PersonaPromptProvider() {
        // utilitaire, pas d’instanciation
    }

    public static String getPersonaSection(Persona persona) {
        return switch (persona) {
            case ETUDIANT -> """
                Tu es un étudiant.
                Tu privilégies les emails liés aux examens, aux devoirs,
                aux délais universitaires et aux communications pédagogiques.
                """;

            case DEVELOPPEUR -> """
                Tu es un développeur.
                Tu privilégies les incidents techniques, les bugs bloquants,
                les délais de livraison et les demandes liées au code.
                """;

            case PROFESSEUR -> """
                Tu es un professeur.
                Tu privilégies les obligations d’enseignement,
                les évaluations, les réunions académiques
                et les communications institutionnelles.
                """;

            case NEUTRE -> "";
        };
    }
}
