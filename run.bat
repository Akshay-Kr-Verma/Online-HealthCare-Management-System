@echo off
echo ==========================================
echo    STARTING HEALTHCARE MANAGEMENT SYSTEM
echo ==========================================

:: 1. Create a 'bin' folder if it doesn't exist to store compiled files
if not exist bin mkdir bin

:: 2. Compile the Java files (DAO, GUI, MAIN) and put them in 'bin'
:: We include the MySQL Connector jar in the classpath (-cp)
echo Compiling Java Source Code...
javac -d bin -cp "lib/*;." src/dao/*.java src/gui/*.java src/main/*.java

:: Check if compilation worked
if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Compilation Failed! Check your code errors above.
    pause
    exit /b
)

:: 3. Run the Application
echo compilation successful! Launching App...
echo.
java -cp "bin;lib/*" main.MainApp

:: 4. Keep window open if app closes (so you can see errors)
pause