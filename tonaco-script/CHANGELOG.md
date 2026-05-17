# Changelog — TONACO SCRIPT

## [2.0.0] — 2026

### Adicionado
- Pipeline de compilador completo: Lexer → Parser → AST → SemanticAnalyzer → BytecodeCompiler → VirtualMachine
- `TokenType.java` separado de `Token.java` (fix crítico: Java não permite duas classes públicas no mesmo arquivo)
- Estrutura de pacotes `com.tonaco.tns` em todos os arquivos
- `SemanticAnalyzer` com verificação de variáveis não declaradas, tipos incompatíveis, redeclaração de variáveis no mesmo escopo
- `VirtualMachine` com suporte a funções, pilha de retorno, frames de escopo, geração de relatórios HTML e JSON
- `BytecodeCompiler` com patch de chamadas de função, geração de JMPs com backpatching
- Suporte a `for` loop no Parser e Bytecode
- Suporte a `deepscan <url>, <profundidade>` e `download <url>, <destino>`
- REPL interativo (`--repl`) com suporte a blocos multi-linha
- Flags do compilador: `--tokens`, `--ast`, `--codegen`, `--check`, `--translate`, `--version`
- `SimilarityTranslator` reescrito com mapa direto cobrindo 100% dos aliases v1 + multilíngue
- `TradutorUniversal` com dicionários embutidos para PT, ES, FR, DE e detecção automática de idioma
- `AutoEvolucao` corrigido com package `com.tonaco.tns` e imports corretos
- `build.sh` (Linux/Mac) e `build.bat` (Windows) com download automático do Gson
- `pom.xml` Maven com fat JAR
- Scripts `.tns` reorganizados na pasta `scripts/`
- `exemplo_v1_legado.tns` para demonstrar compatibilidade retroativa
- `README.md` completo com todos os comandos, flags e exemplos
- `.gitignore` adequado

### Corrigido
- `Token.java` continha `TokenType` como segunda classe pública — movido para `TokenType.java`
- `AutoEvolucao.java` não tinha `package com.tonaco.tns`
- `TradutorUniversal.java` não tinha `package com.tonaco.tns`
- `SimilarityTranslator` não traduzia `variavel → let` nem `escrever → print`
- `config.tns` usava sintaxe incompatível com o parser (arrays com `[...]`)
- Ponto-e-vírgula agora é **opcional** (compatibilidade v1 sem `;`)

### Arquitetura
- `TonacoScriptInterpreter.java` (v1) mantido como legacy — não integrado ao compilador v2
- Separação clara: compilador v2 em `src/main/java/com/tonaco/tns/`, scripts em `scripts/`

---

## [1.0.0] — 2026 (legacy)

- Interpretador direto (`TonacoScriptInterpreter.java`)
- Comandos: `buscar`, `scanear`, `analisar`, `baixar`, `escrever`, `variavel`, `relatorio`
- `AutoEvolucao.java` com comandos personalizados
- `TradutorUniversal.java` com dicionário PT/ES/FR/DE
- Sem compilação para bytecode — execução linha a linha
