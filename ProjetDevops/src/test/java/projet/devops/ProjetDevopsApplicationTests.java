package projet.devops;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import projet.devops.Mail.Classifier.EisenhowerAction;
import projet.devops.Mail.Mail;
import projet.devops.Mail.Service.MailFlowService;

@SpringBootTest
class ProjetDevopsApplicationTests {

    // On ne déclare le service QU'UNE SEULE FOIS pour toute la classe
    @Autowired
    private MailFlowService service; 

    @Test
    void testUpdateMailTag() {
        // 1. On crée un mail de test
        Mail testMail = new Mail("ID1", "2026", "Sujet", "test@gmail.com", "Contenu");
        
        // On l'ajoute à la liste via le getter
        service.getMails().add(testMail); 

        // 2. On exécute la mise à jour manuelle
        service.updateMailTag(0, "DO");

        // 3. On vérifie que le tag a bien changé en DO
        assertEquals(EisenhowerAction.DO, service.getMails().get(0).getAction());
    }

    @Test
    void mailShouldBePendingByDefault() {
        // Test unitaire simple : un nouveau mail doit être en PENDING
        Mail mail = new Mail("ID1", "2026", "Test", "moi@test.com", "Hello");
        assertEquals(EisenhowerAction.PENDING, mail.getAction());
    }

    @Test
    void listShouldGrowWhenAddingMail() {
        // Test d'intégration simple : vérifier que la liste s'incrémente
        int initialSize = service.getMails().size();
        
        service.getMails().add(new Mail("1", "2", "3", "4", "5"));
        
        assertEquals(initialSize + 1, service.getMails().size());
    }
}