#!/usr/bin/env bash
# build.sh — TONACO SCRIPT v2.0
# Uso: ./build.sh [clean|run|repl]
set -e
GSON_VERSION="2.10.1"
GSON_JAR="libs/gson-${GSON_VERSION}.jar"
GSON_URL="https://repo1.maven.org/maven2/com/google/code/gson/gson/${GSON_VERSION}/gson-${GSON_VERSION}.jar"
SRC="src/main/java"
BUILD="build/classes"
JAR="tonaco-script.jar"
MAIN="com.tonaco.tns.TonacoCompiler"

info(){ echo "[INFO] $*"; }
err() { echo "[ERRO] $*"; exit 1; }

check_java(){ command -v javac &>/dev/null||err "JDK nao encontrado. Instale JDK 17+"; info "Java OK: $(java -version 2>&1|head -1)"; }

download_gson(){
    [ -f "$GSON_JAR" ]&&{ info "Gson ja presente";return; }
    info "Baixando Gson ${GSON_VERSION}..."
    mkdir -p libs
    if command -v wget &>/dev/null;then wget -q "$GSON_URL" -O "$GSON_JAR"
    elif command -v curl &>/dev/null;then curl -sL "$GSON_URL" -o "$GSON_JAR"
    else err "wget ou curl nao encontrado"; fi
    info "Gson baixado"
}

compile(){
    mkdir -p "$BUILD"
    info "Compilando..."
    javac -cp "$GSON_JAR" -d "$BUILD" --release 17 \
        $SRC/com/tonaco/tns/TokenType.java \
        $SRC/com/tonaco/tns/Token.java \
        $SRC/com/tonaco/tns/ASTNodes.java \
        $SRC/com/tonaco/tns/Lexer.java \
        $SRC/com/tonaco/tns/Parser.java \
        $SRC/com/tonaco/tns/TypeSystem.java \
        $SRC/com/tonaco/tns/Bytecode.java \
        $SRC/com/tonaco/tns/SimilarityTranslator.java \
        $SRC/com/tonaco/tns/TradutorUniversal.java \
        $SRC/com/tonaco/tns/AutoEvolucao.java \
        $SRC/com/tonaco/tns/TonacoCompiler.java
    info "Compilacao concluida"
}

package(){
    info "Empacotando $JAR..."
    mkdir -p "$BUILD/META-INF"
    printf "Manifest-Version: 1.0\nMain-Class: $MAIN\nClass-Path: $GSON_JAR\n" > "$BUILD/META-INF/MANIFEST.MF"
    jar cfm "$JAR" "$BUILD/META-INF/MANIFEST.MF" -C "$BUILD" .
    info "JAR gerado: $JAR"
    info "Executar: java -cp \"$JAR:$GSON_JAR\" $MAIN scripts/exemplo_v2.tns"
}

case "${1:-}" in
    clean) rm -rf build/ "$JAR"; info "Limpo" ;;
    run)   check_java;download_gson;compile;java -cp "$BUILD:$GSON_JAR" $MAIN scripts/exemplo_v2.tns ;;
    repl)  check_java;download_gson;compile;java -cp "$BUILD:$GSON_JAR" $MAIN --repl ;;
    *)     check_java;download_gson;compile;package ;;
esac
