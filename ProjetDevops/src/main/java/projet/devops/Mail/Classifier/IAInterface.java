package projet.devops.Mail.Classifier;

public interface IAInterface{
    EisenhowerTag analyzeEmail(String emailText) throws Exception;
    String getModelName();
}