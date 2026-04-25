@echo off
REM Event Organizer build script. Works in cmd.exe, PowerShell, and Windows
REM Terminal. Handles paths that contain spaces.
setlocal enableextensions enabledelayedexpansion
cd /d "%~dp0"

where javac >nul 2>&1
if errorlevel 1 (
    echo [error] javac not found on PATH.
    echo         Install JDK 11 or newer and ensure ^<jdk^>\bin is on your PATH.
    exit /b 1
)

if exist out rmdir /s /q out
mkdir out

REM Collect every .java under src into a sources file. javac's @-file parser
REM treats backslashes as escape characters, so we rewrite each path with
REM forward slashes (which javac accepts on Windows). Each path is also
REM wrapped in quotes so spaces in the project root survive parsing.
if exist .sources.txt del .sources.txt
set "FOUND=0"
(for /R src %%f in (*.java) do (
    set "P=%%f"
    set "P=!P:\=/!"
    echo "!P!"
    set "FOUND=1"
)) > .sources.txt

if "!FOUND!"=="0" (
    echo [error] no Java sources found under src\
    del .sources.txt
    exit /b 1
)

javac -d out -cp "lib\*" @.sources.txt
set BUILD_RC=%errorlevel%
del .sources.txt

if not "%BUILD_RC%"=="0" (
    echo [error] javac failed with exit code %BUILD_RC%
    exit /b %BUILD_RC%
)

REM Copy vendored fonts (if present) so FontLoader can find them on classpath.
if exist lib\fonts (
    if not exist out\fonts mkdir out\fonts
    copy /y lib\fonts\*.ttf out\fonts\ >nul 2>&1
)

echo Build OK -^> out\
endlocal
