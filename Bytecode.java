// Bytecode.java
// Compilador de bytecode e Máquina Virtual do TONACO SCRIPT v2.0
// Criado por: Guilherme Lucas Tonaco Carvalho

package com.tonaco.tns;

import java.util.*;

// ============================================================
// OPCODES
// ============================================================

enum OpCode {
    // Stack
    PUSH_INT, PUSH_FLOAT, PUSH_STRING, PUSH_BOOL, PUSH_NULL,
    PUSH_VAR, POP, DUP,

    // Variáveis
    STORE_VAR, LOAD_VAR,

    // Operações aritméticas
    ADD, SUB, MUL, DIV, MOD,

    // Operações relacionais / lógicas
    EQ, NEQ, LT, GT, LE, GE,
    AND, OR, NOT,

    // Concatenação de strings
    CONCAT,

    // Controle de fluxo
    JMP, JMP_IF_FALSE, JMP_IF_TRUE,

    // Funções
    CALL, RET, MAKE_FRAME, POP_FRAME,

    // Comandos TNS
    TNS_SCAN, TNS_DEEPSCAN, TNS_ANALYZE, TNS_DOWNLOAD,
    TNS_GITHUB, TNS_REPORT, TNS_SYNC,

    // I/O
    PRINT, PRINTLN,

    // Fim
    HALT
}

// ============================================================
// INSTRUÇÃO
// ============================================================

class Instruction {
    public final OpCode opCode;
    public final Object operand;

    public Instruction(OpCode opCode)              { this(opCode, null); }
    public Instruction(OpCode opCode, Object operand) {
        this.opCode = opCode; this.operand = operand;
    }

    @Override public String toString() {
        return operand == null ? opCode.name() : opCode.name() + " " + operand;
    }
}

// ============================================================
// BYTECODE COMPILER
// ============================================================

class BytecodeCompiler implements Visitor<Void> {
    private List<Instruction> code = new ArrayList<>();
    // Mapa de funções: nome → índice de entrada
    private Map<String, Integer> functionAddrs = new HashMap<>();
    // Patches pendentes para CALL (resolvidos após compilar tudo)
    private Map<Integer, String> callPatches = new HashMap<>();

    public List<Instruction> compile(ProgramNode program) {
        // Primeira passagem: registra endereços de funções
        for (ASTNode stmt : program.statements) {
            if (stmt instanceof FunctionNode fn) {
                functionAddrs.put(fn.name, -1); // placeholder
            }
        }
        program.accept(this);
        emit(OpCode.HALT);
        patchCalls();
        return code;
    }

    private void emit(OpCode op)              { code.add(new Instruction(op)); }
    private void emit(OpCode op, Object operand) { code.add(new Instruction(op, operand)); }
    private int here() { return code.size(); }

    private int emitJump(OpCode op) {
        code.add(new Instruction(op, -1)); // placeholder
        return code.size() - 1;
    }

    private void patchJump(int idx) {
        code.set(idx, new Instruction(code.get(idx).opCode, code.size()));
    }

    private void patchCalls() {
        for (var entry : callPatches.entrySet()) {
            int idx = entry.getKey();
            String fnName = entry.getValue();
            Integer addr = functionAddrs.get(fnName);
            if (addr != null && addr >= 0) {
                code.set(idx, new Instruction(OpCode.CALL, addr));
            }
        }
    }

    @Override public Void visitProgramNode(ProgramNode n) {
        for (ASTNode s : n.statements) if (s != null) s.accept(this);
        return null;
    }

    @Override public Void visitBlockNode(BlockNode n) {
        for (ASTNode s : n.statements) if (s != null) s.accept(this);
        return null;
    }

    @Override public Void visitPrintNode(PrintNode n) {
        n.expression.accept(this);
        emit(OpCode.PRINTLN);
        return null;
    }

    @Override public Void visitLetNode(LetNode n) {
        n.value.accept(this);
        emit(OpCode.STORE_VAR, n.name);
        return null;
    }

    @Override public Void visitAssignNode(AssignNode n) {
        n.value.accept(this);
        emit(OpCode.STORE_VAR, n.name);
        return null;
    }

    @Override public Void visitVariableNode(VariableNode n) {
        emit(OpCode.LOAD_VAR, n.name);
        return null;
    }

    @Override public Void visitStringNode(StringNode n)   { emit(OpCode.PUSH_STRING, n.value); return null; }
    @Override public Void visitBooleanNode(BooleanNode n) { emit(OpCode.PUSH_BOOL, n.value);   return null; }
    @Override public Void visitNullNode(NullNode n)       { emit(OpCode.PUSH_NULL);             return null; }

    @Override public Void visitNumberNode(NumberNode n) {
        if (n.value instanceof Double) emit(OpCode.PUSH_FLOAT, n.value);
        else emit(OpCode.PUSH_INT, n.value);
        return null;
    }

