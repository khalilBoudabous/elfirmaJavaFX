# EL FIRMA

**EL FIRMA** est une application de gestion des utilisateurs pour une plateforme dédiée aux agriculteurs, experts, fournisseurs, et administrateurs. Elle permet d'ajouter, modifier, supprimer et afficher des utilisateurs avec des rôles spécifiques. L'application utilise **JavaFX** pour l'interface utilisateur et **MySQL** pour la gestion des données.

## Table des matières

- [Fonctionnalités](#fonctionnalités)
- [Structure du projet](#structure-du-projet)
- [Installation](#installation)
- [Utilisation](#utilisation)
- [Technologies utilisées](#technologies-utilisées)
- [Contributeurs](#contributeurs)
- [Licence](#licence)
- [Topics et mots-clés](#topics-et-mots-clés)

---

## Fonctionnalités

- **Gestion des utilisateurs** :
  - Ajouter des utilisateurs avec des rôles spécifiques (Agriculteur, Expert, Fournisseur, Administrateur).
  - Modifier les informations des utilisateurs.
  - Supprimer des utilisateurs.
  - Afficher la liste des utilisateurs.

- **Validation des champs** :
  - Validation des champs obligatoires (nom, prénom, email, etc.).
  - Validation des formats (email, téléphone, etc.).

- **Interface utilisateur** :
  - Interface graphique intuitive avec JavaFX.
  - Navigation entre les écrans (ajout, modification, liste des utilisateurs).

- **Base de données** :
  - Connexion à une base de données MySQL pour stocker les informations des utilisateurs.

---

## Structure du projet

### Modules du projet

L’application **El_FIRMA** est organisée en plusieurs modules clés, chacun conçu pour répondre aux besoins spécifiques des utilisateurs et offrir une plateforme numérique complète pour l’agriculture.

#### 📅 Module Événement (Event Management)
Permet aux fournisseurs de promouvoir leurs produits en organisant des événements :

- Création et gestion d'événements en ligne ou sur site via le tableau de bord.
- Génération de QR codes pour les tickets, facilitant le check-in.
- Accès public pour agriculteurs, experts et autres fournisseurs.
- Favorise le réseautage, le partage de connaissances et la croissance des affaires.

#### 🛒 Module Produit (Product Management)
Permet aux fournisseurs de vendre leurs produits agricoles :

- Ajout de produits avec images, descriptions, prix, et quantités.
- Achat en ligne sécurisé via Stripe.
- Filtres avancés par catégorie et prix pour une navigation efficace.

#### 💬 Module Conseil Expert (Expert Advice & Forum)
Crée un espace de collaboration entre experts et agriculteurs :

- Forum interactif pour poser des questions et obtenir des réponses d’experts.
- Base de connaissances consultable pour les réponses passées.
- Améliore les pratiques agricoles grâce à un support communautaire.

#### 🌾 Module Terrain (Land Rental)
Facilite la location de terrains agricoles entre utilisateurs :

- Publication d’annonces détaillées de terrains (localisation, surface, type...).
- Recherche et location de terrains adaptés aux besoins des agriculteurs.
- Optimise l’utilisation des terres et aide ceux qui n’en possèdent pas.

#### 👤 Module Utilisateur (User Management)
Cœur de l’application pour la gestion des rôles et des accès :

- Contrôle des accès et permissions selon le rôle (admin, expert, fournisseur, agriculteur).
- Authentification et autorisation sécurisées.
- Gestion des profils et données utilisateurs pour une expérience personnalisée.

---

## Installation

### Prérequis

- **Java 8 ou supérieur**
- **Maven**
- **MySQL**

### Étapes

1. **Clonez le dépôt** :
   ```bash
   git clone <URL_DU_DEPOT>
   cd FirmaPIDEV
   ```

2. **Configurez la base de données** :
   - Créez une base de données MySQL nommée `elfirma`.
   - Mettez à jour les informations de connexion dans le fichier `MyDataBase.java`.

3. **Installez les dépendances Maven** :
   ```bash
   mvn install
   ```

4. **Lancez l'application** :
   ```bash
   mvn javafx:run
   ```

---

## Utilisation

### Ajouter un utilisateur
- Lancez l'application.
- Naviguez vers l'écran **"Ajouter Utilisateur"**.
- Remplissez les champs obligatoires et cliquez sur **"Ajouter"**.

### Modifier un utilisateur
- Depuis la liste des utilisateurs, cliquez sur **"Modifier"**.
- Modifiez les informations et cliquez sur **"Sauvegarder"**.

### Supprimer un utilisateur
- Depuis la liste des utilisateurs, cliquez sur **"Supprimer"**.
- Confirmez la suppression.

---

## Technologies utilisées

- **Java** : Langage principal.
- **JavaFX** : Framework pour l'interface utilisateur.
- **MySQL** : Base de données relationnelle.
- **Maven** : Gestionnaire de dépendances.
- **ControlsFX** : Bibliothèque pour la validation des champs.

---

## Contributeurs

- **Développeur principal** : [Votre nom ou celui de l'équipe]
- **Contact** : [Votre email ou lien vers un profil professionnel]

---

## Licence

Ce projet est sous licence **MIT**. Consultez le fichier `LICENSE` pour plus d'informations.

---

## Topics et mots-clés

- **Topics** : Java, JavaFX, MySQL, Gestion des utilisateurs, Validation des champs, Interface graphique.
- **Mots-clés** : Agriculteur, Expert, Fournisseur, Administrateur, CRUD, Validation, Base de données, Maven, ControlsFX
