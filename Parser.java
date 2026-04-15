// Parser.java
// Analisador sintático do TONACO SCRIPT v2.0
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
            ASTNode stmt = declaration();
            if (stmt != null) statements.add(stmt);
        }
        return new ProgramNode(statements);
    }

    // ============================================================
    // DECLARAÇÕES
    // ============================================================

    private ASTNode declaration() {
        try {
            if (match(TokenType.FUNCTION)) return functionDeclaration();
            return statement();
        } catch (RuntimeException e) {
            synchronize();
            return null;
        }
    }

    private FunctionNode functionDeclaration() {
        Token name = consume(TokenType.IDENTIFIER, "Esperado nome da função");
        consume(TokenType.LPAREN, "Esperado '(' após nome da função");
        List<String> params = new ArrayList<>();
        if (!check(TokenType.RPAREN)) {
            do {
                params.add(consume(TokenType.IDENTIFIER, "Esperado nome do parâmetro").lexeme);
            } while (match(TokenType.COMMA));
        }
        consume(TokenType.RPAREN, "Esperado ')'");
        consume(TokenType.LBRACE, "Esperado '{' antes do corpo da função");
        ASTNode body = blockStatement();
        return new FunctionNode(name.lexeme, params, body);
    }

    // ============================================================
    // STATEMENTS
    // ============================================================

    private ASTNode statement() {
        if (match(TokenType.PRINT))    return printStatement();
        if (match(TokenType.LET))      return letStatement();
        if (match(TokenType.IF))       return ifStatement();
        if (match(TokenType.WHILE))    return whileStatement();
        if (match(TokenType.FOR))      return forStatement();
        if (match(TokenType.RETURN))   return returnStatement();
        if (match(TokenType.LBRACE))   return blockStatement();

        // Comandos TNS
        if (match(TokenType.SCAN))     return scanStatement();
        if (match(TokenType.DEEPSCAN)) return deepScanStatement();
        if (match(TokenType.ANALYZE))  return analyzeStatement();
        if (match(TokenType.DOWNLOAD)) return downloadStatement();
        if (match(TokenType.GITHUB))   return githubStatement();
        if (match(TokenType.REPORT))   return reportStatement();
        if (match(TokenType.SYNC))     { consumeOptionalSemicolon(); return new SyncNode(); }

        return expressionStatement();
    }

    private PrintNode printStatement() {
        ASTNode expr = expression();
        consumeOptionalSemicolon();
        return new PrintNode(expr);
    }

    private LetNode letStatement() {
        Token name = consume(TokenType.IDENTIFIER, "Esperado nome da variável");
        String typeHint = null;
        if (match(TokenType.COLON)) {
            typeHint = consume(TokenType.IDENTIFIER, "Esperado tipo após ':'").lexeme;
        }
        consume(TokenType.ASSIGN, "Esperado '=' após nome da variável");
        ASTNode value = expression();
        consumeOptionalSemicolon();
        return new LetNode(name.lexeme, value, typeHint);
    }

    private IfNode ifStatement() {
        consume(TokenType.LPAREN, "Esperado '(' após 'if'");
        ASTNode condition = expression();
        consume(TokenType.RPAREN, "Esperado ')'");
        ASTNode thenBranch = statement();
        ASTNode elseBranch = null;
        if (match(TokenType.ELSE)) elseBranch = statement();
        return new IfNode(condition, thenBranch, elseBranch);
    }

    private WhileNode whileStatement() {
        consume(TokenType.LPAREN, "Esperado '(' após 'while'");
        ASTNode condition = expression();
        consume(TokenType.RPAREN, "Esperado ')'");
        ASTNode body = statement();
        return new WhileNode(condition, body);
    }

    private ForNode forStatement() {
        consume(TokenType.LPAREN, "Esperado '(' após 'for'");
        ASTNode init = null;
        if (!check(TokenType.SEMICOLON)) {
            if (match(TokenType.LET)) init = letStatement();
            else init = expressionStatement();
        } else consume(TokenType.SEMICOLON, "");

        ASTNode condition = check(TokenType.SEMICOLON) ? new BooleanNode(true) : expression();
        consume(TokenType.SEMICOLON, "Esperado ';' após condição do for");

        ASTNode increment = null;
        if (!check(TokenType.RPAREN)) increment = expression();
        consume(TokenType.RPAREN, "Esperado ')'");

        ASTNode body = statement();
        return new ForNode(init, condition, increment, body);
    }

    private ReturnNode returnStatement() {
        ASTNode value = null;
        if (!check(TokenType.SEMICOLON) && !isAtEnd()) value = expression();
        consumeOptionalSemicolon();
        return new ReturnNode(value);
    }

    private BlockNode blockStatement() {
        List<ASTNode> stmts = new ArrayList<>();
        while (!check(TokenType.RBRACE) && !isAtEnd()) {
            ASTNode d = declaration();
            if (d != null) stmts.add(d);
        }
        consume(TokenType.RBRACE, "Esperado '}'");
        return new BlockNode(stmts);
    }

    // --- Comandos TNS ---

    private ScanNode scanStatement() {
        ASTNode url = expression();
        consumeOptionalSemicolon();
        return new ScanNode(url);
    }

    private DeepScanNode deepScanStatement() {
        ASTNode url = expression();
        ASTNode depth = null;
        if (match(TokenType.COMMA)) depth = expression();
        consumeOptionalSemicolon();
        return new DeepScanNode(url, depth);
    }

    private AnalyzeNode analyzeStatement() {
        ASTNode url = expression();
        consumeOptionalSemicolon();
        return new AnalyzeNode(url);
    }

    private DownloadNode downloadStatement() {
        ASTNode url = expression();
        ASTNode dest = null;
        if (match(TokenType.COMMA)) dest = expression();
        consumeOptionalSemicolon();
        return new DownloadNode(url, dest);
    }

    private GithubNode githubStatement() {
        ASTNode repo = expression();
        consumeOptionalSemicolon();
        return new GithubNode(repo);
    }

    private ReportNode reportStatement() {
        Token fmt = consume(TokenType.IDENTIFIER, "Esperado formato: json ou html");
        consumeOptionalSemicolon();
        return new ReportNode(fmt.lexeme.toLowerCase());
    }

    private ASTNode expressionStatement() {
        ASTNode expr = expression();
        consumeOptionalSemicolon();
        return expr;
    }

    // ============================================================
    // EXPRESSÕES
    // ============================================================

    private ASTNode expression() { return assignment(); }

    private ASTNode assignment() {
        ASTNode expr = logicalOr();
        if (match(TokenType.ASSIGN)) {
            ASTNode value = assignment();
            if (expr instanceof VariableNode var) return new AssignNode(var.name, value);
            throw error("Lado esquerdo da atribuição inválido");
        }
        return expr;
    }

    private ASTNode logicalOr() {
        ASTNode expr = logicalAnd();
        while (match(TokenType.OR)) {
            TokenType op = previous().type;
            expr = new BinaryNode(op, expr, logicalAnd());
        }
        return expr;
    }

    private ASTNode logicalAnd() {
        ASTNode expr = equality();
        while (match(TokenType.AND)) {
            TokenType op = previous().type;
            expr = new BinaryNode(op, expr, equality());
        }
        return expr;
    }

    private ASTNode equality() {
        ASTNode expr = comparison();
        while (match(TokenType.EQUALS, TokenType.NOT_EQUALS)) {
            expr = new BinaryNode(previous().type, expr, comparison());
        }
        return expr;
    }

    private ASTNode comparison() {
        ASTNode expr = addition();
        while (match(TokenType.LESS, TokenType.GREATER, TokenType.LESS_EQUAL, TokenType.GREATER_EQUAL)) {
            expr = new BinaryNode(previous().type, expr, addition());
        }
        return expr;
    }

    private ASTNode addition() {
        ASTNode expr = multiplication();
        while (match(TokenType.PLUS, TokenType.MINUS)) {
            expr = new BinaryNode(previous().type, expr, multiplication());
        }
        return expr;
    }

    private ASTNode multiplication() {
        ASTNode expr = unary();
        while (match(TokenType.MULTIPLY, TokenType.DIVIDE, TokenType.MODULO)) {
            expr = new BinaryNode(previous().type, expr, unary());
        }
        return expr;
    }

    private ASTNode unary() {
        if (match(TokenType.NOT, TokenType.MINUS)) {
            return new UnaryNode(previous().type, unary());
        }
        return call();
    }

    private ASTNode call() {
        ASTNode expr = primary();
        if (expr instanceof VariableNode var && match(TokenType.LPAREN)) {
            List<ASTNode> args = new ArrayList<>();
            if (!check(TokenType.RPAREN)) {
                do { args.add(expression()); } while (match(TokenType.COMMA));
            }
            consume(TokenType.RPAREN, "Esperado ')' após argumentos");
            return new CallNode(var.name, args);
        }
        return expr;
    }

    private ASTNode primary() {
        if (match(TokenType.NUMBER))  return new NumberNode(previous().literal);
        if (match(TokenType.STRING))  return new StringNode((String) previous().literal);
        if (match(TokenType.TRUE))    return new BooleanNode(true);
        if (match(TokenType.FALSE))   return new BooleanNode(false);
        if (match(TokenType.NULL))    return new NullNode();
        if (match(TokenType.IDENTIFIER)) return new VariableNode(previous().lexeme);
        if (match(TokenType.LPAREN)) {
            ASTNode expr = expression();
            consume(TokenType.RPAREN, "Esperado ')'");
            return expr;
        }
        throw error("Expressão inválida — token inesperado: " + peek().lexeme);
    }

    // ============================================================
    // HELPERS
    // ============================================================

    private boolean match(TokenType... types) {
        for (TokenType t : types) {
            if (check(t)) { advance(); return true; }
        }
        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        throw error(message);
    }

    private void consumeOptionalSemicolon() {
        match(TokenType.SEMICOLON); // ponto-e-vírgula é opcional em v1
    }

    private boolean check(TokenType type) {
        return !isAtEnd() && peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd()  { return peek().type == TokenType.EOF; }
    private Token peek()       { return tokens.get(current); }
    private Token previous()   { return tokens.get(current - 1); }

    private RuntimeException error(String message) {
        Token t = peek();
        String msg = String.format("[Parser] Linha %d, col %d: %s", t.line, t.column, message);
        System.err.println(msg);
        return new RuntimeException(msg);
    }

    private void synchronize() {
        advance();
        while (!isAtEnd()) {
            if (previous().type == TokenType.SEMICOLON) return;
            switch (peek().type) {
                case FUNCTION, LET, IF, WHILE, FOR, PRINT, RETURN -> { return; }
                default -> advance();
            }
        }
    }
}
