@echo off

echo Compilation du projet...
call mvn clean compile

if errorlevel 1 (
    echo Erreur de compilation
    pause
    exit /b 1
)

echo Lancement de l'application...
call mvn javafx:run

pause
