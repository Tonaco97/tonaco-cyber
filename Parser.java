// Parser.java
// Analisador sintático do TONACO SCRIPT
// Criado por: Guilherme Lucas Tonaco Carvalho

package com.tonaco.tns;

import java.util.*;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;
    
    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }
    
    public ProgramNode parse() {
        List<ASTNode> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(statement());
        }
        return new ProgramNode(statements);
    }
    
    private ASTNode statement() {
        if (match(TokenType.PRINT)) {
            return printStatement();
        }
        if (match(TokenType.LET)) {
            return letStatement();
        }
        if (match(TokenType.IF)) {
            return ifStatement();
        }
        if (match(TokenType.WHILE)) {
            return whileStatement();
        }
        if (match(TokenType.LBRACE)) {
            return blockStatement();
        }
        if (match(TokenType.SCAN)) {
            return scanStatement();
        }
        if (match(TokenType.ANALYZE)) {
            return analyzeStatement();
        }
        if (match(TokenType.REPORT)) {
            return reportStatement();
        }
        return expressionStatement();
    }
    
    private PrintNode printStatement() {
        ASTNode expr = expression();
        consume(TokenType.SEMICOLON, "Esperado ';' após expressão");
        return new PrintNode(expr);
    }
    
    private LetNode letStatement() {
        Token nameToken = consume(TokenType.IDENTIFIER, "Esperado nome da variável");
        String type = null;
        
        if (match(TokenType.COLON)) {
            if (match(TokenType.IDENTIFIER)) {
                type = previous().lexeme;
            }
        }
        
        consume(TokenType.ASSIGN, "Esperado '='");
        ASTNode value = expression();
        consume(TokenType.SEMICOLON, "Esperado ';'");
        
        return new LetNode(nameToken.lexeme, value, type);
    }
    
    private IfNode ifStatement() {
        consume(TokenType.LPAREN, "Esperado '(' após 'if'");
        ASTNode condition = expression();
        consume(TokenType.RPAREN, "Esperado ')'");
        
        ASTNode thenBranch = statement();
        ASTNode elseBranch = null;
        
        if (match(TokenType.ELSE)) {
            elseBranch = statement();
        }
        
        return new IfNode(condition, thenBranch, elseBranch);
    }
    
    private WhileNode whileStatement() {
        consume(TokenType.LPAREN, "Esperado '(' após 'while'");
        ASTNode condition = expression();
        consume(TokenType.RPAREN, "Esperado ')'");
        
        ASTNode body = statement();
        return new WhileNode(condition, body);
    }
    
    private BlockNode blockStatement() {
        List<ASTNode> statements = new ArrayList<>();
        while (!check(TokenType.RBRACE) && !isAtEnd()) {
            statements.add(statement());
        }
        consume(TokenType.RBRACE, "Esperado '}'");
        return new BlockNode(statements);
    }
    
    private ScanNode scanStatement() {
        ASTNode url = expression();
        int depth = 2; // padrão
        
        if (match(TokenType.COMMA)) {
            if (match(TokenType.NUMBER)) {
                depth = ((Number)previous().literal).intValue();
            }
        }
        consume(TokenType.SEMICOLON, "Esperado ';'");
        return new ScanNode(url, depth);
    }
    
    private AnalyzeNode analyzeStatement() {
        ASTNode url = expression();
        consume(TokenType.SEMICOLON, "Esperado ';'");
        return new AnalyzeNode(url);
    }
    
    private ReportNode reportStatement() {
        Token format = consume(TokenType.IDENTIFIER, "Esperado formato (json/html)");
        consume(TokenType.SEMICOLON, "Esperado ';'");
        return new ReportNode(format.lexeme);
    }
    
    private ASTNode expressionStatement() {
        ASTNode expr = expression();
        consume(TokenType.SEMICOLON, "Esperado ';'");
        return expr;
    }
    
    private ASTNode expression() {
        return assignment();
    }
    
    private ASTNode assignment() {
        ASTNode expr = logicalOr();
        
        if (match(TokenType.ASSIGN)) {
            if (expr instanceof VariableNode) {
                VariableNode var = (VariableNode) expr;
                ASTNode value = assignment();
                // Retorna uma estrutura de atribuição
                return new BinaryNode(TokenType.ASSIGN, var, value);
            }
            error("Lado esquerdo da atribuição deve ser uma variável");
        }
        
        return expr;
    }
    
    private ASTNode logicalOr() {
        ASTNode expr = logicalAnd();
        
        while (match(TokenType.OR)) {
            TokenType op = previous().type;
            ASTNode right = logicalAnd();
            expr = new BinaryNode(op, expr, right);
        }
        
        return expr;
    }
    
    private ASTNode logicalAnd() {
        ASTNode expr = equality();
        
        while (match(TokenType.AND)) {
            TokenType op = previous().type;
            ASTNode right = equality();
            expr = new BinaryNode(op, expr, right);
        }
        
        return expr;
    }
    
    private ASTNode equality() {
        ASTNode expr = comparison();
        
        while (match(TokenType.EQUALS, TokenType.NOT_EQUALS)) {
            TokenType op = previous().type;
            ASTNode right = comparison();
            expr = new BinaryNode(op, expr, right);
        }
        
        return expr;
    }
    
    private ASTNode comparison() {
        ASTNode expr = addition();
        
        while (match(TokenType.LESS, TokenType.GREATER, TokenType.LESS_EQUAL, TokenType.GREATER_EQUAL)) {
            TokenType op = previous().type;
            ASTNode right = addition();
            expr = new BinaryNode(op, expr, right);
        }
        
        return expr;
    }
    
    private ASTNode addition() {
        ASTNode expr = multiplication();
        
        while (match(TokenType.PLUS, TokenType.MINUS)) {
            TokenType op = previous().type;
            ASTNode right = multiplication();
            expr = new BinaryNode(op, expr, right);
        }
        
        return expr;
    }
    
    private ASTNode multiplication() {
        ASTNode expr = unary();
        
        while (match(TokenType.MULTIPLY, TokenType.DIVIDE, TokenType.MODULO)) {
            TokenType op = previous().type;
            ASTNode right = unary();
            expr = new BinaryNode(op, expr, right);
        }
        
        return expr;
    }
    
    private ASTNode unary() {
        if (match(TokenType.NOT, TokenType.MINUS)) {
            TokenType op = previous().type;
            ASTNode expr = unary();
            return new UnaryNode(op, expr);
        }
        return primary();
    }
    
    private ASTNode primary() {
        if (match(TokenType.NUMBER)) {
            return new NumberNode(previous().literal);
        }
        if (match(TokenType.STRING)) {
            return new StringNode((String)previous().literal);
        }
        if (match(TokenType.TRUE)) {
            return new BooleanNode(true);
        }
        if (match(TokenType.FALSE)) {
            return new BooleanNode(false);
        }
        if (match(TokenType.IDENTIFIER)) {
            return new VariableNode(previous().lexeme);
        }
        if (match(TokenType.LPAREN)) {
            ASTNode expr = expression();
            consume(TokenType.RPAREN, "Esperado ')'");
            return expr;
        }
        
        throw error("Expressão inválida");
    }
    
    // ============================================================
    // MÉTODOS AUXILIARES
    // ============================================================
    
    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }
    
    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        throw error(message);
    }
    
    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }
    
    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }
    
    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }
    
    private Token peek() {
        return tokens.get(current);
    }
    
    private Token previous() {
        return tokens.get(current - 1);
    }
    
    private RuntimeException error(String message) {
        Token token = peek();
        System.err.println("Erro sintático na linha " + token.line + ", coluna " + token.column + ": " + message);
        return new RuntimeException(message);
    }
}
