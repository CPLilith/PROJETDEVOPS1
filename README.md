![License](https://img.shields.io/badge/license-MIT-yellowgreen)
![GitHub release (latest by date)](https://img.shields.io/github/v/release/CPLilith/PROJETDEVOPS1)
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

Présentation
EisenFlow est un gestionnaire de mails intelligent basé sur la matrice d'Eisenhower. Il se connecte à une boîte Gmail via IMAP, analyse automatiquement chaque mail grâce à un LLM local (Ollama/TinyLlama), et le classe en 4 tags d'action: DO PLAN DELEGATE DELETE. L'objectif est d'aider l'utilisateur à prioriser, déléguer et planifier ses mails sans effort manuel.

Fonctionnalités:

Classification IA (Matrice d'Eisenhower)

Connexion IMAP à Gmail et récupération des 30 derniers mails
Classification automatique par LLM local (TinyLlama via Ollama) en 4 catégories :

DO — Urgent & Important : à traiter immédiatement
PLAN — Important & Non urgent : à planifier
DELEGATE — Urgent & Non important : à déléguer
DELETE — Ni urgent ni important : à supprimer


Analyse parallèle multi-threadée (5 threads simultanés) pour réduire le temps de traitement
Tags DO personnalisés créés par l'utilisateur intégrés dans l'analyse IA
Adaptation au profil utilisateur via un système de Persona (Étudiant, Développeur, Professeur)
Synchronisation des labels vers Gmail

Suivi Kanban et délégations intelligente

Identification automatique du meilleur expert parmi les contacts enregistrés et génération d'un brouillon de transfert personnalisé.

Vue Kanban des mails délégués en deux colonnes : EN COURS et FINALISÉ
Détection automatique du statut via l'IA (FINALISÉ ou SUIVI)

Agenda IA

Détection automatique des dates et lieux dans les mails PLAN et DO
Génération d'une fiche mémo PDF pour préparer une réunion (contexte, objectif, points clés)

Gestion des contacts

Analyse automatique des expéditeurs pour en déduire leur expertise métier
Dictionnaire de contacts persisté en JSON, utilisé pour la délégation

Import de fichiers Markdown (.md)
Synthèse IA multi-documents avec classification Eisenhower automatique
Gestion des notes avec tags personnalisables

Tags personnalisés

Création et suppression de sous-catégories DO (DO_FORMATION, DO_URGENCE_CLIENT...)
Pris en compte dans l'analyse IA et les filtres de l'interface


Architecture technique

Spring Boot — backend MVC + API REST
Ollama / TinyLlama — LLM local pour la classification et la génération de texte
IMAP / SMTP Gmail — récupération et envoi de mails
Thymeleaf — templates HTML côté serveur
Persistance JSON — mails, contacts, notes, tags et persona stockés localement
Patterns appliqués — Strategy, Facade, Observer, Template Method, Repository (GoF) + principes SOLID

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
```

---
## Équipe

| Membre | GitHub | Numéro Étudiant | Rôle |
| :--- | :--- | :--- | :--- |
| **Alex BRINDUSOIU** | [@4l43](https://github.com/4l43) | 41014348 | ? |
| **Franck ZHENG** | [@CPLilith](https://github.com/CPLilith) | 43010994 | ? |
| **Swetha SARAVANAN** | [@SwethaSaravanan16](https://github.com/SwethaSaravanan16) | 41003040 | ? |
| **Arthur CHAUVEAU** | [@CHAUVEAUArthur](https://github.com/CHAUVEAUArthur) | 41005688 | ? |
