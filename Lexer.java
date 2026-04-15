// Lexer.java
// Analisador léxico do TONACO SCRIPT v2.0
// Suporta sintaxe v2 nativa e palavras-chave v1 para compatibilidade retroativa
// Criado por: Guilherme Lucas Tonaco Carvalho

package com.tonaco.tns;

import java.util.*;

public class Lexer {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;
    private int column = 1;

    private static final Map<String, TokenType> KEYWORDS = new HashMap<>();

    static {
        // === Palavras-chave v2 (canônicas) ===
        KEYWORDS.put("print",    TokenType.PRINT);
        KEYWORDS.put("let",      TokenType.LET);
        KEYWORDS.put("if",       TokenType.IF);
        KEYWORDS.put("else",     TokenType.ELSE);
        KEYWORDS.put("while",    TokenType.WHILE);
        KEYWORDS.put("for",      TokenType.FOR);
        KEYWORDS.put("function", TokenType.FUNCTION);
        KEYWORDS.put("return",   TokenType.RETURN);
        KEYWORDS.put("true",     TokenType.TRUE);
        KEYWORDS.put("false",    TokenType.FALSE);
        KEYWORDS.put("null",     TokenType.NULL);

        // === Comandos TNS v2 ===
        KEYWORDS.put("scan",     TokenType.SCAN);
        KEYWORDS.put("deepscan", TokenType.DEEPSCAN);
        KEYWORDS.put("download", TokenType.DOWNLOAD);
        KEYWORDS.put("analyze",  TokenType.ANALYZE);
        KEYWORDS.put("github",   TokenType.GITHUB);
        KEYWORDS.put("report",   TokenType.REPORT);
        KEYWORDS.put("sync",     TokenType.SYNC);
        KEYWORDS.put("share",    TokenType.SHARE);

        // === Aliases v1 → mapeados para tokens v2 (compatibilidade) ===
        KEYWORDS.put("escrever",  TokenType.PRINT);
        KEYWORDS.put("imprimir",  TokenType.PRINT);
        KEYWORDS.put("mostrar",   TokenType.PRINT);
        KEYWORDS.put("variavel",  TokenType.LET);
        KEYWORDS.put("var",       TokenType.LET);
        KEYWORDS.put("se",        TokenType.IF);
        KEYWORDS.put("senao",     TokenType.ELSE);
        KEYWORDS.put("enquanto",  TokenType.WHILE);
        KEYWORDS.put("funcao",    TokenType.FUNCTION);
        KEYWORDS.put("retornar",  TokenType.RETURN);
        KEYWORDS.put("verdadeiro",TokenType.TRUE);
        KEYWORDS.put("falso",     TokenType.FALSE);
        KEYWORDS.put("nulo",      TokenType.NULL);

        // === Aliases TNS v1 ===
        KEYWORDS.put("buscar",    TokenType.SCAN);
        KEYWORDS.put("scanear",   TokenType.DEEPSCAN);
        KEYWORDS.put("analisar",  TokenType.ANALYZE);
        KEYWORDS.put("baixar",    TokenType.DOWNLOAD);
        KEYWORDS.put("relatorio", TokenType.REPORT);
        KEYWORDS.put("sincronizar", TokenType.SYNC);
        KEYWORDS.put("compartilhar", TokenType.SHARE);
    }

    public Lexer(String source) {
        this.source = source;
    }

    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }
        tokens.add(new Token(TokenType.EOF, "", null, line, column));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(' -> addToken(TokenType.LPAREN);
            case ')' -> addToken(TokenType.RPAREN);
            case '{' -> addToken(TokenType.LBRACE);
            case '}' -> addToken(TokenType.RBRACE);
            case '[' -> addToken(TokenType.LBRACKET);
            case ']' -> addToken(TokenType.RBRACKET);
            case ',' -> addToken(TokenType.COMMA);
            case ';' -> addToken(TokenType.SEMICOLON);
            case ':' -> addToken(TokenType.COLON);
            case '.' -> addToken(TokenType.DOT);
            case '+' -> addToken(TokenType.PLUS);
            case '-' -> addToken(TokenType.MINUS);
            case '*' -> addToken(TokenType.MULTIPLY);
            case '%' -> addToken(TokenType.MODULO);
            case '=' -> addToken(match('=') ? TokenType.EQUALS     : TokenType.ASSIGN);
            case '!' -> addToken(match('=') ? TokenType.NOT_EQUALS : TokenType.NOT);
            case '<' -> addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
            case '>' -> addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
            case '&' -> { if (match('&')) addToken(TokenType.AND); }
            case '|' -> { if (match('|')) addToken(TokenType.OR);  }
            case '/' -> {
                if (match('/')) {
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else if (match('*')) {
                    blockComment();
                } else {
                    addToken(TokenType.DIVIDE);
                }
            }
            case '#' -> { while (peek() != '\n' && !isAtEnd()) advance(); } // comentário v1
            case ' ', '\r', '\t' -> {}
            case '\n' -> { line++; column = 1; }
            case '"' -> string('"');
            case '\'' -> string('\'');
            default -> {
                if (isDigit(c)) number();
                else if (isAlpha(c)) identifier();
                else System.err.printf("[Lexer] Linha %d, col %d: caractere desconhecido '%c'%n", line, column, c);
            }
        }
    }

    private void blockComment() {
        while (!(peek() == '*' && peekNext() == '/') && !isAtEnd()) {
            if (peek() == '\n') { line++; column = 1; }
            advance();
        }
        if (!isAtEnd()) { advance(); advance(); } // consume */
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();
        String text = source.substring(start, current);
        TokenType type = KEYWORDS.getOrDefault(text.toLowerCase(), TokenType.IDENTIFIER);
        addToken(type);
    }

    private void number() {
        while (isDigit(peek())) advance();
        if (peek() == '.' && isDigit(peekNext())) {
            advance();
            while (isDigit(peek())) advance();
        }
        String text = source.substring(start, current);
        try {
            addToken(TokenType.NUMBER, text.contains(".") ? Double.parseDouble(text) : Integer.parseInt(text));
        } catch (NumberFormatException e) {
            addToken(TokenType.NUMBER, text);
        }
    }

    private void string(char delimiter) {
        while (peek() != delimiter && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }
        if (isAtEnd()) {
            System.err.printf("[Lexer] String não fechada na linha %d%n", line);
            return;
        }
        advance();
        String value = source.substring(start + 1, current - 1);
        addToken(TokenType.STRING, value);
    }

    // --- Helpers ---
    private boolean match(char expected) {
        if (isAtEnd() || source.charAt(current) != expected) return false;
        current++; column++;
        return true;
    }

    private char advance() {
        char c = source.charAt(current++);
        column++;
        return c;
    }

    private char peek()     { return isAtEnd() ? '\0' : source.charAt(current); }
    private char peekNext() { return (current + 1 >= source.length()) ? '\0' : source.charAt(current + 1); }
    private boolean isAtEnd()        { return current >= source.length(); }
    private boolean isDigit(char c)  { return c >= '0' && c <= '9'; }
    private boolean isAlpha(char c)  { return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_'; }
    private boolean isAlphaNumeric(char c) { return isAlpha(c) || isDigit(c); }

    private void addToken(TokenType type) { addToken(type, null); }
    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line, column - (current - start)));
    }
}
