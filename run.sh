#!/bin/bash

echo "Compilation du projet..."
mvn clean compile

if [ $? -ne 0 ]; then
    echo "Erreur de compilation"
    exit 1
fi

echo "Lancement de l'application..."
mvn javafx:run
