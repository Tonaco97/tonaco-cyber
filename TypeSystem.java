// TypeSystem.java
// Sistema de tipos e analisador semântico do TONACO SCRIPT v2.0
// Criado por: Guilherme Lucas Tonaco Carvalho

package com.tonaco.tns;

import java.util.*;

public enum TNSType {
    INTEGER, FLOAT, STRING, BOOLEAN, ARRAY, FUNCTION, NULL, UNKNOWN;

    public static TNSType fromValue(Object value) {
        if (value == null)             return NULL;
        if (value instanceof Integer)  return INTEGER;
        if (value instanceof Double)   return FLOAT;
        if (value instanceof String)   return STRING;
        if (value instanceof Boolean)  return BOOLEAN;
        if (value instanceof List)     return ARRAY;
        return UNKNOWN;
    }

    public static boolean isCompatible(TNSType a, TNSType b) {
        if (a == UNKNOWN || b == UNKNOWN) return true;
        if (a == NULL    || b == NULL)    return true;
        if (a == INTEGER && b == FLOAT)   return true;
        if (a == FLOAT   && b == INTEGER) return true;
        return a == b;
    }

    public static TNSType promote(TNSType a, TNSType b) {
        if (a == INTEGER && b == INTEGER) return INTEGER;
        if ((a == INTEGER || a == FLOAT) && (b == INTEGER || b == FLOAT)) return FLOAT;
        if (a == STRING   || b == STRING) return STRING;
        if (a == UNKNOWN)  return b;
        if (b == UNKNOWN)  return a;
        return UNKNOWN;
    }

    public static TNSType fromHint(String hint) {
        return switch (hint.toLowerCase()) {
            case "int", "inteiro"  -> INTEGER;
            case "float", "decimal"-> FLOAT;
            case "string", "texto" -> STRING;
            case "bool", "booleano"-> BOOLEAN;
            case "array", "lista"  -> ARRAY;
            default -> UNKNOWN;
        };
    }
}

// ============================================================
// Analisador Semântico
// ============================================================

class SemanticAnalyzer implements Visitor<Void> {

    // Escopo em pilha: cada frame é um Map nome→tipo
    private final Deque<Map<String, TNSType>> scopes = new ArrayDeque<>();
    private final List<String> errors   = new ArrayList<>();
    private final List<String> warnings = new ArrayList<>();

    public SemanticAnalyzer() {
        // escopo global
        scopes.push(new HashMap<>());
    }

    public List<String> getErrors()   { return errors; }
    public List<String> getWarnings() { return warnings; }

    public boolean analyze(ProgramNode program) {
        program.accept(this);
        return errors.isEmpty();
    }

    // --- Utilitários de escopo ---
    private void define(String name, TNSType type) {
        scopes.peek().put(name, type);
    }

    private TNSType lookup(String name) {
        for (Map<String, TNSType> scope : scopes) {
            if (scope.containsKey(name)) return scope.get(name);
        }
        return null;
    }

    private void pushScope() { scopes.push(new HashMap<>()); }
    private void popScope()  { scopes.pop(); }

    private void err(String msg)  { errors.add("  ✖ " + msg); }
    private void warn(String msg) { warnings.add("  ⚠ " + msg); }

    // ============================================================
    // VISITORS
    // ============================================================

    @Override public Void visitProgramNode(ProgramNode n) {
        for (ASTNode s : n.statements) if (s != null) s.accept(this);
        return null;
    }

    @Override public Void visitBlockNode(BlockNode n) {
        pushScope();
        for (ASTNode s : n.statements) if (s != null) s.accept(this);
        popScope();
        return null;
    }

    @Override public Void visitPrintNode(PrintNode n) {
        n.expression.accept(this);
        return null;
    }

    @Override public Void visitLetNode(LetNode n) {
        TNSType valueType = inferType(n.value);
        n.value.accept(this);
        if (n.typeHint != null) {
            TNSType declared = TNSType.fromHint(n.typeHint);
            if (!TNSType.isCompatible(declared, valueType)) {
                err("Variável '" + n.name + "' declarada como " + declared + " mas recebe " + valueType);
            }
        }
        if (lookup(n.name) != null && scopes.peek().containsKey(n.name)) {
            warn("Variável '" + n.name + "' redeclarada no mesmo escopo");
        }
        define(n.name, valueType);
        return null;
    }

