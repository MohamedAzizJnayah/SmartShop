# SmartShop

SmartShop est une application mobile Android conçue pour la gestion d’un catalogue de produits et le suivi de stock. L’application propose une interface moderne, une synchronisation Cloud, des statistiques, des visualisations simples, ainsi que des fonctions d’import/export pour faciliter le suivi et le partage des données.

---

## Fonctionnalités

- Authentification utilisateur
- Gestion complète des produits (ajout, modification, suppression)
- Recherche et filtrage de la liste des produits
- Statistiques de stock (quantités, valeur totale)
- Visualisation graphique simple (bar chart ou pie chart)
- Export de la liste des produits (CSV, PDF)
- Import de produits (CSV, PDF)
- Synchronisation des données entre stockage local et Cloud
- Chatbot intégré pour l’assistance utilisateur (aide, questions fréquentes, guidage)

---

## Technologies

- Android (Kotlin)
- Jetpack Compose (Material 3)
- Architecture MVVM (ViewModel + StateFlow)
- Stockage local (Room)
- Synchronisation Cloud (Firebase Authentication, Cloud Firestore)
- Coroutines & Flow
- Injection de dépendances (Hilt)

---

## Organisation du projet

- Interface utilisateur : écrans Compose, composants réutilisables
- Logique de présentation : ViewModels, états UI, actions
- Couche données : local (Room) et remote (Firestore)
- Couche domaine : modèles métier, repository, règles de gestion
- Module chatbot : logique conversationnelle et intégration

---

## Installation

- Cloner le dépôt
- Configurer Firebase pour l’application
- Synchroniser le projet avec Gradle
- Lancer l’application depuis Android Studio

---

## Utilisation

- Se connecter avec un compte utilisateur
- Ajouter et gérer des produits
- Consulter les statistiques et les graphiques
- Exporter ou importer la liste des produits selon le besoin
- Utiliser le chatbot pour obtenir de l’aide ou des réponses rapides

---

## Contribution

Les contributions sont les bienvenues.

- Proposer une amélioration via une issue
- Créer une branche dédiée pour chaque fonctionnalité ou correctif
- Soumettre une Pull Request claire, avec une description des changements

---

## Licence

Ce projet est distribué sous licence MIT.

---

## Auteur

Mohamed Aziz Jnayah  
GitHub : https://github.com/MohamedAzizJnayah
