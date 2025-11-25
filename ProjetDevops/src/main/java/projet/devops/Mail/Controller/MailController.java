package projet.devops.Mail.Controller;

import projet.devops.Mail.Service.MailService;
import jakarta.mail.internet.MimeMessage;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import java.util.List;

@RestController
public class MailController {

    private final MailService mailService;

    public MailController(MailService mailService) {
        this.mailService = mailService;
    }

    @GetMapping("/mails")
    public List<Map<String, String>> getMails() throws Exception {
        return mailService.getAllMails();
    }
}