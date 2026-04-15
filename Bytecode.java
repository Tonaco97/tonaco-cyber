// Bytecode.java
// Código de bytes e VM do TONACO SCRIPT
// Criado por: Guilherme Lucas Tonaco Carvalho

package com.tonaco.tns;

import java.util.*;

// ============================================================
// OPCODES
// ============================================================

public enum OpCode {
    // Pilha
    PUSH_INT, PUSH_STRING, PUSH_BOOL, PUSH_VAR,
    POP, STORE_VAR, LOAD_VAR,
    
    // Operações
    ADD, SUB, MUL, DIV, MOD,
    EQ, NEQ, LT, GT, LE, GE,
    AND, OR, NOT,
    
    // Controle
    JMP, JMP_IF_FALSE, CALL, RET,
    
    // Comandos TNS
    TNS_SCAN, TNS_ANALYZE, TNS_REPORT, TNS_GITHUB, TNS_SYNC,
    
    // E/S
    PRINT, PRINT_LN,
    
    // Utilitários
    HALT
}

// ============================================================
// INSTRUÇÃO
// ============================================================

public class Instruction {
    public final OpCode opCode;
    public final Object operand;
    
    public Instruction(OpCode opCode) {
        this(opCode, null);
    }
    
    public Instruction(OpCode opCode, Object operand) {
        this.opCode = opCode;
        this.operand = operand;
    }
    
    @Override
    public String toString() {
        if (operand == null) return opCode.toString();
        return opCode + " " + operand;
    }
}

// ============================================================
// BYTECODE COMPILER
// ============================================================

public class BytecodeCompiler implements Visitor<List<Instruction>> {
    private final List<Instruction> code = new ArrayList<>();
    private final Map<String, Integer> varLocations = new HashMap<>();
    private int nextVarSlot = 0;
    
    public List<Instruction> compile(ProgramNode program) {
        code.addAll(program.accept(this));
        code.add(new Instruction(OpCode.HALT));
        return code;
    }
    
    @Override
    public List<Instruction> visitProgramNode(ProgramNode node) {
        List<Instruction> instructions = new ArrayList<>();
        for (ASTNode stmt : node.statements) {
            instructions.addAll(stmt.accept(this));
        }
        return instructions;
    }
    
    @Override
    public List<Instruction> visitPrintNode(PrintNode node) {
        List<Instruction> instructions = new ArrayList<>();
        instructions.addAll(node.expression.accept(this));
        instructions.add(new Instruction(OpCode.PRINT_LN));
        return instructions;
    }
    
    @Override
    public List<Instruction> visitLetNode(LetNode node) {
        List<Instruction> instructions = new ArrayList<>();
        instructions.addAll(node.value.accept(this));
        
        int slot = varLocations.getOrDefault(node.name, nextVarSlot++);
        varLocations.put(node.name, slot);
        instructions.add(new Instruction(OpCode.STORE_VAR, slot));
        
        return instructions;
    }
    
    @Override
    public List<Instruction> visitVariableNode(VariableNode node) {
        List<Instruction> instructions = new ArrayList<>();
        Integer slot = varLocations.get(node.name);
        if (slot == null) {
            slot = nextVarSlot++;
            varLocations.put(node.name, slot);
        }
        instructions.add(new Instruction(OpCode.LOAD_VAR, slot));
        return instructions;
    }
    
    @Override
    public List<Instruction> visitStringNode(StringNode node) {
        return List.of(new Instruction(OpCode.PUSH_STRING, node.value));
    }
    
    @Override
    public List<Instruction> visitNumberNode(NumberNode node) {
        return List.of(new Instruction(OpCode.PUSH_INT, node.value));
    }
    
    @Override
    public List<Instruction> visitBooleanNode(BooleanNode node) {
        return List.of(new Instruction(OpCode.PUSH_BOOL, node.value));
    }
    
    @Override
    public List<Instruction> visitBinaryNode(BinaryNode node) {
        List<Instruction> instructions = new ArrayList<>();
        instructions.addAll(node.left.accept(this));
        instructions.addAll(node.right.accept(this));
        
        OpCode op = toOpCode(node.operator);
        instructions.add(new Instruction(op));
        
        return instructions;
    }
    
