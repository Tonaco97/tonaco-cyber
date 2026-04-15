// ASTNodes.java
// Nós da Abstract Syntax Tree do TONACO SCRIPT
// Criado por: Guilherme Lucas Tonaco Carvalho

package com.tonaco.tns;

import java.util.*;

// ============================================================
// BASE
// ============================================================

public interface ASTNode {
    <R> R accept(Visitor<R> visitor);
}

public interface Visitor<R> {
    R visitProgramNode(ProgramNode node);
    R visitPrintNode(PrintNode node);
    R visitLetNode(LetNode node);
    R visitVariableNode(VariableNode node);
    R visitStringNode(StringNode node);
    R visitNumberNode(NumberNode node);
    R visitBooleanNode(BooleanNode node);
    R visitBinaryNode(BinaryNode node);
    R visitUnaryNode(UnaryNode node);
    R visitIfNode(IfNode node);
    R visitWhileNode(WhileNode node);
    R visitBlockNode(BlockNode node);
    R visitScanNode(ScanNode node);
    R visitAnalyzeNode(AnalyzeNode node);
    R visitReportNode(ReportNode node);
}

// ============================================================
// NÓS DE EXPRESSÃO
// ============================================================

public class ProgramNode implements ASTNode {
    public final List<ASTNode> statements;
    
    public ProgramNode(List<ASTNode> statements) {
        this.statements = statements;
    }
    
    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitProgramNode(this);
    }
}

public class PrintNode implements ASTNode {
    public final ASTNode expression;
    
    public PrintNode(ASTNode expression) {
        this.expression = expression;
    }
    
    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitPrintNode(this);
    }
}

public class LetNode implements ASTNode {
    public final String name;
    public final ASTNode value;
    public final String type; // opcional: "int", "string", "bool"
    
    public LetNode(String name, ASTNode value, String type) {
        this.name = name;
        this.value = value;
        this.type = type;
    }
    
    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitLetNode(this);
    }
}

public class VariableNode implements ASTNode {
    public final String name;
    
    public VariableNode(String name) {
        this.name = name;
    }
    
    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitVariableNode(this);
    }
}

public class StringNode implements ASTNode {
    public final String value;
    
    public StringNode(String value) {
        this.value = value;
    }
    
    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitStringNode(this);
    }
}

public class NumberNode implements ASTNode {
    public final Object value; // Integer ou Double
    
    public NumberNode(Object value) {
        this.value = value;
    }
    
    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitNumberNode(this);
    }
}

public class BooleanNode implements ASTNode {
    public final boolean value;
    
    public BooleanNode(boolean value) {
        this.value = value;
    }
    
    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitBooleanNode(this);
    }
}

public class BinaryNode implements ASTNode {
    public final TokenType operator;
    public final ASTNode left;
    public final ASTNode right;
    
    public BinaryNode(TokenType operator, ASTNode left, ASTNode right) {
        this.operator = operator;
        this.left = left;
        this.right = right;
    }
    
    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitBinaryNode(this);
    }
}

public class UnaryNode implements ASTNode {
    public final TokenType operator;
    public final ASTNode expression;
    
    public UnaryNode(TokenType operator, ASTNode expression) {
        this.operator = operator;
        this.expression = expression;
    }
    
    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitUnaryNode(this);
    }
}

public class IfNode implements ASTNode {
    public final ASTNode condition;
    public final ASTNode thenBranch;
    public final ASTNode elseBranch;
    
    public IfNode(ASTNode condition, ASTNode thenBranch, ASTNode elseBranch) {
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }
    
    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitIfNode(this);
    }
}

public class WhileNode implements ASTNode {
    public final ASTNode condition;
    public final ASTNode body;
    
    public WhileNode(ASTNode condition, ASTNode body) {
        this.condition = condition;
        this.body = body;
    }
    
    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitWhileNode(this);
    }
}

public class BlockNode implements ASTNode {
    public final List<ASTNode> statements;
    
    public BlockNode(List<ASTNode> statements) {
        this.statements = statements;
    }
    
    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitBlockNode(this);
    }
}

// ============================================================
// NÓS PARA COMANDOS TNS
// ============================================================

public class ScanNode implements ASTNode {
    public final ASTNode url;
    public final int depth;
    
    public ScanNode(ASTNode url, int depth) {
        this.url = url;
        this.depth = depth;
    }
    
    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitScanNode(this);
    }
}

public class AnalyzeNode implements ASTNode {
    public final ASTNode url;
    
    public AnalyzeNode(ASTNode url) {
        this.url = url;
    }
    
    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitAnalyzeNode(this);
    }
}

public class ReportNode implements ASTNode {
    public final String format;
    
    public ReportNode(String format) {
        this.format = format;
    }
    
    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitReportNode(this);
    }
}
