@echo off
REM Event Organizer launcher. Works in cmd.exe, PowerShell, and Windows Terminal.
setlocal enableextensions
cd /d "%~dp0"

where java >nul 2>&1
if errorlevel 1 (
    echo [error] java not found on PATH.
    echo         Install JDK 11 or newer and ensure ^<jdk^>\bin is on your PATH.
    exit /b 1
)

if not exist out\com\eventorganizer\Main.class (
    echo [info] no compiled classes found - running build first...
    call "%~dp0build.bat"
    if errorlevel 1 exit /b %errorlevel%
)

REM `lib\*` is interpreted by the JVM (not the shell); the semicolon is the
REM Windows classpath separator.
java -cp "out;lib\*" com.eventorganizer.Main %*
exit /b %errorlevel%
