// TonacoCompiler.java
// Compilador, entry point e REPL do TONACO SCRIPT v2.0
// Pipeline: Lexer → Parser → SemanticAnalyzer → BytecodeCompiler → VM
// Criado por: Guilherme Lucas Tonaco Carvalho

package com.tonaco.tns;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class TonacoCompiler {

    private static final String VERSION = "2.0";
    private static final String BANNER =
        "╔══════════════════════════════════════════════════════════════╗\n" +
        "║        TONACO SCRIPT v" + VERSION + " — Compilador & VM                ║\n" +
        "║        Lexer · Parser · AST · TypeSystem · Bytecode · VM    ║\n" +
        "║        Tradutor Multilíngue por Similaridade Semântica       ║\n" +
        "║        Desenvolvedor: Guilherme Lucas Tonaco Carvalho        ║\n" +
        "╚══════════════════════════════════════════════════════════════╝";

    public static void main(String[] args) {
        System.out.println(BANNER);
        System.out.println();

        if (args.length == 0) {
            printUsage();
            return;
        }

        switch (args[0]) {
            case "--repl",   "-r"         -> repl();
            case "--tokens", "-t"         -> runFile(args[args.length - 1], Mode.TOKENS);
            case "--ast",    "-a"         -> runFile(args[args.length - 1], Mode.AST);
            case "--codegen","-c"         -> runFile(args[args.length - 1], Mode.CODEGEN);
            case "--run",    "-x"         -> runFile(args[args.length - 1], Mode.RUN);
            case "--check"               -> runFile(args[args.length - 1], Mode.CHECK);
            case "--translate"           -> translateFile(args[args.length - 1]);
            case "--version", "-v"       -> System.out.println("TONACO SCRIPT v" + VERSION);
            default -> {
                // Se não for flag, assume que é um arquivo .tns
                runFile(args[0], Mode.RUN);
            }
        }
    }

    // ============================================================
    // MODOS DE EXECUÇÃO
    // ============================================================

    private enum Mode { TOKENS, AST, CODEGEN, CHECK, RUN }

    private static void runFile(String filename, Mode mode) {
        Path path = Path.of(filename);
        if (!Files.exists(path)) {
            System.err.println("[Erro] Arquivo não encontrado: " + filename);
            System.exit(1);
        }

        try {
            String source = Files.readString(path);
            runSource(source, filename, mode);
        } catch (IOException e) {
            System.err.println("[Erro] Falha ao ler arquivo: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void runSource(String source, String filename, Mode mode) {
        // 1. Tradução multilíngue (v1 + aliases → v2 canônico)
        String translated = translateSource(source);

        // 2. Lexer
        Lexer lexer = new Lexer(translated);
        List<Token> tokens = lexer.scanTokens();

        if (mode == Mode.TOKENS) {
            System.out.println("[TOKENS] " + tokens.size() + " tokens:");
            tokens.forEach(t -> System.out.printf("  %-20s %s%n", t.type, t.lexeme));
            return;
        }

        // 3. Parser
        Parser parser = new Parser(tokens);
        ProgramNode ast;
        try {
            ast = parser.parse();
        } catch (RuntimeException e) {
            System.err.println("[PARSER FALHOU] " + e.getMessage());
            return;
        }

        System.out.printf("[LEXER]  %d tokens | [PARSER] AST construída%n", tokens.size());

        if (mode == Mode.AST) {
            printAST(ast, 0);
            return;
        }

        // 4. Análise semântica
        SemanticAnalyzer semantic = new SemanticAnalyzer();
        boolean ok = semantic.analyze(ast);

        if (!semantic.getWarnings().isEmpty()) {
            System.out.println("[AVISOS]");
            semantic.getWarnings().forEach(System.out::println);
        }

        if (!ok) {
            System.out.println("[ERROS SEMÂNTICOS]");
            semantic.getErrors().forEach(System.out::println);
            if (mode != Mode.CHECK) return;
        } else {
            System.out.println("[SEMANTIC] OK");
        }

        if (mode == Mode.CHECK) return;

        // 5. Compilar para bytecode
        BytecodeCompiler compiler = new BytecodeCompiler();
        List<Instruction> bytecode = compiler.compile(ast);

        System.out.printf("[BYTECODE] %d instruções geradas%n", bytecode.size());

        if (mode == Mode.CODEGEN) {
            System.out.println("\n[BYTECODE DUMP]");
            for (int i = 0; i < bytecode.size(); i++) {
                System.out.printf("  %4d: %s%n", i, bytecode.get(i));
            }
            return;
        }

        // 6. Executar na VM
        System.out.println("\n[VM] Executando...\n");
        System.out.println("─".repeat(60));
        VirtualMachine vm = new VirtualMachine();
        vm.execute(bytecode);
        System.out.println("─".repeat(60));
        System.out.println("\n[VM] Execução concluída com sucesso.");
    }

    // ============================================================
    // REPL INTERATIVO
    // ============================================================

    private static void repl() {
        System.out.println("[REPL] TONACO SCRIPT Interativo — digite 'sair' para encerrar\n");
        Scanner scanner = new Scanner(System.in);
        StringBuilder buffer = new StringBuilder();

        while (true) {
            System.out.print(buffer.isEmpty() ? "tns> " : "...  ");
            if (!scanner.hasNextLine()) break;
            String line = scanner.nextLine().trim();

            if (line.equalsIgnoreCase("sair") || line.equalsIgnoreCase("exit")) {
                System.out.println("Até logo!");
                break;
            }

            if (line.equalsIgnoreCase("limpar") || line.equalsIgnoreCase("clear")) {
                buffer.setLength(0);
                System.out.println("[REPL] Buffer limpo.");
                continue;
            }

            buffer.append(line).append("\n");

            // Executa quando a linha não termina com '{' nem está em bloco aberto
            if (!line.endsWith("{") && countBraces(buffer.toString()) == 0) {
                String code = buffer.toString();
                buffer.setLength(0);
                try {
                    runSource(code, "<repl>", Mode.RUN);
                } catch (Exception e) {
                    System.err.println("[REPL Erro] " + e.getMessage());
                }
            }
        }
    }

    private static int countBraces(String s) {
        int count = 0;
        for (char c : s.toCharArray()) {
            if (c == '{') count++;
            if (c == '}') count--;
        }
        return count;
    }

    // ============================================================
    // TRADUÇÃO MULTILÍNGUE
    // ============================================================

    private static String translateSource(String source) {
        String[] lines = source.split("\n");
        StringBuilder out = new StringBuilder();
        for (String line : lines) {
            String trimmed = line.trim();
            // Preserva comentários e linhas vazias sem tradução
            if (trimmed.isEmpty() || trimmed.startsWith("//") || trimmed.startsWith("#")) {
                out.append(line).append("\n");
            } else {
                out.append(SimilarityTranslator.translateCommand(line)).append("\n");
            }
        }
        return out.toString();
    }

    private static void translateFile(String filename) {
        try {
            String source = Files.readString(Path.of(filename));
            String translated = translateSource(source);
            System.out.println("[TRADUÇÃO] " + filename + " → sintaxe v2 canônica:\n");
            System.out.println(translated);
        } catch (IOException e) {
            System.err.println("[Erro] " + e.getMessage());
        }
    }

    // ============================================================
    // PRINT AST
    // ============================================================

    private static void printAST(ASTNode node, int depth) {
        String indent = "  ".repeat(depth);
        if (node == null) { System.out.println(indent + "null"); return; }
        System.out.println(indent + node.getClass().getSimpleName());
        // Impressão simples por reflexão
        for (var field : node.getClass().getDeclaredFields()) {
            try {
                field.setAccessible(true);
                Object val = field.get(node);
                if (val instanceof ASTNode child) {
                    System.out.println(indent + "  ." + field.getName() + ":");
                    printAST(child, depth + 2);
                } else if (val instanceof List<?> list) {
                    System.out.println(indent + "  ." + field.getName() + " [" + list.size() + "]:");
                    for (Object item : list) {
                        if (item instanceof ASTNode n) printAST(n, depth + 2);
                    }
                } else {
                    System.out.println(indent + "  ." + field.getName() + " = " + val);
                }
            } catch (Exception ignored) {}
        }
    }

    // ============================================================
    // AJUDA
    // ============================================================

    private static void printUsage() {
        System.out.println("Uso:");
        System.out.println("  java -cp . com.tonaco.tns.TonacoCompiler <arquivo.tns>     # executar");
        System.out.println("  java -cp . com.tonaco.tns.TonacoCompiler --run    <arquivo>");
        System.out.println("  java -cp . com.tonaco.tns.TonacoCompiler --tokens <arquivo> # ver tokens");
        System.out.println("  java -cp . com.tonaco.tns.TonacoCompiler --ast    <arquivo> # ver AST");
        System.out.println("  java -cp . com.tonaco.tns.TonacoCompiler --codegen <arquivo># ver bytecode");
        System.out.println("  java -cp . com.tonaco.tns.TonacoCompiler --check  <arquivo> # só análise semântica");
        System.out.println("  java -cp . com.tonaco.tns.TonacoCompiler --translate <arq>  # ver tradução v1→v2");
        System.out.println("  java -cp . com.tonaco.tns.TonacoCompiler --repl             # modo interativo");
        System.out.println("  java -cp . com.tonaco.tns.TonacoCompiler --version");
    }
}
