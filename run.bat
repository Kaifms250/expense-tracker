@echo off
set ROOT=%~dp0
set OUT=%ROOT%target\classes
set GSON=%ROOT%lib\gson-2.11.0.jar

if not exist "%OUT%\com\expensetracker\Main.class" (
    echo Classes not found. Run compile.bat first.
    exit /b 1
)

echo Starting web server at http://localhost:8080
java -cp "%OUT%;%GSON%" com.expensetracker.Main
