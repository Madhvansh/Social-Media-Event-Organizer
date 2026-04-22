@echo off
setlocal
cd /d "%~dp0"
if exist out rmdir /s /q out
mkdir out
dir /s /b src\*.java > .sources.txt
javac -d out -cp "lib/*" @.sources.txt
del .sources.txt
echo Build OK -^> out\
