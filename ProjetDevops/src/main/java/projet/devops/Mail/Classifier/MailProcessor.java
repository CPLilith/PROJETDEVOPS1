package projet.devops.Mail.Classifier;

import java.util.List;

import projet.devops.Mail.Mail;

public class MailProcessor {
    
    private final IAAggregator iaAggregator;
    
    // Constructeur avec agrégateur directement
    public MailProcessor(IAAggregator iaAggregator) {
        this.iaAggregator = iaAggregator;
    }
    
    // Constructeur par défaut
    public MailProcessor() {
        this(new IAAggregator()); // Utilise le constructeur par défaut de IAAggregator
    }
    
    public Mail tagMail(Mail mail) {
        String preparedText = TextCleaner.prepareEmailForAnalysis(mail);
        
        // Utiliser SEULEMENT les IA pour décider du tag
        EisenhowerTag tag = iaAggregator.getConsensusTag(preparedText);
        
        return mail.withTag(tag);
    }
    
    // Méthode pour obtenir les résultats détaillés de chaque IA
    public List<IAAggregator.IAResult> getDetailedResults(Mail mail) {
        String preparedText = TextCleaner.prepareEmailForAnalysis(mail);
        return iaAggregator.analyzeWithAllIA(preparedText);
    }
}