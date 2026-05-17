package com.tonaco.tns;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class TonacoCompiler {
    private static final String VERSION="2.0";
    private static final String BANNER=
        "╔══════════════════════════════════════════════════════════════╗\n"+
        "║        TONACO SCRIPT v2.0 — Compilador & VM                 ║\n"+
        "║        Lexer · Parser · AST · TypeSystem · Bytecode · VM    ║\n"+
        "║        Desenvolvedor: Guilherme Lucas Tonaco Carvalho        ║\n"+
        "╚══════════════════════════════════════════════════════════════╝";

    public static void main(String[] args){
        System.out.println(BANNER);System.out.println();
        if(args.length==0){printUsage();return;}
        switch(args[0]){
            case "--repl","-r"->repl();
            case "--tokens","-t"->runFile(args[args.length-1],Mode.TOKENS);
            case "--ast","-a"->runFile(args[args.length-1],Mode.AST);
            case "--codegen","-c"->runFile(args[args.length-1],Mode.CODEGEN);
            case "--check"->runFile(args[args.length-1],Mode.CHECK);
            case "--translate"->translateFile(args[args.length-1]);
            case "--version","-v"->System.out.println("TONACO SCRIPT v"+VERSION);
            default->runFile(args[0],Mode.RUN);
        }
    }
    private enum Mode{TOKENS,AST,CODEGEN,CHECK,RUN}
    private static void runFile(String f,Mode mode){
        try{runSource(Files.readString(Path.of(f)),f,mode);}
        catch(IOException e){System.err.println("[Erro] "+e.getMessage());System.exit(1);}
    }
    private static void runSource(String source,String name,Mode mode){
        String tr=translateSource(source);
        Lexer lexer=new Lexer(tr);
        List<Token> tokens=lexer.scanTokens();
        if(mode==Mode.TOKENS){System.out.println("[TOKENS] "+tokens.size());tokens.forEach(t->System.out.printf("  %-20s %s%n",t.type,t.lexeme));return;}
        Parser parser=new Parser(tokens);ProgramNode ast;
        try{ast=parser.parse();}catch(RuntimeException e){System.err.println("[PARSER FALHOU] "+e.getMessage());return;}
        System.out.printf("[LEXER] %d tokens | [PARSER] AST OK%n",tokens.size());
        if(mode==Mode.AST){System.out.println("[AST] "+ast.statements.size()+" statements");return;}
        SemanticAnalyzer sem=new SemanticAnalyzer();boolean ok=sem.analyze(ast);
        if(!sem.getWarnings().isEmpty()){System.out.println("[AVISOS]");sem.getWarnings().forEach(System.out::println);}
        if(!ok){System.out.println("[ERROS SEMANTICOS]");sem.getErrors().forEach(System.out::println);if(mode!=Mode.CHECK)return;}
        else System.out.println("[SEMANTIC] OK");
        if(mode==Mode.CHECK)return;
        BytecodeCompiler bc=new BytecodeCompiler();List<Instruction> bytecode=bc.compile(ast);
        System.out.printf("[BYTECODE] %d instrucoes%n",bytecode.size());
        if(mode==Mode.CODEGEN){System.out.println("\n[BYTECODE DUMP]");for(int i=0;i<bytecode.size();i++)System.out.printf("  %4d: %s%n",i,bytecode.get(i));return;}
        System.out.println("\n[VM] Executando...\n"+"─".repeat(60));
        new VirtualMachine().execute(bytecode);
        System.out.println("─".repeat(60)+"\n[VM] Concluido.");
    }
    private static void repl(){
        System.out.println("[REPL] Digite 'sair' para encerrar\n");
        Scanner sc=new Scanner(System.in);StringBuilder buf=new StringBuilder();
        while(true){
            System.out.print(buf.isEmpty()?"tns> ":"...  ");
            if(!sc.hasNextLine())break;
            String line=sc.nextLine().trim();
            if(line.equalsIgnoreCase("sair")||line.equalsIgnoreCase("exit")){System.out.println("Ate logo!");break;}
            if(line.equalsIgnoreCase("limpar")||line.equalsIgnoreCase("clear")){buf.setLength(0);continue;}
            buf.append(line).append("\n");
            if(!line.endsWith("{")&&countBraces(buf.toString())==0){String c=buf.toString();buf.setLength(0);try{runSource(c,"<repl>",Mode.RUN);}catch(Exception e){System.err.println("[REPL] "+e.getMessage());}}
        }
    }
    private static int countBraces(String s){int c=0;for(char ch:s.toCharArray()){if(ch=='{')c++;if(ch=='}')c--;}return c;}
    private static String translateSource(String src){
        String[] lines=src.split("\n");StringBuilder out=new StringBuilder();
        for(String l:lines){String t=l.trim();if(t.isEmpty()||t.startsWith("//")||t.startsWith("#"))out.append(l);else out.append(SimilarityTranslator.translateCommand(l));out.append("\n");}
        return out.toString();
    }
    private static void translateFile(String f){try{System.out.println(translateSource(Files.readString(Path.of(f))));}catch(IOException e){System.err.println("[Erro] "+e.getMessage());}}
    private static void printUsage(){
        System.out.println("Uso:");
        System.out.println("  java ... TonacoCompiler <arquivo.tns>         # executar");
        System.out.println("  java ... TonacoCompiler --tokens  <arquivo>   # ver tokens");
        System.out.println("  java ... TonacoCompiler --ast     <arquivo>   # ver AST");
        System.out.println("  java ... TonacoCompiler --codegen <arquivo>   # ver bytecode");
        System.out.println("  java ... TonacoCompiler --check   <arquivo>   # so analise semantica");
        System.out.println("  java ... TonacoCompiler --translate <arquivo> # traduz v1->v2");
        System.out.println("  java ... TonacoCompiler --repl                # REPL interativo");
        System.out.println("  java ... TonacoCompiler --version");
    }
}
