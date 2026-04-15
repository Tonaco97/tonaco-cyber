// TonacoCompiler.java
// Compilador completo do TONACO SCRIPT v2.0
// Criado por: Guilherme Lucas Tonaco Carvalho

package com.tonaco.tns;

import java.nio.file.*;
import java.util.*;

public class TonacoCompiler {
    
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║      TONACO SCRIPT v2.0 - Compilador e VM                    ║");
        System.out.println("║           Lexer | Parser | AST | Type System | VM            ║");
        System.out.println("║           Tradutor por Similaridade Semântica                ║");
        System.out.println("║           Desenvolvedor: Guilherme Lucas Tonaco Carvalho     ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println();
        
        if (args.length == 0) {
            System.out.println("Uso: java TonacoCompiler arquivo.tns");
            System.out.println("     java TonacoCompiler --run arquivo.tns");
            System.out.println("     java TonacoCompiler --codegen arquivo.tns");
            return;
        }
        
        String filename = args[args.length - 1];
        
        try {
            String source = Files.readString(Path.of(filename));
            
            // Tradução inteligente (similarity-based)
            String translated = translateSource(source);
            
            // 1. Lexer
            Lexer lexer = new Lexer(translated);
            List<Token> tokens = lexer.scanTokens();
            
            System.out.println("[LEXER] " + tokens.size() + " tokens encontrados");
            
            // 2. Parser
            Parser parser = new Parser(tokens);
            ProgramNode ast = parser.parse();
            
            System.out.println("[PARSER] AST construída com sucesso");
            
            // 3. Semantic Analyzer
            SemanticAnalyzer semantic = new SemanticAnalyzer();
            List<String> errors = semantic.analyze(ast);
            
            if (!errors.isEmpty()) {
                System.out.println("\n[ERROS SEMÂNTICOS]");
                for (String err : errors) {
                    System.out.println("  ❌ " + err);
                }
                return;
            }
            
            System.out.println("[SEMANTIC] Análise semântica aprovada");
            
            // 4. Bytecode Compiler
            BytecodeCompiler compiler = new BytecodeCompiler();
            List<Instruction> bytecode = compiler.compile(ast);
            
            System.out.println("[BYTECODE] " + bytecode.size() + " instruções geradas");
            
            // 5. VM Execution
            if (args[0].equals("--run") || args.length == 1) {
                System.out.println("\n[VM] Executando...\n");
                VirtualMachine vm = new VirtualMachine();
                vm.execute(bytecode);
                System.out.println("\n[VM] Execução concluída");
            }
            
            // 6. CodeGen opcional
            if (args[0].equals("--codegen")) {
                System.out.println("\n[CODEGEN] Bytecode:");
                for (int i = 0; i < bytecode.size(); i++) {
                    System.out.println("  " + i + ": " + bytecode.get(i));
                }
            }
            
        } catch (Exception e) {
            System.err.println("Erro: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static String translateSource(String source) {
        String[] lines = source.split("\n");
        StringBuilder translated = new StringBuilder();
        
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("//")) {
                translated.append(line).append("\n");
                continue;
            }
            
            // Traduz cada linha usando similarity
            String translatedLine = SimilarityTranslator.translateCommand(line);
            translated.append(translatedLine).append("\n");
        }
        
        return translated.toString();
    }
}
