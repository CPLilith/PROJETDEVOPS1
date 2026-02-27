![License](https://img.shields.io/badge/license-MIT-yellowgreen)
![Versions](https://img.shields.io/badge/Version-v1.0.3_alpha-007ec6)
[![Tests OK](https://github.com/CPLilith/PROJETDEVOPS1/actions/workflows/maven.yml/badge.svg)](https://github.com/CPLilith/PROJETDEVOPS1/actions/workflows/maven.yml)
![CI - Couverture (JaCoCo Maven)](https://github.com/CPLilith/PROJETDEVOPS1/actions/workflows/Couverture.yml/badge.svg)
![Build OK](https://github.com/CPLilith/PROJETDEVOPS1/actions/workflows/Build.yml/badge.svg)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=CPLilith_PROJETDEVOPS1&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=CPLilith_PROJETDEVOPS1)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=CPLilith_PROJETDEVOPS1&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=CPLilith_PROJETDEVOPS1)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=CPLilith_PROJETDEVOPS1&metric=coverage)](https://sonarcloud.io/summary/new_code?id=CPLilith_PROJETDEVOPS1)
[![SonarQube Cloud](https://sonarcloud.io/images/project_badges/sonarcloud-light.svg)](https://sonarcloud.io/summary/new_code?id=CPLilith_PROJETDEVOPS1)

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

# Aller dans le répertoire du projet
cd /PROJETDEVOPS1/ProjetDevops

#Lancer le projet
./run_project.sh

# Compiler et exécuter l'application Spring Boot
mvn clean install
mvn spring-boot:run
```
## Version 1.0.0 alpha :

**Features Principales**
• Classification IA intelligente des emails via Ollama (TinyLlama) s’adaptant dynamiquement au
profil utilisateur (Persona).
– Synchronisation des d´ecisions de priorit´e (Labels) avec l’interface serveur Gmail.
• Syst`eme de Knowledge Base (Vault) permettant l’import de dossiers locaux et la g´en´eration auto-
matique de synth`eses par l’IA.

**Petites Features**
• Interface de lecture unifi´ee avec s´electeurs de priorit´e manuels (DO, PLAN, DELEGATE, DELETE)
pour les mails et les notes.
• Syst`eme de bascule de Persona (
Etudiant, D´eveloppeur, Professeur) modifiant le comportement de
l’analyse IA.

**Notes Techniques**
• Mise en place du moteur de d´ependances Maven pour la gestion des biblioth`eques externes
