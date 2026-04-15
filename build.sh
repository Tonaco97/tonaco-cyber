#!/usr/bin/env bash
# build.sh — Script de compilação do TONACO SCRIPT v2.0
# Criado por: Guilherme Lucas Tonaco Carvalho
#
# Uso:
#   ./build.sh            → compila e empacota tonaco-script.jar
#   ./build.sh clean      → remove artefatos de build
#   ./build.sh run        → compila e executa o script de exemplo
#   ./build.sh repl       → abre o REPL interativo

set -e

GSON_VERSION="2.10.1"
GSON_JAR="libs/gson-${GSON_VERSION}.jar"
GSON_URL="https://repo1.maven.org/maven2/com/google/code/gson/gson/${GSON_VERSION}/gson-${GSON_VERSION}.jar"
SRC_DIR="src/main/java"
BUILD_DIR="build/classes"
JAR_NAME="tonaco-script.jar"
MAIN_CLASS="com.tonaco.tns.TonacoCompiler"

# ─── Cores ──────────────────────────────────────────────────────
GRN='\033[0;32m'; YLW='\033[1;33m'; RED='\033[0;31m'; RST='\033[0m'

info()  { echo -e "${GRN}[INFO]${RST} $*"; }
warn()  { echo -e "${YLW}[WARN]${RST} $*"; }
error() { echo -e "${RED}[ERRO]${RST} $*"; exit 1; }

# ─── Verificações de dependência ────────────────────────────────
check_java() {
    if ! command -v java &>/dev/null; then
        error "Java não encontrado. Instale o JDK 17+ e tente novamente."
    fi
    JAVA_VER=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
    if [ "${JAVA_VER}" -lt 17 ] 2>/dev/null; then
        warn "Java ${JAVA_VER} detectado. Recomendado: Java 17+."
    else
        info "Java ${JAVA_VER} OK"
    fi
}

# ─── Download do Gson ───────────────────────────────────────────
download_gson() {
    if [ -f "${GSON_JAR}" ]; then
        info "Gson já presente: ${GSON_JAR}"
        return
    fi
    info "Baixando Gson ${GSON_VERSION}..."
    mkdir -p libs
    if command -v wget &>/dev/null; then
        wget -q "${GSON_URL}" -O "${GSON_JAR}"
    elif command -v curl &>/dev/null; then
        curl -sL "${GSON_URL}" -o "${GSON_JAR}"
    else
        error "Wget ou curl não encontrados. Baixe manualmente: ${GSON_URL}"
    fi
    info "Gson baixado: ${GSON_JAR}"
}

# ─── Compilação ─────────────────────────────────────────────────
compile() {
    mkdir -p "${BUILD_DIR}"
    info "Compilando fontes em ${SRC_DIR}..."

    # Ordem de compilação garante dependências corretas
    SOURCES=(
        "${SRC_DIR}/com/tonaco/tns/TokenType.java"
        "${SRC_DIR}/com/tonaco/tns/Token.java"
        "${SRC_DIR}/com/tonaco/tns/ASTNodes.java"
        "${SRC_DIR}/com/tonaco/tns/Lexer.java"
        "${SRC_DIR}/com/tonaco/tns/Parser.java"
        "${SRC_DIR}/com/tonaco/tns/TypeSystem.java"
        "${SRC_DIR}/com/tonaco/tns/Bytecode.java"
        "${SRC_DIR}/com/tonaco/tns/SimilarityTranslator.java"
        "${SRC_DIR}/com/tonaco/tns/TradutorUniversal.java"
        "${SRC_DIR}/com/tonaco/tns/AutoEvolucao.java"
        "${SRC_DIR}/com/tonaco/tns/TonacoCompiler.java"
    )

    javac -cp "${GSON_JAR}" \
          -d "${BUILD_DIR}" \
          --release 17 \
          "${SOURCES[@]}"

    info "Compilação concluída."
}

# ─── Empacotamento ──────────────────────────────────────────────
package() {
    info "Empacotando ${JAR_NAME}..."

    # Cria MANIFEST
    mkdir -p "${BUILD_DIR}/META-INF"
    cat > "${BUILD_DIR}/META-INF/MANIFEST.MF" <<EOF
Manifest-Version: 1.0
Main-Class: ${MAIN_CLASS}
Class-Path: libs/gson-${GSON_VERSION}.jar
Created-By: TONACO SCRIPT Build System
Implementation-Version: 2.0
EOF

    jar cfm "${JAR_NAME}" "${BUILD_DIR}/META-INF/MANIFEST.MF" -C "${BUILD_DIR}" .

    info "JAR gerado: ${JAR_NAME}"
    info ""
    info "Para executar:"
    info "  java -cp '${JAR_NAME}:${GSON_JAR}' ${MAIN_CLASS} scripts/exemplo_v2.tns"
    info "  java -cp '${JAR_NAME}:${GSON_JAR}' ${MAIN_CLASS} --repl"
}

# ─── Limpeza ────────────────────────────────────────────────────
clean() {
    info "Limpando artefatos..."
    rm -rf build/ "${JAR_NAME}"
    info "Limpo."
}

# ─── Execução de exemplo ────────────────────────────────────────
run_example() {
    compile
    info "Executando scripts/exemplo_v2.tns..."
    java -cp "${BUILD_DIR}:${GSON_JAR}" "${MAIN_CLASS}" scripts/exemplo_v2.tns
}

repl() {
    compile
    info "Abrindo REPL..."
    java -cp "${BUILD_DIR}:${GSON_JAR}" "${MAIN_CLASS}" --repl
}

# ─── Entry point ────────────────────────────────────────────────
case "${1:-}" in
    clean)   clean ;;
    run)     check_java; download_gson; run_example ;;
    repl)    check_java; download_gson; repl ;;
    *)
        check_java
        download_gson
        compile
        package
        ;;
esac
