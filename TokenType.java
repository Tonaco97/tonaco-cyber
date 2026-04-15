// TokenType.java
// Tipos de token da linguagem TONACO SCRIPT v2.0
// Criado por: Guilherme Lucas Tonaco Carvalho

package com.tonaco.tns;

public enum TokenType {
    // Palavras-chave v2
    PRINT, LET, IF, ELSE, WHILE, FOR, FUNCTION, RETURN,
    TRUE, FALSE, NULL,

    // Comandos especiais TNS
    SCAN, DEEPSCAN, DOWNLOAD, ANALYZE, GITHUB, REPORT, SYNC, SHARE,

    // Literais
    IDENTIFIER, STRING, NUMBER,

    // Operadores
    PLUS, MINUS, MULTIPLY, DIVIDE, MODULO,
    ASSIGN, EQUALS, NOT_EQUALS,
    LESS, GREATER, LESS_EQUAL, GREATER_EQUAL,
    AND, OR, NOT,

    // Pontuação
    LPAREN, RPAREN, LBRACE, RBRACE, LBRACKET, RBRACKET,
    COMMA, SEMICOLON, COLON, DOT,

    // Fim / Erro
    EOF, ERROR
}