    @Override public Void visitAssignNode(AssignNode n) {
        TNSType current = lookup(n.name);
        if (current == null) {
            err("Variável '" + n.name + "' não declarada antes de ser atribuída");
        }
        TNSType valueType = inferType(n.value);
        n.value.accept(this);
        if (current != null && !TNSType.isCompatible(current, valueType)) {
            warn("Atribuição incompatível: '" + n.name + "' é " + current + ", recebe " + valueType);
        }
        return null;
    }

    @Override public Void visitVariableNode(VariableNode n) {
        if (lookup(n.name) == null) {
            err("Variável '" + n.name + "' não declarada");
        }
        return null;
    }

    @Override public Void visitBinaryNode(BinaryNode n) {
        n.left.accept(this);
        n.right.accept(this);
        TNSType lt = inferType(n.left), rt = inferType(n.right);
        if (n.operator == TokenType.PLUS) return null; // permite string + qualquer
        if (!TNSType.isCompatible(lt, rt)) {
            warn("Operação " + n.operator + " entre tipos incompatíveis: " + lt + " e " + rt);
        }
        return null;
    }

    @Override public Void visitUnaryNode(UnaryNode n) {
        n.expression.accept(this);
        return null;
    }

    @Override public Void visitIfNode(IfNode n) {
        n.condition.accept(this);
        n.thenBranch.accept(this);
        if (n.elseBranch != null) n.elseBranch.accept(this);
        return null;
    }

    @Override public Void visitWhileNode(WhileNode n) {
        n.condition.accept(this);
        n.body.accept(this);
        return null;
    }

    @Override public Void visitForNode(ForNode n) {
        pushScope();
        if (n.init != null) n.init.accept(this);
        if (n.condition != null) n.condition.accept(this);
        if (n.increment != null) n.increment.accept(this);
        n.body.accept(this);
        popScope();
        return null;
    }

    @Override public Void visitFunctionNode(FunctionNode n) {
        define(n.name, TNSType.FUNCTION);
        pushScope();
        for (String p : n.params) define(p, TNSType.UNKNOWN);
        n.body.accept(this);
        popScope();
        return null;
    }

    @Override public Void visitCallNode(CallNode n) {
        if (lookup(n.callee) == null) {
            err("Função '" + n.callee + "' não declarada");
        }
        for (ASTNode arg : n.arguments) arg.accept(this);
        return null;
    }

    @Override public Void visitReturnNode(ReturnNode n) {
        if (n.value != null) n.value.accept(this);
        return null;
    }

    // Literais — sem verificação necessária
    @Override public Void visitStringNode(StringNode n)   { return null; }
    @Override public Void visitNumberNode(NumberNode n)   { return null; }
    @Override public Void visitBooleanNode(BooleanNode n) { return null; }
    @Override public Void visitNullNode(NullNode n)       { return null; }

    // Comandos TNS
    @Override public Void visitScanNode(ScanNode n)         { n.url.accept(this);  return null; }
    @Override public Void visitDeepScanNode(DeepScanNode n) { n.url.accept(this);  if (n.depth != null) n.depth.accept(this); return null; }
    @Override public Void visitAnalyzeNode(AnalyzeNode n)   { n.url.accept(this);  return null; }
    @Override public Void visitDownloadNode(DownloadNode n) { n.url.accept(this);  if (n.destination != null) n.destination.accept(this); return null; }
    @Override public Void visitGithubNode(GithubNode n)     { n.repo.accept(this); return null; }
    @Override public Void visitReportNode(ReportNode n)     { return null; }
    @Override public Void visitSyncNode(SyncNode n)         { return null; }

    // --- Inferência de tipo ---
    private TNSType inferType(ASTNode n) {
        if (n instanceof StringNode)   return TNSType.STRING;
        if (n instanceof NumberNode num)
            return (num.value instanceof Double) ? TNSType.FLOAT : TNSType.INTEGER;
        if (n instanceof BooleanNode)  return TNSType.BOOLEAN;
        if (n instanceof NullNode)     return TNSType.NULL;
        if (n instanceof VariableNode var) {
            TNSType t = lookup(var.name);
            return t != null ? t : TNSType.UNKNOWN;
        }
        if (n instanceof BinaryNode bin)
            return TNSType.promote(inferType(bin.left), inferType(bin.right));
        return TNSType.UNKNOWN;
    }
}
