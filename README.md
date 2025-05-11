# EL_FIRMA

**EL_FIRMA** est une application de gestion des utilisateurs pour une plateforme dédiée aux agriculteurs, experts, fournisseurs, et administrateurs. Elle permet d'ajouter, modifier, supprimer et afficher des utilisateurs avec des rôles spécifiques. L'application utilise **JavaFX** pour l'interface utilisateur et **MySQL** pour la gestion des données.

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
