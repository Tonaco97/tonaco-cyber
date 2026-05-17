# TONACO SCRIPT (TNS) v2.0

> **Linguagem de programação para automação e segurança ofensiva.**  
> Pipeline completo: Lexer → Parser → AST → Análise Semântica → Bytecode → VM  
> Suporte multilíngue por similaridade semântica (PT, EN, ES, FR, DE, IT)

---

## Sumário

- [O que é](#o-que-é)
- [Arquitetura](#arquitetura)
- [Instalação e Build](#instalação-e-build)
- [Uso Rápido](#uso-rápido)
- [Sintaxe](#sintaxe)
- [Compatibilidade v1](#compatibilidade-v1)
- [Comandos TNS](#comandos-tns)
- [Suporte Multilíngue](#suporte-multilíngue)
- [REPL Interativo](#repl-interativo)
- [Flags do Compilador](#flags-do-compilador)
- [Auto-Evolução](#auto-evolução)

---

## O que é

TONACO SCRIPT é uma linguagem de scripting com compilador próprio criada para automação de tarefas de segurança ofensiva, recon e integração com ferramentas como GitHub. Possui compilador completo implementado em Java com geração de bytecode e VM própria.

**Desenvolvedor:** Guilherme Lucas Tonaco Carvalho  
**Marca:** [Tonaco Cyber](https://github.com/Tonaco97)

---

## Arquitetura

```
Código-fonte (.tns)
       ↓
TradutorUniversal / SimilarityTranslator   ← Normaliza v1 e multilíngue para v2
       ↓
Lexer          (Lexer.java)                ← Tokenização
       ↓
Parser         (Parser.java)               ← Árvore sintática (AST)
       ↓
SemanticAnalyzer (TypeSystem.java)         ← Verificação de tipos e escopos
       ↓
BytecodeCompiler (Bytecode.java)           ← Geração de bytecode
       ↓
VirtualMachine   (Bytecode.java)           ← Execução
```

**Arquivos principais:**

| Arquivo                  | Responsabilidade                             |
|--------------------------|----------------------------------------------|
| `TokenType.java`         | Enum de todos os tipos de token              |
| `Token.java`             | Estrutura de um token (type, lexeme, linha)  |
| `ASTNodes.java`          | Todos os nós da AST + interface Visitor      |
| `Lexer.java`             | Tokenização com suporte a aliases v1         |
| `Parser.java`            | Recursive descent parser completo            |
| `TypeSystem.java`        | Enum TNSType + SemanticAnalyzer              |
| `Bytecode.java`          | BytecodeCompiler + VirtualMachine            |
| `SimilarityTranslator.java` | Tradução por embeddings semânticos        |
| `TradutorUniversal.java` | Dicionários PT/ES/FR/DE/IT                   |
| `AutoEvolucao.java`      | Comandos personalizados + comunidade         |
| `TonacoCompiler.java`    | Entry point, REPL, orchestração do pipeline  |

---

## Instalação e Build

### Pré-requisitos

- Java JDK 17+
- `wget` ou `curl` (para baixar Gson automaticamente)

### Opção 1 — build.sh (Linux/Mac)

```bash
chmod +x build.sh
./build.sh          # compila e gera tonaco-script.jar
./build.sh run      # compila e executa exemplo
./build.sh repl     # abre o REPL interativo
./build.sh clean    # limpa artefatos
```

### Opção 2 — build.bat (Windows)

```cmd
build.bat
build.bat run
build.bat repl
build.bat clean
```

### Opção 3 — Maven

```bash
mvn package
# Gera: target/tonaco-script.jar (fat JAR com Gson embutido)
```

### Executar um arquivo .tns

```bash
# Com o JAR
java -cp "tonaco-script.jar:libs/gson-2.10.1.jar" com.tonaco.tns.TonacoCompiler scripts/exemplo_v2.tns

# Windows
java -cp "tonaco-script.jar;libs\gson-2.10.1.jar" com.tonaco.tns.TonacoCompiler scripts\exemplo_v2.tns
```

---

## Uso Rápido

```tns
let alvo = "https://exemplo.com"

print "Iniciando recon em " + alvo

scan alvo
analyze alvo
report html
```

Execute:
```bash
./build.sh run
```

---

## Sintaxe

### Variáveis

```tns
let nome = "TONACO"
let versao:int = 2
let ativo:bool = verdadeiro
```

### Condicionais

```tns
if (ativo) {
    print "Online"
} else {
    print "Offline"
}
```

### Loops

```tns
// while
let i = 0
while (i < 10) {
    print i
    i = i + 1
}

// for
for (let j = 0; j < 5; j = j + 1) {
    print j
}
```

### Funções

```tns
function saudar(nome) {
    print "Olá, " + nome + "!"
    return verdadeiro
}

saudar("Tonaco Cyber")
```

### Operadores

| Operador | Descrição              |
|----------|------------------------|
| `+`      | Soma / concatenação    |
| `-` `*` `/` `%` | Aritmética    |
| `==` `!=` | Igualdade             |
| `<` `>` `<=` `>=` | Comparação  |
| `&&` `\|\|` `!` | Lógica          |

---

## Compatibilidade v1

Scripts escritos em sintaxe v1 funcionam automaticamente no compilador v2 via `SimilarityTranslator`:

```tns
# Sintaxe v1 — funciona no compilador v2
escrever "Olá mundo"
variavel site = "https://exemplo.com"
buscar site
analisar site
relatorio html
```

Para ver a tradução gerada antes de executar:
```bash
java ... TonacoCompiler --translate scripts/exemplo_v1_legado.tns
```

---

## Comandos TNS

| Comando v2   | Alias v1      | Descrição                           |
|--------------|---------------|-------------------------------------|
| `scan`       | `buscar`      | Varredura de URLs e arquivos        |
| `deepscan`   | `scanear`     | Varredura profunda com profundidade |
| `analyze`    | `analisar`    | Análise de segurança                |
| `download`   | `baixar`      | Download de recursos                |
| `github`     | —             | Integração com GitHub               |
| `report`     | `relatorio`   | Gera relatório (`html` ou `json`)   |
| `sync`       | `sincronizar` | Sincroniza comandos da comunidade   |
| `share`      | `compartilhar`| Compartilha comando personalizado   |

```tns
scan "https://alvo.com"
deepscan "https://alvo.com", 3
analyze "https://alvo.com"
download "https://alvo.com/wordlist.txt", "./downloads/"
github "https://github.com/Tonaco97/tonaco-cyber"
report html
report json
sync
```

---

## Suporte Multilíngue

TONACO SCRIPT aceita código em português, inglês, espanhol, francês, alemão e italiano. O `SimilarityTranslator` usa embeddings semânticos de 8 dimensões para mapear qualquer palavra similar ao comando correto.

```tns
// Todos equivalentes:
print  "olá"   // inglês (v2 canônico)
escrever "olá" // português
afficher "olá" // francês
anzeigen "olá" // alemão
mostrar  "olá" // espanhol/português

buscar "https://alvo.com"  // PT
scan   "https://alvo.com"  // EN
chercher "https://alvo.com" // FR
suchen "https://alvo.com"  // DE
```

---

## REPL Interativo

```bash
./build.sh repl
```

```
tns> let x = 42
tns> print x + 8
50
tns> scan "https://exemplo.com"
[TNS SCAN] → https://exemplo.com
tns> sair
Até logo!
```

Comandos especiais no REPL:
- `sair` / `exit` — encerra
- `limpar` / `clear` — limpa o buffer

---

## Flags do Compilador

| Flag            | Descrição                              |
|-----------------|----------------------------------------|
| `<arquivo.tns>` | Executa o arquivo                      |
| `--run`         | Executa (equivalente ao padrão)        |
| `--tokens`      | Exibe lista de tokens gerados pelo Lexer |
| `--ast`         | Exibe a AST completa                   |
| `--codegen`     | Exibe dump do bytecode gerado          |
| `--check`       | Só análise semântica, não executa      |
| `--translate`   | Exibe a tradução v1→v2 do arquivo      |
| `--repl`        | Abre o REPL interativo                 |
| `--version`     | Exibe a versão                         |

---

## Auto-Evolução

Crie comandos personalizados e compartilhe com a comunidade:

```tns
// Dentro de um script ou via API Java
criar_comando "ping" "Verifica se site está online" "analyze {param}"
compartilhar "ping"
sincronizar
```

Comandos são persistidos em `.tonaco_comandos/comandos.json`.

---

## Licença

Propriedade de Guilherme Lucas Tonaco Carvalho / Tonaco Cyber.  
Uso autorizado para fins educacionais e de segurança ofensiva autorizada.

---

*Feito com precisão cirúrgica por root_GLTC*
