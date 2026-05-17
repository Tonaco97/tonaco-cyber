@echo off
REM build.bat — Script de compilação do TONACO SCRIPT v2.0 (Windows)
REM Criado por: Guilherme Lucas Tonaco Carvalho
REM
REM Uso:
REM   build.bat            -> compila e empacota tonaco-script.jar
REM   build.bat clean      -> remove artefatos de build
REM   build.bat run        -> compila e executa o script de exemplo
REM   build.bat repl       -> abre o REPL interativo

setlocal enabledelayedexpansion

set GSON_VERSION=2.10.1
set GSON_JAR=libs\gson-%GSON_VERSION%.jar
set GSON_URL=https://repo1.maven.org/maven2/com/google/code/gson/gson/%GSON_VERSION%/gson-%GSON_VERSION%.jar
set SRC_DIR=src\main\java
set BUILD_DIR=build\classes
set JAR_NAME=tonaco-script.jar
set MAIN_CLASS=com.tonaco.tns.TonacoCompiler

if "%1"=="clean" goto :clean
if "%1"=="run"   goto :run
if "%1"=="repl"  goto :repl
goto :build

:check_java
    java -version >nul 2>&1
    if errorlevel 1 (
        echo [ERRO] Java nao encontrado. Instale o JDK 17+.
        exit /b 1
    )
    echo [INFO] Java OK
    goto :eof

:download_gson
    if exist "%GSON_JAR%" (
        echo [INFO] Gson ja presente.
        goto :eof
    )
    echo [INFO] Baixando Gson %GSON_VERSION%...
    if not exist libs mkdir libs
    powershell -Command "Invoke-WebRequest -Uri '%GSON_URL%' -OutFile '%GSON_JAR%'"
    echo [INFO] Gson baixado.
    goto :eof

:compile
    if not exist "%BUILD_DIR%" mkdir "%BUILD_DIR%"
    echo [INFO] Compilando fontes...

    javac -cp "%GSON_JAR%" ^
          -d "%BUILD_DIR%" ^
          --release 17 ^
          "%SRC_DIR%\com\tonaco\tns\TokenType.java" ^
          "%SRC_DIR%\com\tonaco\tns\Token.java" ^
          "%SRC_DIR%\com\tonaco\tns\ASTNodes.java" ^
          "%SRC_DIR%\com\tonaco\tns\Lexer.java" ^
          "%SRC_DIR%\com\tonaco\tns\Parser.java" ^
          "%SRC_DIR%\com\tonaco\tns\TypeSystem.java" ^
          "%SRC_DIR%\com\tonaco\tns\Bytecode.java" ^
          "%SRC_DIR%\com\tonaco\tns\SimilarityTranslator.java" ^
          "%SRC_DIR%\com\tonaco\tns\TradutorUniversal.java" ^
          "%SRC_DIR%\com\tonaco\tns\AutoEvolucao.java" ^
          "%SRC_DIR%\com\tonaco\tns\TonacoCompiler.java"

    if errorlevel 1 (
        echo [ERRO] Compilacao falhou.
        exit /b 1
    )
    echo [INFO] Compilacao concluida.
    goto :eof

:package
    echo [INFO] Empacotando %JAR_NAME%...
    if not exist "%BUILD_DIR%\META-INF" mkdir "%BUILD_DIR%\META-INF"

    (
        echo Manifest-Version: 1.0
        echo Main-Class: %MAIN_CLASS%
        echo Class-Path: libs/gson-%GSON_VERSION%.jar
        echo Created-By: TONACO SCRIPT Build System
        echo Implementation-Version: 2.0
    ) > "%BUILD_DIR%\META-INF\MANIFEST.MF"

    jar cfm "%JAR_NAME%" "%BUILD_DIR%\META-INF\MANIFEST.MF" -C "%BUILD_DIR%" .
    echo [INFO] JAR gerado: %JAR_NAME%
    echo.
    echo [INFO] Para executar:
    echo   java -cp "%JAR_NAME%;%GSON_JAR%" %MAIN_CLASS% scripts\exemplo_v2.tns
    echo   java -cp "%JAR_NAME%;%GSON_JAR%" %MAIN_CLASS% --repl
    goto :eof

:build
    call :check_java
    call :download_gson
    call :compile
    call :package
    goto :end

:run
    call :check_java
    call :download_gson
    call :compile
    java -cp "%BUILD_DIR%;%GSON_JAR%" %MAIN_CLASS% scripts\exemplo_v2.tns
    goto :end

:repl
    call :check_java
    call :download_gson
    call :compile
    java -cp "%BUILD_DIR%;%GSON_JAR%" %MAIN_CLASS% --repl
    goto :end

:clean
    echo [INFO] Limpando artefatos...
    if exist build rmdir /s /q build
    if exist "%JAR_NAME%" del "%JAR_NAME%"
    echo [INFO] Limpo.
    goto :end

:end
endlocal
