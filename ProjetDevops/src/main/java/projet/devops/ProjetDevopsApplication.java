package projet.devops;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ProjetDevopsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjetDevopsApplication.class, args);
        // Initialisation simple avec les 3 modèles par défaut

        // test des fonctionnalités de classification des mails :
        // MailProcessor processor = new MailProcessor();
        // Mail taggedMail = processor.tagMail(mail);

        // // Ou avec des modèles personnalisés
        // List<IAInterface> customIAs = List.of(
        // new OllamaIA("tinyllama:latest"),
        // new OllamaIA("llama2:latest"),
        // new OllamaIA("mistral:latest")
        // );
        // MailProcessor customProcessor = new MailProcessor(customIAs);
        // Mail taggedMail2 = customProcessor.tagMail(mail2);

        // // Pour voir les résultats détaillés de chaque IA
        // List<IAAggregator.IAResult> detailedResults =
        // processor.getDetailedResults(mail);
        // for (IAAggregator.IAResult result : detailedResults) {
        // System.out.println(result.getModelName() + ": " + result.getTag());
        // }
    }

}