    @Override
    public List<Instruction> visitUnaryNode(UnaryNode node) {
        List<Instruction> instructions = node.expression.accept(this);
        if (node.operator == TokenType.NOT) {
            instructions.add(new Instruction(OpCode.NOT));
        } else if (node.operator == TokenType.MINUS) {
            instructions.add(new Instruction(OpCode.PUSH_INT, 0));
            instructions.add(new Instruction(OpCode.SUB));
        }
        return instructions;
    }
    
    @Override
    public List<Instruction> visitIfNode(IfNode node) {
        List<Instruction> instructions = new ArrayList<>();
        
        instructions.addAll(node.condition.accept(this));
        instructions.add(new Instruction(OpCode.JMP_IF_FALSE, null)); // placeholder
        
        int jmpIndex = instructions.size() - 1;
        instructions.addAll(node.thenBranch.accept(this));
        
        if (node.elseBranch != null) {
            instructions.add(new Instruction(OpCode.JMP, null)); // placeholder
            int jmpEndIndex = instructions.size() - 1;
            
            instructions.set(jmpIndex, new Instruction(OpCode.JMP_IF_FALSE, instructions.size()));
            instructions.addAll(node.elseBranch.accept(this));
            instructions.set(jmpEndIndex, new Instruction(OpCode.JMP, instructions.size()));
        } else {
            instructions.set(jmpIndex, new Instruction(OpCode.JMP_IF_FALSE, instructions.size()));
        }
        
        return instructions;
    }
    
    @Override
    public List<Instruction> visitWhileNode(WhileNode node) {
        List<Instruction> instructions = new ArrayList<>();
        
        int loopStart = instructions.size();
        instructions.addAll(node.condition.accept(this));
        instructions.add(new Instruction(OpCode.JMP_IF_FALSE, null)); // placeholder
        
        int jmpIndex = instructions.size() - 1;
        instructions.addAll(node.body.accept(this));
        instructions.add(new Instruction(OpCode.JMP, loopStart));
        
        instructions.set(jmpIndex, new Instruction(OpCode.JMP_IF_FALSE, instructions.size()));
        
        return instructions;
    }
    
    @Override
    public List<Instruction> visitBlockNode(BlockNode node) {
        List<Instruction> instructions = new ArrayList<>();
        for (ASTNode stmt : node.statements) {
            instructions.addAll(stmt.accept(this));
        }
        return instructions;
    }
    
    @Override
    public List<Instruction> visitScanNode(ScanNode node) {
        List<Instruction> instructions = node.url.accept(this);
        instructions.add(new Instruction(OpCode.TNS_SCAN, node.depth));
        return instructions;
    }
    
    @Override
    public List<Instruction> visitAnalyzeNode(AnalyzeNode node) {
        List<Instruction> instructions = node.url.accept(this);
        instructions.add(new Instruction(OpCode.TNS_ANALYZE));
        return instructions;
    }
    
    @Override
    public List<Instruction> visitReportNode(ReportNode node) {
        return List.of(new Instruction(OpCode.TNS_REPORT, node.format));
    }
    
    private OpCode toOpCode(TokenType type) {
        return switch (type) {
            case PLUS -> OpCode.ADD;
            case MINUS -> OpCode.SUB;
            case MULTIPLY -> OpCode.MUL;
            case DIVIDE -> OpCode.DIV;
            case MODULO -> OpCode.MOD;
            case EQUALS -> OpCode.EQ;
            case NOT_EQUALS -> OpCode.NEQ;
            case LESS -> OpCode.LT;
            case GREATER -> OpCode.GT;
            case LESS_EQUAL -> OpCode.LE;
            case GREATER_EQUAL -> OpCode.GE;
            case AND -> OpCode.AND;
            case OR -> OpCode.OR;
            case ASSIGN -> OpCode.STORE_VAR;
            default -> throw new IllegalArgumentException("Unknown operator: " + type);
        };
    }
}

// ============================================================
// VIRTUAL MACHINE
// ============================================================

public class VirtualMachine {
    private final Stack<Object> stack = new Stack<>();
    private final Map<Integer, Object> globals = new HashMap<>();
    private int pc = 0;
    private List<Instruction> code;
    private boolean running = true;
    