    @Override public Void visitBinaryNode(BinaryNode n) {
        n.left.accept(this);
        n.right.accept(this);
        switch (n.operator) {
            case PLUS    -> emit(OpCode.ADD);
            case MINUS   -> emit(OpCode.SUB);
            case MULTIPLY-> emit(OpCode.MUL);
            case DIVIDE  -> emit(OpCode.DIV);
            case MODULO  -> emit(OpCode.MOD);
            case EQUALS  -> emit(OpCode.EQ);
            case NOT_EQUALS -> emit(OpCode.NEQ);
            case LESS    -> emit(OpCode.LT);
            case GREATER -> emit(OpCode.GT);
            case LESS_EQUAL -> emit(OpCode.LE);
            case GREATER_EQUAL -> emit(OpCode.GE);
            case AND     -> emit(OpCode.AND);
            case OR      -> emit(OpCode.OR);
            default      -> {}
        }
        return null;
    }

    @Override public Void visitUnaryNode(UnaryNode n) {
        n.expression.accept(this);
        if (n.operator == TokenType.NOT)   emit(OpCode.NOT);
        if (n.operator == TokenType.MINUS) { emit(OpCode.PUSH_INT, -1); emit(OpCode.MUL); }
        return null;
    }

    @Override public Void visitIfNode(IfNode n) {
        n.condition.accept(this);
        int jumpFalse = emitJump(OpCode.JMP_IF_FALSE);
        n.thenBranch.accept(this);
        if (n.elseBranch != null) {
            int jumpEnd = emitJump(OpCode.JMP);
            patchJump(jumpFalse);
            n.elseBranch.accept(this);
            patchJump(jumpEnd);
        } else {
            patchJump(jumpFalse);
        }
        return null;
    }

    @Override public Void visitWhileNode(WhileNode n) {
        int loopStart = here();
        n.condition.accept(this);
        int jumpEnd = emitJump(OpCode.JMP_IF_FALSE);
        n.body.accept(this);
        emit(OpCode.JMP, loopStart);
        patchJump(jumpEnd);
        return null;
    }

    @Override public Void visitForNode(ForNode n) {
        if (n.init != null) n.init.accept(this);
        int loopStart = here();
        if (n.condition != null) {
            n.condition.accept(this);
            int jumpEnd = emitJump(OpCode.JMP_IF_FALSE);
            n.body.accept(this);
            if (n.increment != null) { n.increment.accept(this); emit(OpCode.POP); }
            emit(OpCode.JMP, loopStart);
            patchJump(jumpEnd);
        } else {
            n.body.accept(this);
            if (n.increment != null) { n.increment.accept(this); emit(OpCode.POP); }
            emit(OpCode.JMP, loopStart);
        }
        return null;
    }

    @Override public Void visitFunctionNode(FunctionNode n) {
        int jumpOver = emitJump(OpCode.JMP); // pula o corpo durante execução linear
        functionAddrs.put(n.name, here());
        emit(OpCode.MAKE_FRAME, n.params);
        n.body.accept(this);
        emit(OpCode.PUSH_NULL);
        emit(OpCode.RET);
        patchJump(jumpOver);
        return null;
    }

    @Override public Void visitCallNode(CallNode n) {
        for (ASTNode arg : n.arguments) arg.accept(this);
        int callIdx = here();
        emit(OpCode.CALL, -1); // será patchado
        callPatches.put(callIdx, n.callee);
        return null;
    }

    @Override public Void visitReturnNode(ReturnNode n) {
        if (n.value != null) n.value.accept(this);
        else emit(OpCode.PUSH_NULL);
        emit(OpCode.RET);
        return null;
    }

    // Comandos TNS
    @Override public Void visitScanNode(ScanNode n)    { n.url.accept(this); emit(OpCode.TNS_SCAN); return null; }
    @Override public Void visitDeepScanNode(DeepScanNode n) {
        n.url.accept(this);
        if (n.depth != null) n.depth.accept(this); else emit(OpCode.PUSH_INT, 2);
        emit(OpCode.TNS_DEEPSCAN);
        return null;
    }
    @Override public Void visitAnalyzeNode(AnalyzeNode n)   { n.url.accept(this); emit(OpCode.TNS_ANALYZE); return null; }
    @Override public Void visitDownloadNode(DownloadNode n) {
        n.url.accept(this);
        if (n.destination != null) n.destination.accept(this); else emit(OpCode.PUSH_STRING, "./downloads");
        emit(OpCode.TNS_DOWNLOAD);
        return null;
    }
    @Override public Void visitGithubNode(GithubNode n) { n.repo.accept(this); emit(OpCode.TNS_GITHUB); return null; }
    @Override public Void visitReportNode(ReportNode n) { emit(OpCode.TNS_REPORT, n.format); return null; }
    @Override public Void visitSyncNode(SyncNode n)     { emit(OpCode.TNS_SYNC); return null; }
}

