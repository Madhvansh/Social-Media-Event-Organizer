@echo off
REM Compile + run the JUnit 5 test suite. Works in cmd.exe, PowerShell, and
REM paths that contain spaces.
setlocal enableextensions enabledelayedexpansion
cd /d "%~dp0"

where javac >nul 2>&1
if errorlevel 1 (
    echo [error] javac not found on PATH.
    exit /b 1
)

if not exist out\com\eventorganizer\Main.class (
    echo [info] no production classes - running build first...
    call "%~dp0build.bat"
    if errorlevel 1 exit /b %errorlevel%
)

if exist out-test rmdir /s /q out-test
mkdir out-test

if exist .test-sources.txt del .test-sources.txt
set "FOUND=0"
(for /R test %%f in (*.java) do (
    set "P=%%f"
    set "P=!P:\=/!"
    echo "!P!"
    set "FOUND=1"
)) > .test-sources.txt

if "!FOUND!"=="0" (
    echo [error] no test sources found under test\
    del .test-sources.txt
    exit /b 1
)

javac -d out-test -cp "out;lib\*" @.test-sources.txt
set BUILD_RC=%errorlevel%
del .test-sources.txt
if not "%BUILD_RC%"=="0" (
    echo [error] test compile failed
    exit /b %BUILD_RC%
)

java -jar lib\junit-platform-console-standalone-1.10.2.jar ^
    -cp "out;out-test" --scan-classpath --details=tree
exit /b %errorlevel%
