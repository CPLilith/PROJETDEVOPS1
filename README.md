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
lien vidéo domo : https://www.youtube.com/watch?v=iJFdPye2llk&feature=youtu.be
---

## 1. Qui sommes-nous?

Nous sommes une équipe de quatre étudiants en **Master 1 MIAGE** (Méthodes Informatiques Appliquées à la Gestion des Entreprises), parcours **APPRENTISSAGE**, promotion **2025** à l'**Université Paris Nanterre.**

Ce projet a été réalisé dans le cadre de notre cursus **DevOps**.

## 2. Quoi ? Pourquoi ?

L'objectif principal est de résoudre la "surcharge cognitive" liée au flux incessant d'emails en appliquant la méthode de productivité **Eisenhower** directement à la source. 

Plutôt que de traiter les messages de manière chronologique, l'application force une hiérarchisation basée sur la valeur (importance) et l'urgence. L'innovation majeure réside dans l'utilisation d'une **IA locale** : contrairement aux solutions cloud classiques, le traitement sémantique s'effectue sur la machine de l'utilisateur, garantissant une **confidentialité absolue** des données sensibles et une indépendance vis-à-vis des connexions internet pour la partie analyse.

## 3. À propos du projet

**Eisenflow** est une solution intelligente de gestion du flux d'emails. Contrairement à une boîte de messagerie classique où l'utilisateur subit un flux chronologique et indifférencié, notre application :

* **Importe et Synchronise** : Récupération sécurisée de vos emails (via IMAP/Gmail) et création d'un cache local pour une navigation ultra-rapide et déconnectée.
* **Analyse** : Évaluation sémantique locale par IA du contenu de chaque message pour en déduire le contexte, le niveau d'urgence et l'importance.
* **Optimise et Catégorise** : Classement automatique (ou manuel) de vos emails selon la **Matrice d'Eisenhower** (DO, PLAN, DELEGATE, DELETE), avec la possibilité de créer des sous-tags personnalisés (ex: *DO · RH*, *DO · Technique*).
* **Délégation Assistée par IA** : Identification automatique du meilleur collaborateur dans votre équipe pour traiter une tâche spécifique, couplée à la pré-rédaction intelligente d'un brouillon de transfert.
* **Extraction de Connaissances** : Transformation des fils d'emails complexes en notes structurées persistantes (*Knowledge Base*) et génération de rapports ou mémos au format PDF.
* **Synchronisation Bidirectionnelle** : Exportation de vos décisions locales (tags et catégories) directement sur votre compte Gmail sous forme de labels, en un clic.
* **Gestion Intelligente de l'Intégrité** : Mécanisme de suppression en cascade garantissant que la modification ou la suppression d'un tag personnalisé réaffecte proprement les emails associés, sans aucune perte de données.

### Public visé

* **Étudiants :** Pour jongler entre les mails administratifs de l'université, les rendus de projets urgents et les travaux de groupe. L'application permet d'isoler instantanément ce qui requiert une action immédiate (DO) des simples newsletters étudiantes (DELETE), réduisant ainsi le stress lié aux deadlines.
* **Développeurs :** Pour protéger les précieuses périodes de "Deep Work". L'IA aide à séparer les alertes serveurs ou rapports de bugs critiques (DO) du reste du bruit numérique (notifications de pull requests, newsletters tech), tout en facilitant la délégation de tickets d'assistance au reste de l'équipe technique.
* **Enseignants :** Pour gérer efficacement le flux constant et chronophage de questions d'étudiants, de directives administratives et de réunions pédagogiques. L'outil aide à prioriser les demandes urgentes, à planifier les corrections (PLAN) et à extraire rapidement les informations clés en mémos PDF pour les cours.

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
