@echo off
title Kore Enterprise DMS
echo ========================================================
echo Iniciando Kore Enterprise DMS...
echo Java 21 LTS: C:\Users\USER\.jdks\ms-21.0.12.1
echo ========================================================
set "JAVA_HOME=C:\Users\USER\.jdks\ms-21.0.12.1"
set "PATH=%JAVA_HOME%\bin;%PATH%"

cd /d "%~dp0"
call "C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.2\plugins\maven-plugin\lib\maven3\bin\mvn.cmd" spring-boot:run
pause
