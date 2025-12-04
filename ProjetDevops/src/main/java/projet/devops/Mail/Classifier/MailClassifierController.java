package projet.devops.Mail.Classifier;

import java.io.File;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import projet.devops.Mail.Mail;

@RestController
public class MailClassifierController {

    private final ObjectMapper mapper = new ObjectMapper();

    @GetMapping("/mails_classified")
    public List<Mail> getTaggedMails() throws Exception {

        File file = new File(getClass().getClassLoader().getResource("mails_temp.txt").getFile());

        if (!file.exists()) {
            throw new RuntimeException("Fichier mail_temp.txt introuvable !");
        }

        List<Mail> mails = mapper.readValue(
                file, new TypeReference<List<Mail>>() {}
        );

        List<String> importanceWords = List.of(
                "utilise peut-être votre compte",
                "sécurisez votre compte",
                "compte",
                "google"
        );

        List<String> urgencyWords = List.of(
                "immédiatement",
                "urgent",
                "alerte",
                "clé d'accès",
                "mot de passe d'appli"
        );

        return mails.stream()
                .map(mail -> MailProcessor.tagMail(mail, importanceWords, urgencyWords))
                .sorted((m1, m2) ->
                        Integer.compare(
                                MailProcessor.tagPriority(m1.getTag()),
                                MailProcessor.tagPriority(m2.getTag())
                        ))
                .toList();
    }
}
