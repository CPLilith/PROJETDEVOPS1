# PROJETDEVOPS1
Github du projet DevOps M1 MIAGE APP

Etudiants:
Franck ZHENG
Arthur CHAUVEAU
Swetha SARAVANAN
Alex BRINDUSOIU

Projet Gestionnaire de Mail intelligent

déroulé du gestionnaire de mail intelligent
- possibilité d'obtenir les inputs ( plusieurs approche possible, au début nous allons nous axer sur le cccv meme si non pertinent. on élaboreras sur les approches plus intéressant après avancement sur le projet )
- matrice d'eisenhower (important, urgent, no imp, no urg) pour la décision des gestions de mail, il est possible de réaliser ce procédé de differente manière: manuel par l'utilisateur, semi auto par classificateur puis validation et complétement auto par classificateur et totale confiance dans le programme/LLM )
- type d'action: 
  - imp + urg -> traiter : laisser dans a traiter, confirmation quand plus dans l'espace
  - imp + no urg -> planifier : mail into mail / mail into agenda, option de lier le mail par calendrier, alerte, tri par date
  - no imp + urg -> déléguer : délégue le mail a une autre personne. Possibilité de rajout de message, possibilité d'associer des personnes/groupes 
  - no imp + no urg -> supprimer : supprime le message

backlog:
- pouvoir input par sélection les non lus/ drapeau / certain mail etc -> systeme de filtre pour input
- choix technique : ql librairie, api, facon de stock (nv user/mail réel),  local,etc
- trouver un moyen d'exporter les mails (input cccv?)
- garde de tri ?

---

### Pré-requis & Exécution complète

Les commandes suivantes :

Sommaire des commandes : 
- installent **Ollama**
- téléchargent le modèle **TinyLlama**
- démarrent le serveur Ollama
- se placent dans le répertoire du projet
- compilent et lancent l’application Spring Boot

```bash
# Installer Ollama
curl -fsSL https://ollama.com/install.sh | sh

# Télécharger le modèle TinyLlama
ollama pull tinyllama

# Lancer le serveur Ollama
ollama serve &

# Aller dans le répertoire du projet
cd /PROJETDEVOPS1/ProjetDevops

# Compiler et exécuter l'application Spring Boot
mvn clean install
mvn spring-boot:run


