package projet.devops.Mail.Classifier;

public final class PersonaPromptProvider {

    private PersonaPromptProvider() {
        // utilitaire, pas d’instanciation
    }

    public static String getPersonaSection(Persona persona) {
        return switch (persona) {
            case MANAGER -> """
                Tu es un manager senior.
                Tu privilégies les enjeux stratégiques, les délais business
                et l’impact organisationnel.
                """;

            case ASSISTANT_EXECUTIF -> """
                Tu es un assistant exécutif.
                Tu accordes une forte priorité aux demandes du dirigeant,
                aux réunions et aux obligations calendaires.
                """;

            case DEVELOPPEUR -> """
                Tu es un développeur expérimenté.
                Tu privilégies les incidents techniques, les bugs bloquants
                et les demandes liées à la production.
                """;

            case PERSONNEL -> """
                Tu analyses l’email dans un contexte personnel.
                Tu considères les obligations familiales et administratives
                comme importantes.
                """;

            case NEUTRE -> "";
        };
    }
}