    public void execute(List<Instruction> code) {
        this.code = code;
        this.pc = 0;
        this.running = true;
        
        while (running && pc < code.size()) {
            Instruction ins = code.get(pc);
            pc++;
            executeInstruction(ins);
        }
    }
    
    private void executeInstruction(Instruction ins) {
        switch (ins.opCode) {
            case PUSH_INT -> stack.push(ins.operand);
            case PUSH_STRING -> stack.push(ins.operand);
            case PUSH_BOOL -> stack.push(ins.operand);
            case PUSH_VAR -> stack.push(ins.operand);
            
            case POP -> stack.pop();
            case STORE_VAR -> {
                int slot = (int) ins.operand;
                globals.put(slot, stack.pop());
            }
            case LOAD_VAR -> {
                int slot = (int) ins.operand;
                stack.push(globals.get(slot));
            }
            
            case ADD -> {
                Object b = stack.pop();
                Object a = stack.pop();
                if (a instanceof Number && b instanceof Number) {
                    double val = ((Number)a).doubleValue() + ((Number)b).doubleValue();
                    stack.push((int)val);
                } else {
                    stack.push(a.toString() + b.toString());
                }
            }
            case SUB -> {
                int b = ((Number)stack.pop()).intValue();
                int a = ((Number)stack.pop()).intValue();
                stack.push(a - b);
            }
            case MUL -> {
                int b = ((Number)stack.pop()).intValue();
                int a = ((Number)stack.pop()).intValue();
                stack.push(a * b);
            }
            case DIV -> {
                int b = ((Number)stack.pop()).intValue();
                int a = ((Number)stack.pop()).intValue();
                stack.push(a / b);
            }
            case MOD -> {
                int b = ((Number)stack.pop()).intValue();
                int a = ((Number)stack.pop()).intValue();
                stack.push(a % b);
            }
            
            case EQ -> {
                Object b = stack.pop();
                Object a = stack.pop();
                stack.push(a.equals(b));
            }
            case NEQ -> {
                Object b = stack.pop();
                Object a = stack.pop();
                stack.push(!a.equals(b));
            }
            case LT -> {
                int b = ((Number)stack.pop()).intValue();
                int a = ((Number)stack.pop()).intValue();
                stack.push(a < b);
            }
            case GT -> {
                int b = ((Number)stack.pop()).intValue();
                int a = ((Number)stack.pop()).intValue();
                stack.push(a > b);
            }
            case LE -> {
                int b = ((Number)stack.pop()).intValue();
                int a = ((Number)stack.pop()).intValue();
                stack.push(a <= b);
            }
            case GE -> {
                int b = ((Number)stack.pop()).intValue();
                int a = ((Number)stack.pop()).intValue();
                stack.push(a >= b);
            }
            
            case AND -> {
                boolean b = (boolean) stack.pop();
                boolean a = (boolean) stack.pop();
                stack.push(a && b);
            }
            case OR -> {
                boolean b = (boolean) stack.pop();
                boolean a = (boolean) stack.pop();
                stack.push(a || b);
            }
            case NOT -> {
                boolean a = (boolean) stack.pop();
                stack.push(!a);
            }
            
            case JMP -> pc = (int) ins.operand;
            case JMP_IF_FALSE -> {
                boolean cond = (boolean) stack.pop();
                if (!cond) pc = (int) ins.operand;
            }
            
            case PRINT -> System.out.print(stack.pop());
            case PRINT_LN -> System.out.println(stack.pop());
            
            case TNS_SCAN -> {
                String url = (String) stack.pop();
                int depth = (int) ins.operand;
                System.out.println("[TNS] Escaneando " + url + " (profundidade " + depth + ")");
                // Aqui chamaria o scanner real
            }
            case TNS_ANALYZE -> {
                String url = (String) stack.pop();
                System.out.println("[TNS] Analisando " + url);
            }
            case TNS_REPORT -> {
                String format = (String) ins.operand;
                System.out.println("[TNS] Gerando relatório " + format);
            }
            case TNS_GITHUB -> System.out.println("[TNS] Integrando GitHub");
            case TNS_SYNC -> System.out.println("[TNS] Sincronizando comandos");
            
            case HALT -> running = false;
        }
    }
}
