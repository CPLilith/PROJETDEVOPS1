package projet.devops.Mail.Controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import projet.devops.Mail.Gestion.FichierTemp;
import projet.devops.Mail.Gestion.FichierTempTraiter;
import projet.devops.Mail.Service.MailService;
import projet.devops.Mail.Service.TagSyncService;

//Dans cette classe, on utilise y trouve tout les url à entrer dans le navigateur, afin d'afficher les mails ou de synchroniser les tags vers Gmail.
//Donc on fait d'abord un /mails/view si mail_temps.txt n'existe pas part chercher les mails.
//puis on peut faire un /mails/refresh pour forcer la récupération des mails depuis IMAP + tag par IA.
//Et pour finir un /mails/sync-tags pour synchroniser les tags vers Gmail.


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
}