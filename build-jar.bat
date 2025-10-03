@echo off
echo ========================================
echo    CRÉATION DU JAR SOLUBANK
echo ========================================

REM Vérifier si les fichiers compilés existent
if not exist "out\com\solubank\Main.class" (
    echo ❌ ERREUR: Fichiers compilés non trouvés!
    echo Veuillez d'abord exécuter compile.bat
    echo ========================================
    pause
    exit /b 1
)

REM Créer le dossier dist s'il n'existe pas
if not exist "dist" mkdir dist

echo Création du manifeste...
REM Créer le fichier manifest
echo Main-Class: com.solubank.Main > manifest.txt
echo Class-Path: mysql-connector-j-9.4.0.jar >> manifest.txt

echo Création du JAR principal...
REM Créer le JAR avec le manifest
jar cfm dist\solubank-bank-anomaly-detector.jar manifest.txt -C out .

REM Copier les dépendances
echo Copie des dépendances...
copy lib\mysql-connector-j-9.4.0.jar dist\

REM Nettoyer le fichier manifest temporaire
del manifest.txt

REM Vérifier si la création du JAR a réussi
if exist "dist\solubank-bank-anomaly-detector.jar" (
    echo.
    echo ✅ JAR CRÉÉ AVEC SUCCÈS!
    echo Fichier: dist\solubank-bank-anomaly-detector.jar
    echo Dépendances: dist\mysql-connector-j-9.4.0.jar
    echo.
    echo Pour exécuter le JAR:
    echo java -jar dist\solubank-bank-anomaly-detector.jar
) else (
    echo.
    echo ❌ ERREUR LORS DE LA CRÉATION DU JAR!
)

echo ========================================
pause