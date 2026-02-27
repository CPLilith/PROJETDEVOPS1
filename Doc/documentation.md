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

## 4. Analyse de Concurrence

Parmi les concurrents de notre application, on retrouve plusieurs applications largement utilisées qui proposent des fonctionnalités similaires. Par exemple, les boîtes mails classiques comme Gmail, Outlook ou AppleMail, qui permettent la gestion centralisée des mails, d'afficher des messages, de créer des dossiers ou encore de créer des règles manuelles. Contrairement à notre application, Gmail vise à faciliter la communication et l’organisation de l’information pour les utilisateurs mais elle ne permet pas le tri de mails automatique (pour l'instant).

Un autre type d'applications parmi les boîtes mails sont celles utilisant l'IA comme SuperHuman. L'IA peut résumer un mail et écrire une réponse. SuperHuman a donc quelques ressemblances par rapport à notre application (utilisation de l'IA ainsi que réponse qui ressemble à notre DELEGATE automatique) mais contrairement à nous, SuperHuman n'utilise pas une IA locale mais envoie les mails sur des serveurs en ligne. Si l'utilisateur souhaite de la confidentialité, ça serait une problématique. 

Il y a aussi les applications de gestion de projet comme Jira qui proposent des tableaux Kanban, permettent de suivre les tâches et sont donc très utiles pour le travail en équipe. Notre application permet de suivre les tâches lues dans les mails, contrairement à Jira qui nécessite de mettre manuellement chaque tâche.

L'intégration de l'Intelligence Artificielle au sein de Gmail

L'évolution récente de Gmail, propulsée par l'intégration de modèles d'intelligence artificielle avancés (tels que Gemini), a transformé la messagerie standard en un véritable assistant de productivité. L'état de l'art de ces fonctionnalités natives s'articule autour de cinq axes majeurs :

* Rédaction assistée ("Help me write") : Cette fonctionnalité permet de générer des courriels complets à partir de requêtes textuelles basiques. L'IA offre également la possibilité d'ajuster dynamiquement le ton du message (pour le rendre plus formel ou décontracté) et inclut des outils de relecture avancée visant à optimiser la syntaxe et l'impact du texte.

* Résumés automatiques de l'historique : Face à la surcharge informationnelle, l'IA propose une synthèse proactive des longs fils de discussion. Le système extrait et structure les points clés des échanges, permettant à l'utilisateur de s'approprier rapidement le contexte sans avoir à parcourir l'intégralité des messages.

* Recherche par requêtes en langage naturel (Q&A) : Le moteur de recherche classique par mots-clés évolue vers une interface conversationnelle. L'utilisateur peut interroger directement sa base de données d'e-mails (par exemple : « Quand arrive mon colis ? ») pour obtenir une réponse précise et synthétisée par l'IA, remplaçant ainsi la traditionnelle liste de résultats cliquables.

* Réponses intelligentes et contextuelles (Smart Replies) : S'appuyant sur une analyse sémantique globale de l'échange en cours, le système est capable de suggérer des réponses longues et adaptées. Ces propositions prennent en compte le contexte spécifique de la discussion, réduisant le temps de rédaction pour les tâches répétitives.

* Tri prédictif et système de relances (Nudging) : L'organisation de la boîte de réception est optimisée par une priorisation automatique des messages jugés prioritaires. Ce tri intelligent est couplé à un mécanisme de rappels proactifs (« Nudges »), qui identifie et fait remonter les courriels en attente d'une réponse ou nécessitant un suivi de la part de l'utilisateur.

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

* **Scénario F3 & F4 (Délégation et TAG personnalisé) :** L'utilisateur reçoit une demande technique qu'il ne peut pas traiter. L'application utilise le routage hybride pour identifier l'expert concerné, prépare un brouillon de réponse expliquant le contexte, prêt à être envoyé.

![Diagramme de séquence](https://github.com/user-attachments/assets/2948a8f5-1d0c-47f6-8faa-d05887287d8a)
![Diagramme de séquence](https://github.com/user-attachments/assets/5a46deb2-204d-449a-be53-bcdd085a4bfb)


* **Scénario F5 & F6 (Réunion et Agenda) :** Un fil de mails contient une invitation pour un point de synchronisation vendredi à 10h. L'IA (via NER) extrait la date, propose d'ajouter le rendez-vous à l'agenda (F6) et génère simultanément un PDF via OpenPDF résumant les échanges précédents pour préparer la réunion (F5).

![Diagramme de séquence](https://github.com/user-attachments/assets/45069392-ac90-40bf-aba1-297a3d2ce816)
![Diagramme de séquence](https://github.com/user-attachments/assets/adc5f698-546b-4cdd-9199-6c838600df28)


### 6.3 Wireframe et screenshots

**F1 : Tri Intelligent (IA)**

![SreanF1](https://github.com/user-attachments/assets/cd2b0a3e-746c-444a-afe7-5d157780b504)

**F2 : Traitement et fusion de fichier marksdown**
![SreanF2](https://github.com/user-attachments/assets/6f462a65-7ced-415c-8c2f-5953c516dc20)
**F3 : Tags Personnalisés**
![SreanF3](https://github.com/user-attachments/assets/a9dff067-ae3f-49b4-aa24-b5371072cedd)

**F4 : Délégation Assistée**
![SreanF4](https://github.com/user-attachments/assets/c5f58c8e-241e-4e74-96ea-0a0800e568b6)
**F5 : Préparation Réunions**

![SreanF5](https://github.com/user-attachments/assets/c264c441-d032-4e98-ab94-f679bb8cbbca)
**F6 : Agenda & Rendez-vous**
![SreanF6](https://github.com/user-attachments/assets/ac544583-a606-4329-aaed-cb31b5fa2e4b)

\newpage

## 7. Résumé des fonctionnalités

**1. Gestion intelligente des tâches et de l'agenda (F1, F4, F6)**
Le cœur du système utilise une IA locale (Ollama) pour analyser sémantiquement les besoins de l'utilisateur. Il est capable de trier et prioriser automatiquement les tâches selon la matrice d'Eisenhower (via le Pattern Strategy), mais aussi de détecter des dates et lieux (NER et Regex) pour proposer des planifications dans l'agenda. Si une tâche doit être transférée, un routage hybride identifie le bon expert et prépare un brouillon de délégation assistée (via le Pattern Adapter).

**2. Traitement documentaire et Synthèse (F2, F5)**
L'outil excelle dans la manipulation de documents. Il peut compiler un livrable final en fusionnant et en nettoyant dynamiquement plusieurs fragments de fichiers Markdown avant de les passer à Pandoc. Il agit également comme un assistant de préparation de réunions en demandant à l'IA de synthétiser l'historique des échanges pour générer un mémo au format PDF (en utilisant OpenPDF et des flux binaires).

**3. Personnalisation de l'espace de travail (F3)**
Pour s'adapter aux processus spécifiques de chaque utilisateur, le système permet une gestion flexible grâce à des tags personnalisés. Ces tags sont strictement contrôlés par des expressions régulières (Regex) et sauvegardés au format JSON via la librairie Jackson.
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
