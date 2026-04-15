// ASTNodes.java
// Nós da Abstract Syntax Tree do TONACO SCRIPT v2.0
// Criado por: Guilherme Lucas Tonaco Carvalho

package com.tonaco.tns;

import java.util.*;

// ============================================================
// INTERFACES BASE
// ============================================================

public interface ASTNode {
    <R> R accept(Visitor<R> visitor);
}

interface Visitor<R> {
    R visitProgramNode(ProgramNode node);
    R visitPrintNode(PrintNode node);
    R visitLetNode(LetNode node);
    R visitAssignNode(AssignNode node);
    R visitVariableNode(VariableNode node);
    R visitStringNode(StringNode node);
    R visitNumberNode(NumberNode node);
    R visitBooleanNode(BooleanNode node);
    R visitNullNode(NullNode node);
    R visitBinaryNode(BinaryNode node);
    R visitUnaryNode(UnaryNode node);
    R visitIfNode(IfNode node);
    R visitWhileNode(WhileNode node);
    R visitForNode(ForNode node);
    R visitBlockNode(BlockNode node);
    R visitFunctionNode(FunctionNode node);
    R visitCallNode(CallNode node);
    R visitReturnNode(ReturnNode node);
    R visitScanNode(ScanNode node);
    R visitDeepScanNode(DeepScanNode node);
    R visitAnalyzeNode(AnalyzeNode node);
    R visitDownloadNode(DownloadNode node);
    R visitGithubNode(GithubNode node);
    R visitReportNode(ReportNode node);
    R visitSyncNode(SyncNode node);
}

// ============================================================
// PROGRAMA
// ============================================================

class ProgramNode implements ASTNode {
    public final List<ASTNode> statements;
    public ProgramNode(List<ASTNode> statements) { this.statements = statements; }
    @Override public <R> R accept(Visitor<R> v) { return v.visitProgramNode(this); }
}

// ============================================================
// CONTROLE
// ============================================================

class PrintNode implements ASTNode {
    public final ASTNode expression;
    public PrintNode(ASTNode expression) { this.expression = expression; }
    @Override public <R> R accept(Visitor<R> v) { return v.visitPrintNode(this); }
}

class LetNode implements ASTNode {
    public final String name;
    public final ASTNode value;
    public final String typeHint; // opcional: "int", "string", "bool"
    public LetNode(String name, ASTNode value, String typeHint) {
        this.name = name; this.value = value; this.typeHint = typeHint;
    }
    @Override public <R> R accept(Visitor<R> v) { return v.visitLetNode(this); }
}

class AssignNode implements ASTNode {
    public final String name;
    public final ASTNode value;
    public AssignNode(String name, ASTNode value) { this.name = name; this.value = value; }
    @Override public <R> R accept(Visitor<R> v) { return v.visitAssignNode(this); }
}

class IfNode implements ASTNode {
    public final ASTNode condition;
    public final ASTNode thenBranch;
    public final ASTNode elseBranch; // pode ser null
    public IfNode(ASTNode condition, ASTNode thenBranch, ASTNode elseBranch) {
        this.condition = condition; this.thenBranch = thenBranch; this.elseBranch = elseBranch;
    }
    @Override public <R> R accept(Visitor<R> v) { return v.visitIfNode(this); }
}

class WhileNode implements ASTNode {
    public final ASTNode condition;
    public final ASTNode body;
    public WhileNode(ASTNode condition, ASTNode body) { this.condition = condition; this.body = body; }
    @Override public <R> R accept(Visitor<R> v) { return v.visitWhileNode(this); }
}

class ForNode implements ASTNode {
    public final ASTNode init;
    public final ASTNode condition;
    public final ASTNode increment;
    public final ASTNode body;
    public ForNode(ASTNode init, ASTNode condition, ASTNode increment, ASTNode body) {
        this.init = init; this.condition = condition;
        this.increment = increment; this.body = body;
    }
    @Override public <R> R accept(Visitor<R> v) { return v.visitForNode(this); }
}

class BlockNode implements ASTNode {
    public final List<ASTNode> statements;
    public BlockNode(List<ASTNode> statements) { this.statements = statements; }
    @Override public <R> R accept(Visitor<R> v) { return v.visitBlockNode(this); }
}

