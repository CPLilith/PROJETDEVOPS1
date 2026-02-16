package projet.devops.Mail.Classifier;

import projet.devops.Mail.Mail;
// interface pour la stratégie de classification (choisir son IA)
public interface ClassificationStrategy {
    EisenhowerAction classify(Mail mail, Persona persona);
    String getStrategyName();
}