// ============================================================
// MÁQUINA VIRTUAL
// ============================================================

class VirtualMachine {
    private final Deque<Object> stack = new ArrayDeque<>();
    // Pilha de ambientes (escopos de variáveis)
    private final Deque<Map<String, Object>> envStack = new ArrayDeque<>();
    // Pilha de retorno (endereços de retorno)
    private final Deque<Integer> returnStack = new ArrayDeque<>();
    // Saída capturada (para relatório)
    private final List<String> outputLog = new ArrayList<>();
    private List<Instruction> code;
    private int pc = 0;

    public void execute(List<Instruction> bytecode) {
        this.code = bytecode;
        envStack.push(new HashMap<>());
        boolean running = true;

        while (running && pc < code.size()) {
            Instruction ins = code.get(pc++);

            switch (ins.opCode) {
                // --- Stack ---
                case PUSH_INT    -> push(ins.operand);
                case PUSH_FLOAT  -> push(ins.operand);
                case PUSH_STRING -> push(ins.operand);
                case PUSH_BOOL   -> push(ins.operand);
                case PUSH_NULL   -> push(null);
                case POP         -> pop();
                case DUP         -> push(peek());

                // --- Variáveis ---
                case STORE_VAR -> { String vn = (String) ins.operand; setVar(vn, pop()); }
                case LOAD_VAR  -> {
                    String vn = (String) ins.operand;
                    Object val = getVar(vn);
                    if (val == null && !hasVar(vn)) {
                        System.err.println("[VM] Variável não definida: " + vn);
                    }
                    push(val);
                }

                // --- Aritmética ---
                case ADD -> {
                    Object b = pop(), a = pop();
                    if (a instanceof String || b instanceof String)
                        push(stringify(a) + stringify(b));
                    else push(numericOp(a, b, '+'));
                }
                case SUB -> { Object b = pop(), a = pop(); push(numericOp(a, b, '-')); }
                case MUL -> { Object b = pop(), a = pop(); push(numericOp(a, b, '*')); }
                case DIV -> {
                    Object b = pop(), a = pop();
                    double dv = toDouble(b);
                    if (dv == 0) { System.err.println("[VM] Divisão por zero"); push(0); }
                    else push(numericOp(a, b, '/'));
                }
                case MOD -> { Object b = pop(), a = pop(); push(numericOp(a, b, '%')); }
                case CONCAT -> { Object b = pop(), a = pop(); push(stringify(a) + stringify(b)); }

                // --- Comparações ---
                case EQ  -> { Object b = pop(), a = pop(); push(equals(a, b)); }
                case NEQ -> { Object b = pop(), a = pop(); push(!equals(a, b)); }
                case LT  -> { Object b = pop(), a = pop(); push(compare(a, b) < 0); }
                case GT  -> { Object b = pop(), a = pop(); push(compare(a, b) > 0); }
                case LE  -> { Object b = pop(), a = pop(); push(compare(a, b) <= 0); }
                case GE  -> { Object b = pop(), a = pop(); push(compare(a, b) >= 0); }

                // --- Lógica ---
                case AND -> { Object b = pop(), a = pop(); push(isTruthy(a) && isTruthy(b)); }
                case OR  -> { Object b = pop(), a = pop(); push(isTruthy(a) || isTruthy(b)); }
                case NOT -> push(!isTruthy(pop()));

                // --- Fluxo ---
                case JMP          -> pc = (int) ins.operand;
                case JMP_IF_FALSE -> { if (!isTruthy(pop())) pc = (int) ins.operand; }
                case JMP_IF_TRUE  -> { if (isTruthy(pop()))  pc = (int) ins.operand; }

                // --- Funções ---
                case MAKE_FRAME -> {
                    @SuppressWarnings("unchecked")
                    List<String> params = (List<String>) ins.operand;
                    Map<String, Object> frame = new HashMap<>();
                    // Parâmetros são empilhados em ordem inversa
                    for (int i = params.size() - 1; i >= 0; i--) {
                        frame.put(params.get(i), pop());
                    }
                    envStack.push(frame);
                }
                case CALL -> {
                    returnStack.push(pc);
                    pc = (int) ins.operand;
                }
                case RET -> {
                    Object retVal = pop();
                    if (!envStack.isEmpty()) envStack.pop();
                    pc = returnStack.pop();
                    push(retVal);
                }
                case POP_FRAME -> { if (!envStack.isEmpty()) envStack.pop(); }

                // --- I/O ---
                case PRINT   -> { String s = stringify(pop()); System.out.print(s); outputLog.add(s); }
                case PRINTLN -> { String s = stringify(pop()); System.out.println(s); outputLog.add(s); }

                // --- Comandos TNS ---
                case TNS_SCAN -> {
                    String url = stringify(pop());
                    System.out.println("[TNS SCAN] → " + url);
                }
                case TNS_DEEPSCAN -> {
                    int depth = toInt(pop());
                    String url = stringify(pop());
                    System.out.println("[TNS DEEPSCAN] → " + url + " (profundidade " + depth + ")");
                }
                case TNS_ANALYZE -> {
                    String url = stringify(pop());
                    System.out.println("[TNS ANALYZE] → " + url);
                }
                case TNS_DOWNLOAD -> {
                    String dest = stringify(pop());
                    String url  = stringify(pop());
                    System.out.println("[TNS DOWNLOAD] → " + url + " para " + dest);
                }
                case TNS_GITHUB -> {
                    String repo = stringify(pop());
                    System.out.println("[TNS GITHUB] → " + repo);
                }
                case TNS_REPORT -> {
                    String fmt = (String) ins.operand;
                    System.out.println("[TNS REPORT] Gerando relatório " + fmt.toUpperCase());
                    if (fmt.equals("html")) generateHtmlReport();
                    else generateJsonReport();
                }
                case TNS_SYNC -> System.out.println("[TNS SYNC] Sincronizando comandos da comunidade...");
                case HALT -> running = false;
            }
        }
    }

