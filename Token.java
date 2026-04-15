// Token.java
// Tokens da linguagem TONACO SCRIPT
// Criado por: Guilherme Lucas Tonaco Carvalho

package com.tonaco.tns;

public enum TokenType {
    // Palavras-chave
    PRINT, LET, IF, ELSE, WHILE, FOR, FUNCTION, RETURN,
    TRUE, FALSE, NULL,
    
    // Comandos especiais TNS
    SCAN, DOWNLOAD, ANALYZE, GITHUB, REPORT, SYNC, SHARE,
    
    // Literais
    IDENTIFIER, STRING, NUMBER, 
    
    // Operadores
    PLUS, MINUS, MULTIPLY, DIVIDE, MODULO,
    ASSIGN, EQUALS, NOT_EQUALS, LESS, GREATER,
    LESS_EQUAL, GREATER_EQUAL, AND, OR, NOT,
    
    // Pontuação
    LPAREN, RPAREN, LBRACE, RBRACE, LBRACKET, RBRACKET,
    COMMA, SEMICOLON, COLON, DOT,
    
    // Fim
    EOF, ERROR
}

public class Token {
    public final TokenType type;
    public final String lexeme;
    public final Object literal;
    public final int line;
    public final int column;
    
    public Token(TokenType type, String lexeme, Object literal, int line, int column) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
        this.column = column;
    }
    
    @Override
    public String toString() {
        return type + "(" + lexeme + ")";
    }
}
