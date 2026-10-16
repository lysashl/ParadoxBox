ParadoxBox

# Description

ParadoxBox est un jeu JavaFX inspiré de Sokoban, enrichi d’un mode récursif
 (inspiré de Parabox) permettant de manipuler des mondes imbriqués.

Le projet est structuré avec Maven et organisé en plusieurs modules :
interface graphique, logique métier, persistance, génération de chemins et
résolution automatique.

# Objectifs

* Implémenter un jeu Sokoban fonctionnel
* Ajouter un mode récursif (mondes imbriqués)
* Concevoir une interface graphique JavaFX
* Créer des éditeurs de niveaux
* Mettre en place une architecture Maven propre
* Faciliter l’installation et l’exécution

# Prérequis

* Java *JDK 21*
* Maven *3.x*
* Environnement graphique *JavaFX*

Vérification :


java -version
mvn -version

# Installation

Cloner le projet:
git clone https://gitlab.sorbonne-paris-nord.fr/12308828/paradoxbox.git 

cd paradoxbox

# Lancement du projet
## Méthode recommandée 
Télécharger ici :
https://drive.google.com/file/d/1iNBWc7a3Yv1S2FDXJcRRww5buOv1AVY7/view?usp=drive_link

1/ Décompresser le fichier
2/ Ouvrir le dossier ParadoxBox
3/ Double-cliquer sur ParadoxBox.exe

## Linux / macOS

./run.sh

## Windows

.\run.bat


## Méthode manuelle

mvn clean compile
mvn javafx:run

## Lancement via JAR
mvn clean package
java -jar target/paradoxbox-1.0-SNAPSHOT.jar

# Structure du projet

text
paradoxbox/
├── pom.xml
├── README.md
├── run.sh
├── run.bat
├── src/
│   └── main/
│       ├── java/
│       │   ├── ig/                     # Interface graphique
│       │   ├── modele/                 # Logique du jeu
│       │   ├── plateau/                # Éditeurs de niveaux
│       │   ├── persistance/            # Sauvegarde / chargement
│       │   └── resolutionautomatique/  # Solver
│       └── resources/
│           ├── ressources/             # Resssources (images,sons,niveaux)
│           ├── plateau/  
│           └── modele/                 

# Classe principale 

ig.game

# Fonctionnalités
## Mode classique

* Déplacement du joueur
* Poussée de caisses
* Objectif : placer les caisses sur les cibles

## Mode récursif (Parabox)

* Mondes imbriqués
* Caisses contenant d’autres mondes
* Navigation entre niveaux internes

## Éditeurs

* Éditeur classique
* Éditeur récursif
* Outils : mur, vide, joueur, caisse, cible

## Autres

* Sauvegarde de niveaux
* Chargement de niveaux
* Résolution automatique (solver)



# Déploiement

Le projet utilise Maven pour :

* gérer les dépendances
* compiler automatiquement
* exécuter l’application

Scripts fournis :

* run.sh (Linux / macOS)
* run.bat (Windows)


