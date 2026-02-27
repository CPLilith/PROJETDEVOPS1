---
title: "EisenFlow : Gestion Intelligente d'Emails"
subtitle: "Projet DevOps 1 - Documentation Technique"
author: 
  - Alex BRINDUSOIU
  - Arthur CHAUVEAU
  - Swetha SARAVANAN
  - Franck ZHENG
date: "Février 2026"
lang: fr-FR
classoption: titlepage
toc: true
toc-title: "Table des matières"
numbersections: true
geometry: margin=2.5cm
titlepage: true
header-includes: |
  \usepackage{fancyhdr}
  \pagestyle{fancy}
  \fancyhead[L]{EisenFlow}
  \fancyhead[R]{Projet DevOps 1}
  \fancyfoot[C]{\thepage}
  \usepackage{setspace}
  \onehalfspacing
---

\newpage

# Présentation Générale

## 1. Qui ?

**EisenFlow** a été créé pour aider les personnes qui reçoivent beaucoup d’emails et qui ont du mal à tout gérer. Le but est de réduire le stress et de mieux organiser le travail grâce à l’Intelligence Artificielle.

## 2. Quoi ? Pourquoi ?

L'objectif est d'appliquer la méthode de productivité *Eisenhower* directement dans le flux de travail des emails, en utilisant une IA locale pour garantir la **confidentialité** des données utilisateur.

## 3. Description de l'application

Notre application permet à l'utilisateur de charger ses mails dans notre application. Cette application va ensuite donner la possibilité de soit classer automatiquement les mails dans la Matrice Eisenhower pour que les mails se fassent taguer, soit de laisser classer et taguer les mails manuellement par l'utilisateur. 

Parmi les fonctionnalités présentes dans notre application il y a la possibilité d'envoyer les résultats dans Gmail directement, classer et taguer les mails. 

Dans les tags disponibles, il y a le DO, PLAN, DELEGATE et DELETE. Le **DO** est le tag qui regroupe les tâches urgentes et importantes nécessitant une action immédiate.

L'application offre deux modes d'utilisation :
* **Classement automatique :** L'IA analyse le contenu et applique les tags de la Matrice Eisenhower.
* **Classement manuel :** L'utilisateur garde le contrôle total sur le tagage.

### Fonctionnalités clés
L'application permet d'envoyer les résultats directement dans Gmail. Les tags disponibles sont :

1. **DO** : Tâches urgentes et importantes.
2. **PLAN** : Tâches importantes mais non urgentes.
3. **DELEGATE** : Tâches urgentes mais non importantes.
4. **DELETE** : Tâches ni urgentes ni importantes.

---

## 4. Analyse de Concurrence

Parmi les concurrents de notre application, on retrouve plusieurs applications largement utilisées qui proposent des fonctionnalités similaires. Par exemple, les boîtes mails classiques comme Gmail, Outlook ou AppleMail, qui permettent la gestion centralisée des mails, d'afficher des messages, de créer des dossiers ou encore de créer des règles manuelles. Contrairement à notre application, Gmail vise à faciliter la communication et l’organisation de l’information pour les utilisateurs mais elle ne permet pas le tri de mails automatique (pour l'instant).

Un autre type d'applications parmi les boîtes mails sont celles utilisant l'IA comme SuperHuman. L'IA peut résumer un mail et écrire une réponse. SuperHuman a donc quelques ressemblances par rapport à notre application (utilisation de l'IA ainsi que réponse qui ressemble à notre DELEGATE automatique) mais contrairement à nous, SuperHuman n'utilise pas une IA locale mais envoie les mails sur des serveurs en ligne. Si l'utilisateur souhaite de la confidentialité, ça serait une problématique. 

Il y a aussi les applications de gestion de projet comme Jira qui proposent des tableaux Kanban, permettent de suivre les tâches et sont donc très utiles pour le travail en équipe. Notre application permet de suivre les tâches lues dans les mails, contrairement à Jira qui nécessite de mettre manuellement chaque tâche.

> **Note sur la confidentialité :** Contrairement à SuperHuman, EisenFlow privilégie une **IA locale** pour protéger la vie privée des utilisateurs.

\newpage

# Ingénierie et Fonctionnalités

