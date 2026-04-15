// TypeSystem.java
// Sistema de tipos do TONACO SCRIPT
// Criado por: Guilherme Lucas Tonaco Carvalho

package com.tonaco.tns;

import java.util.*;

public enum TNSDataType {
    INTEGER, STRING, BOOLEAN, ARRAY, NULL, UNKNOWN;
    
    public static TNSDataType fromValue(Object value) {
        if (value == null) return NULL;
        if (value instanceof Integer) return INTEGER;
        if (value instanceof Double) return INTEGER;
        if (value instanceof String) return STRING;
        if (value instanceof Boolean) return BOOLEAN;
        if (value instanceof List) return ARRAY;
        return UNKNOWN;
    }
    
    public static boolean isCompatible(TNSDataType a, TNSDataType b) {
        if (a == UNKNOWN || b == UNKNOWN) return true;
        if (a == NULL || b == NULL) return true;
        return a == b;
    }
    
    public static TNSDataType promote(TNSDataType a, TNSDataType b) {
        if (a == INTEGER && b == INTEGER) return INTEGER;
        if (a == STRING || b == STRING) return STRING;
        if (a == UNKNOWN) return b;
        if (b == UNKNOWN) return a;
        return UNKNOWN;
    }
}

// ============================================================
// SemanticAnalyzer.java
// ============================================================

public class SemanticAnalyzer implements Visitor<Void> {
    private final Map<String, TNSDataType> symbolTable = new HashMap<>();
    private final List<String> errors = new ArrayList<>();
    
    public List<String> analyze(ProgramNode program) {
        program.accept(this);
        return errors;
    }
    
    @Override
    public Void visitProgramNode(ProgramNode node) {
        for (ASTNode stmt : node.statements) {
            stmt.accept(this);
        }
        return null;
    }
    
    @Override
    public Void visitPrintNode(PrintNode node) {
        node.expression.accept(this);
        return null;
    }
    
    @Override
    public Void visitLetNode(LetNode node) {
        TNSDataType valueType = getExpressionType(node.value);
        
        if (node.type != null) {
            TNSDataType declaredType = parseType(node.type);
            if (!TNSDataType.isCompatible(declaredType, valueType)) {
                errors.add("Erro de tipo: variável '" + node.name + 
                           "' declarada como " + declaredType + 
                           " mas recebe " + valueType);
            }
        }
        
        symbolTable.put(node.name, valueType);
        return null;
    }
    
    @Override
    public Void visitVariableNode(VariableNode node) {
        if (!symbolTable.containsKey(node.name)) {
            errors.add("Erro: variável '" + node.name + "' não declarada");
        }
        return null;
    }
    
    @Override
    public Void visitStringNode(StringNode node) {
        return null;
    }
    
    @Override
    public Void visitNumberNode(NumberNode node) {
        return null;
    }
    
    @Override
    public Void visitBooleanNode(BooleanNode node) {
        return null;
    }
    
    @Override
    public Void visitBinaryNode(BinaryNode node) {
        node.left.accept(this);
        node.right.accept(this);
        
        TNSDataType leftType = getExpressionType(node.left);
        TNSDataType rightType = getExpressionType(node.right);
        
        if (node.operator == TokenType.ASSIGN) {
            if (!(node.left instanceof VariableNode)) {
                errors.add("Erro: lado esquerdo da atribuição deve ser variável");
            }
        }
        
        if (!TNSDataType.isCompatible(leftType, rightType)) {
            errors.add("Erro de tipo na operação " + node.operator + 
                       ": " + leftType + " vs " + rightType);
        }
        
        return null;
    }
    
    @Override
    public Void visitUnaryNode(UnaryNode node) {
        node.expression.accept(this);
        return null;
    }
    
    @Override
    public Void visitIfNode(IfNode node) {
        node.condition.accept(this);
        node.thenBranch.accept(this);
        if (node.elseBranch != null) {
            node.elseBranch.accept(this);
        }
        return null;
    }
    
    @Override
    public Void visitWhileNode(WhileNode node) {
        node.condition.accept(this);
        node.body.accept(this);
        return null;
    }
    
    @Override
    public Void visitBlockNode(BlockNode node) {
        Map<String, TNSDataType> previousScope = new HashMap<>(symbolTable);
        for (ASTNode stmt : node.statements) {
            stmt.accept(this);
        }
        symbolTable.clear();
        symbolTable.putAll(previousScope);
        return null;
    }
    
    @Override
    public Void visitScanNode(ScanNode node) {
        node.url.accept(this);
        return null;
    }
    
    @Override
    public Void visitAnalyzeNode(AnalyzeNode node) {
        node.url.accept(this);
        return null;
    }
    
    @Override
    public Void visitReportNode(ReportNode node) {
        return null;
    }
    
    private TNSDataType getExpressionType(ASTNode node) {
        if (node instanceof StringNode) return TNSDataType.STRING;
        if (node instanceof NumberNode) return TNSDataType.INTEGER;
        if (node instanceof BooleanNode) return TNSDataType.BOOLEAN;
        if (node instanceof VariableNode) {
            return symbolTable.getOrDefault(((VariableNode)node).name, TNSDataType.UNKNOWN);
        }
        return TNSDataType.UNKNOWN;
    }
    
    private TNSDataType parseType(String typeName) {
        switch (typeName.toLowerCase()) {
            case "int": return TNSDataType.INTEGER;
            case "string": return TNSDataType.STRING;
            case "bool": return TNSDataType.BOOLEAN;
            default: return TNSDataType.UNKNOWN;
        }
    }
}
