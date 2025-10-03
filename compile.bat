@echo off
echo ========================================
echo    COMPILATION DU PROJET SOLUBANK
echo ========================================

REM Créer le répertoire de sortie s'il n'existe pas
if not exist "out" mkdir out

REM Compiler tous les fichiers Java avec le driver MySQL
echo Compilation en cours...
javac -cp "lib\mysql-connector-j-9.4.0.jar" -d out -encoding UTF-8 --enable-preview --release 17 src\main\java\com\solubank\*.java src\main\java\com\solubank\dao\*.java src\main\java\com\solubank\entity\*.java src\main\java\com\solubank\service\*.java src\main\java\com\solubank\ui\*.java src\main\java\com\solubank\util\*.java

REM Vérifier si la compilation a réussi
if %ERRORLEVEL% == 0 (
    echo.
    echo COMPILATION REUSSIE!
    echo Les fichiers compiles sont dans le dossier 'out'
) else (
    echo.
    echo ERREUR DE COMPILATION!
    echo Verifiez les erreurs ci-dessus.
)

echo ========================================
pause