## 5. Éléments de gestion de projet et Architecture technique

Dans le cadre de ce projet, nous avons mis en place une organisation d'équipe stricte (réunions hebdomadaires, répartition claire des rôles : Backend, UI, QA) et une approche DevOps robuste :

* **Backend & IA Locale :** Le cœur de l'application repose sur **Java 21** et **Spring Boot 3.x**. L'extraction et la relève sécurisée des mails se font via **Jakarta Mail**. L'IA, garantissant 100% de confidentialité, est propulsée localement par **Ollama** (modèle *TinyLlama*).

* **Collaboration & DevOps :** Nous utilisons la méthodologie Agile (Kanban) avec **GitHub** (Issues, PRs). L'intégration continue (CI/CD) est gérée par GitHub Actions pour automatiser la compilation et les tests. Le suivi strict de la qualité du code et des vulnérabilités est assuré par **SonarCloud**, **Jacoco**, et **JUnit 5**.

* **Architecture & Patterns :** Afin de garantir un découplage strict et une flexibilité maximale (par exemple pour changer de modèle d'IA facilement), nous avons implémenté une architecture **MVC** soutenue par les design patterns **Strategy**, **Facade**, et **Registry**.

---

## 6. Diagramme de classes

![Diagramme de classes](https://github.com/user-attachments/assets/04e1e5df-bce5-4aa3-aa25-af5c25877ec3){width=100%}

### 6.1 But des features

**F1 : Tri Intelligent (IA)**
* **Objectif :** Automatiser la priorisation selon la matrice d'Eisenhower.
* **Implémentation :** Analyse sémantique via IA locale (Ollama) prenant en compte le Persona de l'utilisateur. Utilisation du Pattern *Strategy*.

**F2 : Traitement et fusion de fichier marksdown**
* **Objectif:**  Centraliser et assembler dynamiquement plusieurs fichiers Markdown fragmentés en un document unique et structuré pour générer le livrable final.
* **Implémentation :** Un script parcourt les dossiers cibles, nettoie les métadonnées (frontmatter) et concatène les fichiers .md dans un ordre défini avant de passer le résultat au compilateur Pandoc.

**F3 : Tags Personnalisés**
* **Objectif :** Adapter l'outil aux processus spécifiques de l'utilisateur.
* **Implémentation :** Création/suppression de tags sur-mesure validés par contrôles Regex et sauvegardés en JSON via la librairie *Jackson*.

**F4 : Délégation Assistée**
* **Objectif :** Faciliter le transfert des tâches à l'expert adéquat.
* **Implémentation :** Routage hybride (mots-clés + IA locale) pour générer un brouillon automatique. Utilisation du Pattern *Adapter*.

**F5 : Préparation Réunions**
* **Objectif :** Fournir un contexte immédiat avant un rendez-vous.
* **Implémentation :** Synthèse de l'historique des échanges par l'IA et génération d'un mémo au format PDF à l'aide de flux binaires et de la librairie *OpenPDF*.

**F6 : Agenda & Rendez-vous**
* **Objectif :** Extraire et planifier automatiquement les événements.
* **Implémentation :** Détection de dates et lieux via IA locale (NER - *Named Entity Recognition*) et parsing de dates par Regex pour proposer une insertion dans l'agenda.

---

### 6.2 Scénarios

* **Scénario F1 & F2 (Synchronisation et Tri) :** L'utilisateur connecte son compte. Le backend relève les mails via Jakarta Mail. L'IA Ollama analyse sémantiquement chaque message en fonction du persona choisi et les répartit dans les colonnes du Kanban (DO, PLAN, etc.).

![Diagramme de séquence](https://github.com/user-attachments/assets/1ba9aa8a-48d1-4c4d-ae3b-4f438329aeb2)
![Diagramme de séquence](https://github.com/user-attachments/assets/353e2007-63bf-47aa-b829-8048686e99cc)

* **Scénario F4 (Délégation) :** L'utilisateur reçoit une demande technique qu'il ne peut pas traiter. L'application utilise le routage hybride pour identifier l'expert concerné, prépare un brouillon de réponse expliquant le contexte, prêt à être envoyé.

![Diagramme de séquence](https://github.com/user-attachments/assets/2948a8f5-1d0c-47f6-8faa-d05887287d8a)

* **Scénario F5 & F6 (Réunion et Agenda) :** Un fil de mails contient une invitation pour un point de synchronisation vendredi à 10h. L'IA (via NER) extrait la date, propose d'ajouter le rendez-vous à l'agenda (F6) et génère simultanément un PDF via OpenPDF résumant les échanges précédents pour préparer la réunion (F5).

![Diagramme de séquence](https://github.com/user-attachments/assets/45069392-ac90-40bf-aba1-297a3d2ce816)

### 6.3 Wireframe et screenshots
**F1 : Traitement et fusion de fichier marksdown**
**F2 : Tri Intelligent (IA)**
**F3 : Tags Personnalisés**

\newpage

## 7. Résumé des features

L'application repose sur un cœur Java/Spring Boot solide orchestrant 6 fonctionnalités principales :

1. **Synchronisation :** Relève sécurisée (IMAP/SMTP) pour tendre vers le Zéro Inbox.
2. **Tri IA :** Classification Eisenhower sémantique gérée localement par TinyLlama.
3. **Tags personnalisés :** Flexibilité du système gérée via JSON.
4. **Délégation :** Routage et brouillons assistés par IA et mots-clés.
5. **Préparation :** Mémos PDF générés automatiquement.
6. **Agenda :** Détection d'entités (NER) pour planifier les évènements.

---

# 8. Annexe API REST
## Documentation de l'API Mails

**URL de base :** `/api/mails`  
**Format des requêtes et réponses :** `application/json`

---

### 1. Actions Globales (Collection)

Ces routes agissent sur l'ensemble de la boîte mail ou sur la configuration globale.

| Méthode | Route | Description | Corps (Body) |
| :--- | :--- | :--- | :--- |
| **GET** | `/` | Récupère tous les emails avec leurs liens d'actions (HATEOAS). | *Aucun* |
| **POST** | `/fetch` | Récupère les nouveaux emails depuis la source. | *Aucun* |
| **POST** | `/sync` | Synchronise avec Gmail puis récupère les nouveaux emails. | *Aucun* |
| **POST** | `/analyze` | Analyse et classifie automatiquement les emails en attente (PENDING). | *Aucun* |
| **POST** | `/auto-status` | Met à jour automatiquement les statuts grâce à l'IA. | *Aucun* |
| **PUT** | `/persona` | Met à jour le Persona de l'utilisateur. | `{"persona": "NOM_DU_PERSONA"}` |

---

### 2. Actions Spécifiques (Ressource Unique)

Ces routes ciblent un email précis grâce à son identifiant (`{id}`).

| Méthode | Route | Description | Corps (Body) Requis |
| :--- | :--- | :--- | :--- |
| **GET** | `/{id}` | Récupère les détails d'un email spécifique. | *Aucun* |
| **PUT** | `/{id}/status` | Modifie le statut d'un email (ex: TODO, DONE). | `{"status": "DONE"}` |
| **PUT** | `/{id}/tag` | Ajoute ou modifie un tag sur l'email. | `{"tag": "URGENT"}` |
| **DELETE**| `/{id}` | Marque l'email comme supprimé (Action = DELETE). | *Aucun* |

---

### 3. Actions de Délégation

Routes dédiées au processus de délégation (Eisenhower : Delegate).

| Méthode | Route | Description | Corps (Body) Requis |
| :--- | :--- | :--- | :--- |
| **POST** | `/{id}/delegate-auto` | Génère une suggestion de délégation via l'IA. | *Aucun* |
| **POST** | `/{id}/delegate-confirm`| Valide la délégation avec l'assigné et le brouillon. | `{"assignee": "email@...", "draftBody": "texte..."}` |
| **POST** | `/{id}/delegate-manual` | Force une délégation manuelle sans brouillon IA. | `{"assignee": "email@..."}` |

---

> **Note sur le format de réponse (HATEOAS) :** > La plupart des requêtes GET (et certaines PUT) renvoient l'objet modifié accompagné d'un nœud `_links`. Ces liens indiquent au front-end quelles sont les prochaines actions possibles en fonction de l'état actuel de l'email.

## Documentation des Contrôleurs (Application Web)

Ce document liste l'ensemble des routes exposées par les contrôleurs de l'application. La majorité des routes utilisent le pattern MVC classique (redirection vers des vues ou d'autres pages), avec quelques endpoints spécifiques renvoyant des données brutes (JSON ou PDF).

---

### 1. Vues Principales (Navigation)
Géré par `HomeController`. Ces routes permettent d'afficher les différentes pages de l'interface utilisateur.

| Méthode | Route | Description | Paramètres |
| :--- | :--- | :--- | :--- |
| **GET** | `/` | Affiche la page d'accueil (Boîte de réception / Inbox). | *Aucun* |
| **GET** | `/kanban` | Affiche la vue Kanban (tâches en cours et finalisées). | *Aucun* |
| **GET** | `/tags` | Affiche la page de gestion des tags personnalisés. | *Aucun* |

---

### 2. Actions sur les Mails
Géré par `MailActionController`. Ces routes traitent les formulaires d'action sur les emails et redirigent vers les vues.

| Méthode | Route | Description | Paramètres attendus |
| :--- | :--- | :--- | :--- |
| **POST** | `/fetch` | Récupère les nouveaux mails depuis la source. | *Aucun* |
| **POST** | `/sync` | Synchronise avec Gmail et récupère les mails. | *Aucun* |
| **POST** | `/analyze` | Lance l'analyse IA des mails en attente. | *Aucun* |
| **POST** | `/auto-status` | Met à jour les statuts automatiquement avec l'IA. | *Aucun* |
| **POST** | `/update-mail-tag`| Met à jour le tag d'un mail spécifique. | `messageId`, `tag` |
| **POST** | `/update-status` | Met à jour le statut d'un mail. | `messageId`, `status` |
| **POST** | `/persona` | Modifie le Persona global de classification. | `persona` |

### Délégation
| Méthode | Route | Description | Paramètres attendus | Retour |
| :--- | :--- | :--- | :--- | :--- |
| **POST** | `/delegate-auto` | Suggère une délégation via l'IA. | `messageId` | **JSON** |
| **POST** | `/delegate-confirm`| Confirme la délégation avec le brouillon. | `messageId`, `assignee`, `draftBody`| **JSON** |
| **POST** | `/delegate-manual` | Force une délégation sans brouillon IA. | `messageId`, `assignee` | Redirection |

---

### 3. Événements et Planification
Géré par `EventController`. Permet de gérer les mails classifiés comme "À planifier".

| Méthode | Route | Description | Paramètres attendus | Retour |
| :--- | :--- | :--- | :--- | :--- |
| **GET** | `/events` | Affiche la liste des événements et tâches à faire. | *Aucun* | Vue HTML |
| **POST** | `/events/prepare`| Génère une fiche de préparation de réunion. | `messageId` | **Fichier PDF** |

---

### 4. Base de Connaissances (Knowledge)
Géré par `KnowledgeController`. Permet de gérer les notes et fichiers de contexte pour l'IA.

| Méthode | Route | Description | Paramètres attendus |
| :--- | :--- | :--- | :--- |
| **GET** | `/knowledge` | Affiche la base de connaissances. | *Aucun* |
| **POST** | `/knowledge/upload`| Uploade et génère des notes IA depuis des fichiers.| `files` (Multipart) |
| **POST** | `/update-note-tag` | Modifie le tag d'une note spécifique. | `index`, `tag` |
| **POST** | `/knowledge/delete`| Supprime une note de la base. | `index` |

---

### 5. Gestion des Tags (Tags & API)
Géré par `TagApiController`. Gère la création et la suppression des étiquettes personnalisées.

| Méthode | Route | Description | Paramètres attendus | Retour |
| :--- | :--- | :--- | :--- | :--- |
| **GET** | `/api/tags` | Récupère la liste de tous les tags. | *Aucun* | **JSON** |
| **POST** | `/tags/create` | Crée un nouveau tag via le formulaire classique. | `label` | Redirection |
| **POST** | `/tags/create-ajax`| Crée un nouveau tag de manière asynchrone (AJAX).| `label` | **JSON** |
| **POST** | `/tags/delete` | Supprime un tag et nettoie les mails associés. | `tagName` | Redirection |
