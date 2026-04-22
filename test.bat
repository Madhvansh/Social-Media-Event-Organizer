@echo off
setlocal
cd /d "%~dp0"
if exist out-test rmdir /s /q out-test
mkdir out-test
dir /s /b test\*.java > .test-sources.txt
javac -d out-test -cp "out;lib/*" @.test-sources.txt
del .test-sources.txt
java -jar lib/junit-platform-console-standalone-1.10.2.jar ^
    -cp "out;out-test" --scan-classpath --details=tree
