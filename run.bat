@echo off
setlocal
cd /d "%~dp0"
java -cp "out;lib/*" com.eventorganizer.Main
