package projet.devops.Mail.Controller;

import projet.devops.Mail.Service.MailService;
import jakarta.mail.internet.MimeMessage;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import projet.devops.Mail.Service.MailService;
import projet.devops.Mail.Gestion.FichierTemp;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.List;

import java.util.Map;

import java.util.List;

@RestController
public class MailController {

    private final MailService mailService;
    private final FichierTemp fichierTemp;

    public MailController(MailService mailService, FichierTemp fichierTemp) {
        this.mailService = mailService;
        this.fichierTemp = fichierTemp;
    }

    @GetMapping("/mails")
    public List<Map<String, String>> getMails() throws Exception {
        List<Map<String, String>> mails = mailService.getAllMails();
        fichierTemp.sauvegarderMails(mails);
        return mails;
    }
}