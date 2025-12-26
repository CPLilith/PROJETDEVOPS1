package projet.devops.Mail.Controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import projet.devops.Mail.Classifier.Persona;
import projet.devops.Mail.Classifier.PersonaResourceService;
import projet.devops.Mail.Gestion.FichierTemp;
import projet.devops.Mail.Gestion.FichierTempTraiter;
import projet.devops.Mail.Service.MailService;
import projet.devops.Mail.Service.TagSyncService;

//Dans cette classe, on utilise y trouve tout les url à entrer dans le navigateur, afin d'afficher les mails ou de synchroniser les tags vers Gmail.
//Donc on fait d'abord un /mails/view si mail_temps.txt n'existe pas part chercher les mails.
//puis on peut faire un /mails/refresh pour forcer la récupération des mails depuis IMAP + tag par IA.
//Et pour finir un /mails/sync-tags pour synchroniser les tags vers Gmail.
//Pour selectionner le persona, on utilise /mails/persona.



//@RestController
@Controller
@CrossOrigin(origins = "*")
public class MailController {

    private final MailService mailService;
    private final FichierTemp fichierTemp;
    private final FichierTempTraiter fichierTempTraiter;

    public MailController(MailService mailService, FichierTemp fichierTemp, FichierTempTraiter fichierTempTraiter) {
        this.mailService = mailService;
        this.fichierTemp = fichierTemp;
        this.fichierTempTraiter = fichierTempTraiter;
    }
    
    // Endpoint original - mails sans tags
    @GetMapping("/mails")
    @ResponseBody
    public List<Map<String, String>> getMails() throws Exception {
        List<Map<String, String>> mails = mailService.getAllMails();
        fichierTemp.sauvegarderMails(mails);
        return mails;
    }
    
    // Vue Thymeleaf - AFFICHE LES MAILS TRAITÉS AVEC TAGS
    @GetMapping("/mails/view")
    public String viewMails(Model model) throws Exception {
        List<Map<String, String>> mails;
        
        // Si le fichier traité existe, le lire
        if (fichierTempTraiter.fichierExiste()) {
            System.out.println("Chargement depuis le fichier traité");
            mails = fichierTempTraiter.lireMailsTraites();
        } else {
            // Sinon, récupérer depuis IMAP, traiter et sauvegarder
            System.out.println("Récupération et traitement des mails...");
            mails = mailService.getAllMails();
            fichierTempTraiter.traiterEtSauvegarder(mails);
        }
        
        // DEBUG
        System.out.println("Nombre de mails: " + mails.size());
        if (!mails.isEmpty()) {
            System.out.println("Premier mail: " + mails.get(0));
        }
        
        model.addAttribute("mails", mails);
        return "mails";
    }
    
    // Nouveau endpoint pour forcer le rafraîchissement
    @GetMapping("/mails/refresh")
    public String refreshMails(Model model) throws Exception {
        System.out.println("Rafraîchissement forcé des mails...");
        List<Map<String, String>> mails = mailService.getAllMails();
        fichierTempTraiter.traiterEtSauvegarder(mails);
        model.addAttribute("mails", mails);
        return "mails";
    }

    @Autowired
    private TagSyncService tagSyncService;

    @GetMapping("/mails/sync-tags")
    public String syncTagsToGmail(Model model) throws Exception {
        TagSyncService.SyncResult result = tagSyncService.syncTagsToGmail();
        model.addAttribute("syncResult", result);
        model.addAttribute("mails", fichierTempTraiter.lireMailsTraites());
        return "mails";
    }

    @GetMapping("/mails/persona")
    public String personaPage() {
        return "persona";  // Cherche persona.html dans templates/
    }

     // Récupère le persona actuel (renvoie du texte simple)
    @GetMapping("/mails/currentPersona")
    @ResponseBody
    public String getCurrentPersona() {
        try {
            Persona persona = PersonaResourceService.loadPersona();
            return persona.name(); // renvoie ETUDIANT, DEVELOPPEUR, PROFESSEUR ou NEUTRE
        } catch (Exception e) {
            return "NEUTRE";
        }
    }

    @RequestMapping(value = "/mails/selectPersona", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String selectPersona(@RequestParam(value = "persona", required = false) String personaValue) {
        if (personaValue == null || personaValue.isEmpty()) {
            return "Erreur: paramètre persona manquant";
        }
        try {
            Persona persona = Persona.valueOf(personaValue.toUpperCase());
            PersonaResourceService.savePersona(persona);
            return "OK";
        } catch (IllegalArgumentException e) {
            return "Persona inconnu : " + personaValue;
        } catch (IOException e) {
            return "Erreur d'enregistrement : " + e.getMessage();
        }
    }


    //Pour la supression des mails Non_Urgent_Non_Important : 

    /**
     * Récupère tous les emails (méthode originale)
     */
    @GetMapping("/all")
    public ResponseEntity<?> getAllMails() {
        try {
            List<Map<String, String>> mails = mailService.getAllMails();
            return ResponseEntity.ok(mails);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur: " + e.getMessage());
        }
    }

    /**
     * Récupère tous les emails avec leurs labels Gmail
     */
    @GetMapping("/all-with-labels")
    public ResponseEntity<?> getAllMailsWithLabels() {
        try {
            List<Map<String, String>> mails = mailService.getAllMailsWithLabels();
            return ResponseEntity.ok(mails);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur: " + e.getMessage());
        }
    }

    /**
     * Prévisualise les emails qui seraient supprimés (avec label non_urgent_non_important)
     */
    @GetMapping("/preview-delete")
    public ResponseEntity<?> previewEmailsToDelete() {
        try {
            List<Map<String, String>> emailsToDelete = mailService.previewEmailsToDelete();
            
            return ResponseEntity.ok(Map.of(
                "count", emailsToDelete.size(),
                "emails", emailsToDelete,
                "message", emailsToDelete.size() + " email(s) seront supprimés"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur: " + e.getMessage());
        }
    }

    /**
     * Supprime les emails ayant le label "non_urgent_non_important"
     */
    @DeleteMapping("/delete-non-urgent-non-important")
    public ResponseEntity<?> deleteNonUrgentNonImportantEmails() {
        try {
            int deletedCount = mailService.deleteNonUrgentNonImportantMails();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Suppression terminée avec succès",
                "deletedCount", deletedCount
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Erreur lors de la suppression",
                "error", e.getMessage()
            ));
        }
    }

}