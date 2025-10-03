@echo off
echo ========================================
echo    EXÉCUTION DU SYSTÈME AL BARAKA
echo ========================================

REM Vérifier si les fichiers compilés existent
if not exist "out\com\solubank\Main.class" (
    echo ❌ ERREUR: Fichiers compilés non trouvés!
    echo Veuillez d'abord exécuter compile.bat
    echo ========================================
    pause
    exit /b 1
)

echo Démarrage de l'application...
echo.

REM Exécuter l'application avec le classpath incluant les fichiers compilés et le driver MySQL
java --enable-preview -cp "out;lib\mysql-connector-j-9.4.0.jar" com.solubank.Main

echo.
echo ========================================
echo Application terminée.
pause