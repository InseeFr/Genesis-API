# sndil-starter

springboot3 / java17

Projet starter qui pourrait servir à instancier les nouveaux projets.
Les contributions sont les bienvenues !

# Le projet
Un projet avec un simple contrôleur ayant deux endpoints

## Les properties
Deux fichiers sont livrés :
- application.properties avec la liste des propriétés
- application-dev.properties avec des valeurs utilisables en environnement local
Pour que ce second fichier soit pris en compte il faut mettre un profil spring 'dev'

## /starter/health
Endpoint accessible à tout le monde (authentifié ou non)

## /starter/healthadmin
Endpoint réservé aux administrateurs (identifiés par un rôle configuratble)


# Fonctionnalités

## SPRINGDOC-SWAGGER
paramétrable par properties

## Spring security
paramétrable par properties
(rôle admin ; découpage du JWT pour en extraire les rôles et le nom ; whitelist)

# Maven Wrapper 

Ce projet utilise [Maven Wrapper](https://maven.apache.org/wrapper/index.html) : si la distribution de maven ciblée par le wrapper n'est pas disponible dans le cache maven du système qui effectue le build, maven wrapper la télécharge avant d'effectuer le build du projet. Maven wrapper correspond aux deux exécutables `mvnw` (unix), `mvnw.cmd` (windows) et au binaire accompagné de ses propriétés dans le dossier `.mvn/wrapper`.

Pour faire appel au wrapper, il faut remplacer `mvn` par `mvnw` dans ses builds : un exemple de CI est à venir.
