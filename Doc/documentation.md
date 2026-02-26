---
title: "EisenFlow : Gestion Intelligente d'Emails"
subtitle: "Projet DevOps 1 - Documentation Technique"
author: [Ton Nom / Ton Équipe]
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
Notre application permet à l'utilisateur de charger ses mails. Elle offre ensuite deux modes de gestion :

* **Classement automatique :** L'IA analyse le contenu et applique les tags de la **Matrice Eisenhower**.
* **Classement manuel :** L'utilisateur garde le contrôle total sur le tagage.

### Fonctionnalités clés
L'application permet d'envoyer les résultats directement dans Gmail. Les tags disponibles sont :
1.  **DO** : Tâches urgentes et importantes.
2.  **PLAN** : Tâches importantes mais non urgentes.
3.  **DELEGATE** : Tâches urgentes mais non importantes.
4.  **DELETE** : Tâches ni urgentes ni importantes.

## 4. Analyse de Concurrence
Voici comment se situe EisenFlow par rapport aux solutions existantes :

| Concurrent | Points Forts | Points Faibles (vs EisenFlow) |
| :--- | :--- | :--- |
| **Gmail / Outlook** | Standard du marché, règles manuelles. | Pas de tri intelligent automatique. |
| **SuperHuman** | IA performante, rapidité. | Données sur serveurs tiers (confidentialité). |
| **Jira** | Tableaux Kanban puissants. | Saisie manuelle des tâches nécessaire. |

> **Note sur la confidentialité :** Contrairement à SuperHuman, EisenFlow privilégie une **IA locale** pour protéger la vie privée des utilisateurs.

## 5. Éléments de gestion de projet
*(Section à compléter : Planning, outils utilisés, etc.)*

## 6. Diagramme de classes
<img width="1600" height="530" alt="image" src="https://github.com/user-attachments/assets/110fe765-abd3-4907-8ba1-9916bc011874" />


### 6.1 But des features
### 6.2 Scénarios
### 6.3 Wireframe et screenshots

## 7. Résumé des features
L'application se concentre sur l'efficacité : extraction, analyse par IA, et synchronisation Gmail.

## 8. Annexe API REST
*(Détails des endpoints ici)*
OK 
