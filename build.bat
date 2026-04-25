@echo off
setlocal
cd /d "%~dp0"
if exist out rmdir /s /q out
mkdir out
dir /s /b src\*.java > .sources.txt
javac -d out -cp "lib/*" @.sources.txt
del .sources.txt
if exist lib\fonts (
    if not exist out\fonts mkdir out\fonts
    copy /y lib\fonts\*.ttf out\fonts\ >nul 2>&1
)
echo Build OK -^> out\
