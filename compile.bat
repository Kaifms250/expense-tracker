@echo off
setlocal enabledelayedexpansion
set ROOT=%~dp0
set SRC=%ROOT%src\main\java
set RES=%ROOT%src\main\resources
set OUT=%ROOT%target\classes
set GSON=%ROOT%lib\gson-2.11.0.jar
set LIB=%ROOT%lib

if not exist "%GSON%" (
    echo Downloading Gson...
    mkdir "%LIB%" 2>nul
    powershell -NoProfile -Command "Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/com/google/code/gson/gson/2.11.0/gson-2.11.0.jar' -OutFile '%GSON%'"
)

mkdir "%OUT%" 2>nul

set FILES=
for /r "%SRC%" %%f in (*.java) do set FILES=!FILES! "%%f"

javac -encoding UTF-8 -cp "%GSON%" -d "%OUT%" %FILES%
if errorlevel 1 exit /b 1

echo Copying web resources...
xcopy /E /I /Y "%RES%\*" "%OUT%\" >nul

echo Build successful.
