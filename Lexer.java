// Lexer.java
// Analisador léxico do TONACO SCRIPT
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
    
    private static final Map<String, TokenType> keywords = new HashMap<>();
    
    static {
        keywords.put("print", TokenType.PRINT);
        keywords.put("let", TokenType.LET);
        keywords.put("if", TokenType.IF);
        keywords.put("else", TokenType.ELSE);
        keywords.put("while", TokenType.WHILE);
        keywords.put("for", TokenType.FOR);
        keywords.put("function", TokenType.FUNCTION);
        keywords.put("return", TokenType.RETURN);
        keywords.put("true", TokenType.TRUE);
        keywords.put("false", TokenType.FALSE);
        keywords.put("null", TokenType.NULL);
        
        // Comandos TNS
        keywords.put("scan", TokenType.SCAN);
        keywords.put("download", TokenType.DOWNLOAD);
        keywords.put("analyze", TokenType.ANALYZE);
        keywords.put("github", TokenType.GITHUB);
        keywords.put("report", TokenType.REPORT);
        keywords.put("sync", TokenType.SYNC);
        keywords.put("share", TokenType.SHARE);
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
            // Operadores simples
            case '(': addToken(TokenType.LPAREN); break;
            case ')': addToken(TokenType.RPAREN); break;
            case '{': addToken(TokenType.LBRACE); break;
            case '}': addToken(TokenType.RBRACE); break;
            case '[': addToken(TokenType.LBRACKET); break;
            case ']': addToken(TokenType.RBRACKET); break;
            case ',': addToken(TokenType.COMMA); break;
            case ';': addToken(TokenType.SEMICOLON); break;
            case ':': addToken(TokenType.COLON); break;
            case '.': addToken(TokenType.DOT); break;
            
            // Operadores compostos
            case '=':
                if (match('=')) addToken(TokenType.EQUALS);
                else addToken(TokenType.ASSIGN);
                break;
            case '!':
                if (match('=')) addToken(TokenType.NOT_EQUALS);
                else addToken(TokenType.NOT);
                break;
            case '<':
                if (match('=')) addToken(TokenType.LESS_EQUAL);
                else addToken(TokenType.LESS);
                break;
            case '>':
                if (match('=')) addToken(TokenType.GREATER_EQUAL);
                else addToken(TokenType.GREATER);
                break;
            case '&':
                if (match('&')) addToken(TokenType.AND);
                break;
            case '|':
                if (match('|')) addToken(TokenType.OR);
                break;
            case '+': addToken(TokenType.PLUS); break;
            case '-': addToken(TokenType.MINUS); break;
            case '*': addToken(TokenType.MULTIPLY); break;
            case '/':
                if (match('/')) {
                    // Comentário de linha
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else if (match('*')) {
                    // Comentário de bloco
                    while (!(peek() == '*' && peekNext() == '/') && !isAtEnd()) {
                        if (peek() == '\n') {
                            line++;
                            column = 1;
                        }
                        advance();
                    }
                    advance(); // *
                    advance(); // /
                } else {
                    addToken(TokenType.DIVIDE);
                }
                break;
            case '%': addToken(TokenType.MODULO); break;
            
            // Espaços
            case ' ':
            case '\r':
            case '\t':
                break;
            case '\n':
                line++;
                column = 1;
                break;
                
            // Strings
            case '"':
                string();
                break;
                
            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    System.err.println("Erro léxico na linha " + line + ", coluna " + column + ": caractere desconhecido '" + c + "'");
                }
                break;
        }
    }
    
    private void identifier() {
        while (isAlphaNumeric(peek())) advance();
        String text = source.substring(start, current);
        TokenType type = keywords.getOrDefault(text, TokenType.IDENTIFIER);
        addToken(type);
    }
    
    private void number() {
        while (isDigit(peek())) advance();
        
        // Ponto decimal
        if (peek() == '.' && isDigit(peekNext())) {
            advance();
            while (isDigit(peek())) advance();
        }
        
        String text = source.substring(start, current);
        try {
            if (text.contains(".")) {
                addToken(TokenType.NUMBER, Double.parseDouble(text));
            } else {
                addToken(TokenType.NUMBER, Integer.parseInt(text));
            }
        } catch (NumberFormatException e) {
            addToken(TokenType.NUMBER, text);
        }
    }
    
    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }
        
        if (isAtEnd()) {
            System.err.println("String não fechada na linha " + line);
            return;
        }
        
        advance(); // Fecha as aspas
        String value = source.substring(start + 1, current - 1);
        addToken(TokenType.STRING, value);
    }
    
    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;
        current++;
        column++;
        return true;
    }
    
    private char advance() {
        char c = source.charAt(current++);
        column++;
        return c;
    }
    
    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }
    
    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }
    
    private boolean isAtEnd() {
        return current >= source.length();
    }
    
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }
    
    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
               (c >= 'A' && c <= 'Z') ||
               c == '_';
    }
    
    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }
    
    private void addToken(TokenType type) {
        addToken(type, null);
    }
    
    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line, column - (current - start)));
    }
}
