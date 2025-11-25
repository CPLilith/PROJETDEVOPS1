package projet.devops.Mail.Gestion;

import org.junit.Test;

public class EisenhowerClassifierTest {

    @Test
    public void afficherMailsAvecTags() {
        EisenhowerClassifier classifier = new EisenhowerClassifier();

        // Exemple de mails à classifier
        String[][] mails = {
            {"Google <no-reply@accounts.google.com>", "Alerte de sécurité", "mot de passe d'appli créé... quelqu'un utilise peut-être votre compte"},
            {"newsletter@test.com", "Promo", "Profitez de notre offre"},
            {"support@bank.com", "Notification importante", "Votre relevé bancaire est disponible"},
            {"friend@example.com", "Salut !", "On se voit ce week-end ?"}
        };

        // Parcourir les mails et afficher leur tag
        for (String[] mail : mails) {
            String sender = mail[0];
            String subject = mail[1];
            String body = mail[2];

            EisenhowerTag tag = classifier.classifyMail(sender, subject, body);

            System.out.println("Mail:");
            System.out.println("  Expéditeur : " + sender);
            System.out.println("  Sujet      : " + subject);
            System.out.println("  Contenu    : " + body);
            System.out.println("  Tag        : " + tag);
            System.out.println();
        }
    }
}