    // --- Helpers de stack ---
    private void push(Object val) { stack.push(val); }
    private Object pop()          { return stack.isEmpty() ? null : stack.pop(); }
    private Object peek()         { return stack.isEmpty() ? null : stack.peek(); }

    // --- Helpers de variáveis ---
    private void setVar(String name, Object val) { envStack.peek().put(name, val); }
    private Object getVar(String name) {
        for (Map<String, Object> env : envStack) {
            if (env.containsKey(name)) return env.get(name);
        }
        return null;
    }
    private boolean hasVar(String name) {
        for (Map<String, Object> env : envStack) {
            if (env.containsKey(name)) return true;
        }
        return false;
    }

    // --- Aritmética ---
    private Object numericOp(Object a, Object b, char op) {
        double da = toDouble(a), db = toDouble(b);
        double result = switch (op) {
            case '+' -> da + db;
            case '-' -> da - db;
            case '*' -> da * db;
            case '/' -> da / db;
            case '%' -> da % db;
            default  -> 0;
        };
        if (a instanceof Integer && b instanceof Integer && op != '/') return (int) result;
        return result;
    }

    private double toDouble(Object v) {
        if (v instanceof Integer i) return i.doubleValue();
        if (v instanceof Double d)  return d;
        if (v instanceof String s)  try { return Double.parseDouble(s); } catch (Exception e) { return 0; }
        return 0;
    }

    private int toInt(Object v) {
        return (int) toDouble(v);
    }

    private boolean equals(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }

    private int compare(Object a, Object b) {
        return Double.compare(toDouble(a), toDouble(b));
    }

    private boolean isTruthy(Object v) {
        if (v == null)           return false;
        if (v instanceof Boolean bl) return bl;
        if (v instanceof Integer i)  return i != 0;
        if (v instanceof Double d)   return d != 0;
        if (v instanceof String s)   return !s.isEmpty();
        return true;
    }

    private String stringify(Object v) {
        if (v == null)            return "nulo";
        if (v instanceof Boolean) return (Boolean) v ? "verdadeiro" : "falso";
        if (v instanceof Double d) {
            if (d == Math.floor(d) && !Double.isInfinite(d)) return String.valueOf(d.intValue());
            return d.toString();
        }
        return v.toString();
    }

    // --- Geração de relatório ---
    private void generateJsonReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n  \"output\": [\n");
        for (int i = 0; i < outputLog.size(); i++) {
            sb.append("    \"").append(outputLog.get(i).replace("\"", "\\\"")).append("\"");
            if (i < outputLog.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("  ]\n}");
        System.out.println("[REPORT JSON]\n" + sb);
    }

    private void generateHtmlReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset='UTF-8'>");
        sb.append("<title>TONACO SCRIPT Report</title>");
        sb.append("<style>body{font-family:monospace;background:#0d1117;color:#c9d1d9;padding:20px}");
        sb.append("h1{color:#58a6ff}.line{padding:4px 0;border-bottom:1px solid #21262d}</style></head><body>");
        sb.append("<h1>TONACO SCRIPT — Relatório de Execução</h1>");
        for (String line : outputLog) {
            sb.append("<div class='line'>").append(line.replace("<", "&lt;")).append("</div>");
        }
        sb.append("</body></html>");
        System.out.println("[REPORT HTML] " + outputLog.size() + " linhas capturadas.");
    }
}