class FunctionNode implements ASTNode {
    public final String name;
    public final List<String> params;
    public final ASTNode body;
    public FunctionNode(String name, List<String> params, ASTNode body) {
        this.name = name; this.params = params; this.body = body;
    }
    @Override public <R> R accept(Visitor<R> v) { return v.visitFunctionNode(this); }
}

class CallNode implements ASTNode {
    public final String callee;
    public final List<ASTNode> arguments;
    public CallNode(String callee, List<ASTNode> arguments) {
        this.callee = callee; this.arguments = arguments;
    }
    @Override public <R> R accept(Visitor<R> v) { return v.visitCallNode(this); }
}

class ReturnNode implements ASTNode {
    public final ASTNode value; // pode ser null
    public ReturnNode(ASTNode value) { this.value = value; }
    @Override public <R> R accept(Visitor<R> v) { return v.visitReturnNode(this); }
}

// ============================================================
// EXPRESSÕES
// ============================================================

class VariableNode implements ASTNode {
    public final String name;
    public VariableNode(String name) { this.name = name; }
    @Override public <R> R accept(Visitor<R> v) { return v.visitVariableNode(this); }
}

class StringNode implements ASTNode {
    public final String value;
    public StringNode(String value) { this.value = value; }
    @Override public <R> R accept(Visitor<R> v) { return v.visitStringNode(this); }
}

class NumberNode implements ASTNode {
    public final Object value; // Integer ou Double
    public NumberNode(Object value) { this.value = value; }
    @Override public <R> R accept(Visitor<R> v) { return v.visitNumberNode(this); }
}

class BooleanNode implements ASTNode {
    public final boolean value;
    public BooleanNode(boolean value) { this.value = value; }
    @Override public <R> R accept(Visitor<R> v) { return v.visitBooleanNode(this); }
}

class NullNode implements ASTNode {
    @Override public <R> R accept(Visitor<R> v) { return v.visitNullNode(this); }
}

class BinaryNode implements ASTNode {
    public final TokenType operator;
    public final ASTNode left;
    public final ASTNode right;
    public BinaryNode(TokenType operator, ASTNode left, ASTNode right) {
        this.operator = operator; this.left = left; this.right = right;
    }
    @Override public <R> R accept(Visitor<R> v) { return v.visitBinaryNode(this); }
}

class UnaryNode implements ASTNode {
    public final TokenType operator;
    public final ASTNode expression;
    public UnaryNode(TokenType operator, ASTNode expression) {
        this.operator = operator; this.expression = expression;
    }
    @Override public <R> R accept(Visitor<R> v) { return v.visitUnaryNode(this); }
}

// ============================================================
// COMANDOS TNS
// ============================================================

class ScanNode implements ASTNode {
    public final ASTNode url;
    public ScanNode(ASTNode url) { this.url = url; }
    @Override public <R> R accept(Visitor<R> v) { return v.visitScanNode(this); }
}

class DeepScanNode implements ASTNode {
    public final ASTNode url;
    public final ASTNode depth; // pode ser null → usa padrão
    public DeepScanNode(ASTNode url, ASTNode depth) { this.url = url; this.depth = depth; }
    @Override public <R> R accept(Visitor<R> v) { return v.visitDeepScanNode(this); }
}

class AnalyzeNode implements ASTNode {
    public final ASTNode url;
    public AnalyzeNode(ASTNode url) { this.url = url; }
    @Override public <R> R accept(Visitor<R> v) { return v.visitAnalyzeNode(this); }
}

class DownloadNode implements ASTNode {
    public final ASTNode url;
    public final ASTNode destination; // pode ser null
    public DownloadNode(ASTNode url, ASTNode destination) {
        this.url = url; this.destination = destination;
    }
    @Override public <R> R accept(Visitor<R> v) { return v.visitDownloadNode(this); }
}

class GithubNode implements ASTNode {
    public final ASTNode repo;
    public GithubNode(ASTNode repo) { this.repo = repo; }
    @Override public <R> R accept(Visitor<R> v) { return v.visitGithubNode(this); }
}

class ReportNode implements ASTNode {
    public final String format; // "json" | "html"
    public ReportNode(String format) { this.format = format; }
    @Override public <R> R accept(Visitor<R> v) { return v.visitReportNode(this); }
}

class SyncNode implements ASTNode {
    @Override public <R> R accept(Visitor<R> v) { return v.visitSyncNode(this); }
}
