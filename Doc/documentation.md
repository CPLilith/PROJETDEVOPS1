---
title: "EisenFlow : Gestion Intelligente d'Emails"
subtitle: "Projet DevOps 1 - Documentation Technique"
author: [NOus]
date: "Février 2026"
lang: fr-FR
toc: true
toc-title: "Table des matières"
numbersections: true
geometry: margin=2.5cm
header-includes: |
  \usepackage{fancyhdr}
  \pagestyle{fancy}
  \fancyfoot[C]{\thepage}
---

# Présentation Générale

## Couverture
\newpage

## 1. Qui ?
**EisenFlow** a été créé pour aider les personnes qui reçoivent beaucoup d’emails et qui ont du mal à tout gérer. Le but est de réduire le stress et de mieux organiser le travail grâce à l’Intelligence Artificielle.

## 2. Quoi ? Pourquoi ?
L'objectif est d'appliquer la méthode de productivité *Eisenhower* directement dans le flux de travail des emails, en utilisant une IA locale pour garantir la **confidentialité** des données utilisateur.

## 3. Description de l'application
Notre application permet à l'utilisateur de charger ses mails dans notre application. Cette application va ensuite donner la possibilité de soit classer automatiquement les mails dans la Matrice Eisenhower pour que les mails se fassent taguer, soit de laisser classer et taguer les mails manuellement par l'utilisateur. Parmis les fonctionnalités présentes dans notre application il y a la possibilité d'envoyer les résultats dans le gmail directement, classer et taguer les mails. Dans les tags disponible, il y a le DO, PLAN, DELEGATE et DELETE. Le DO est le tag qui 

* **Classement automatique :** L'IA analyse le contenu et applique les tags de la **Matrice Eisenhower**.
* **Classement manuel :** L'utilisateur garde le contrôle total sur le tagage.

### Fonctionnalités clés
L'application permet d'envoyer les résultats directement dans Gmail. Les tags disponibles sont :
1.  **DO** : Tâches urgentes et importantes.
2.  **PLAN** : Tâches importantes mais non urgentes.
3.  **DELEGATE** : Tâches urgentes mais non importantes.
4.  **DELETE** : Tâches ni urgentes ni importantes.

## 4. Analyse de Concurrence

Parmi les concurrents de notre application, on retrouve plusieurs applications largement utilisées qui proposent des fonctionnalités similaires. Par exemple, les boîtes mails classique comme Gmail, Outlook ou AppleMail, qui permet la gestion centralisée des mails, d'afficher des messages, de créer des dossiers ou encore créer des règles manuelles. Contrairement a notre application, Gmail vise à faciliter la communication et l’organisation de l’information pour les utilisateurs mais elle ne permet pas le tri de mails automatique (pour l'instant).

Un autre type d'applications parmi les boîtes mails sont ceux utilisant l'IA comme SuperHuman. l'IA peuvent résumer un mail et écrire une réponse. SuperHuman a donc quelque ressemblance par rapport a notre application ( utilisation de l'IA ainsi que réponse qui ressemble à notre DELEGATE automatique) mais contrairement à nous SuperHuman n'utilise pas une IA locale mais envoie le mail sur des serveurs en ligne. Si l'utilisateur souhaite de la confidentialité, ca serait une problèmatique. 

Il y a aussi les applications de gestion de projet comme Jira qui proposent des tableaux Kanban, permettent de suivre les tâches et sont donc très utiles pour le travail en équipe. Notre application permet de suivre les tâches lues dans les mails contrairement à Jira qui elle doit manuellement mettre chaque tâches.

> **Note sur la confidentialité :** Contrairement à SuperHuman, EisenFlow privilégie une **IA locale** pour protéger la vie privée des utilisateurs.

## 5. Éléments de gestion de projet
*(Section à compléter : Planning, outils utilisés, etc.)*

## 6. Diagramme de classes

![Diagramme de classes](https://github.com/user-attachments/assets/110fe765-abd3-4907-8ba1-9916bc011874){width=100%}

### 6.1 But des features
### 6.2 Scénarios
### 6.3 Wireframe et screenshots

## 7. Résumé des features
L'application se concentre sur l'efficacité : extraction, analyse par IA, et synchronisation Gmail.

## 8. Annexe API REST
*(Détails des endpoints ici)*
