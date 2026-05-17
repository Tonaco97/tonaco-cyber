package com.tonaco.tns;
import java.util.*;

enum OpCode {
    PUSH_INT,PUSH_FLOAT,PUSH_STRING,PUSH_BOOL,PUSH_NULL,
    STORE_VAR,LOAD_VAR,POP,DUP,
    ADD,SUB,MUL,DIV,MOD,
    EQ,NEQ,LT,GT,LE,GE,AND,OR,NOT,
    JMP,JMP_IF_FALSE,JMP_IF_TRUE,
    CALL,RET,MAKE_FRAME,POP_FRAME,
    TNS_SCAN,TNS_DEEPSCAN,TNS_ANALYZE,TNS_DOWNLOAD,TNS_GITHUB,TNS_REPORT,TNS_SYNC,
    PRINT,PRINTLN,HALT
}

class Instruction {
    public final OpCode opCode; public final Object operand;
    public Instruction(OpCode o){this(o,null);}
    public Instruction(OpCode o,Object op){opCode=o;operand=op;}
    @Override public String toString(){return operand==null?opCode.name():opCode.name()+" "+operand;}
}

class BytecodeCompiler implements Visitor<Void> {
    private List<Instruction> code=new ArrayList<>();
    private Map<String,Integer> fnAddrs=new HashMap<>();
    private Map<Integer,String> callPatches=new HashMap<>();
    public List<Instruction> compile(ProgramNode p){p.accept(this);emit(OpCode.HALT);patchCalls();return code;}
    private void emit(OpCode o){code.add(new Instruction(o));}
    private void emit(OpCode o,Object v){code.add(new Instruction(o,v));}
    private int here(){return code.size();}
    private int emitJump(OpCode o){code.add(new Instruction(o,-1));return code.size()-1;}
    private void patchJump(int i){code.set(i,new Instruction(code.get(i).opCode,code.size()));}
    private void patchCalls(){for(var e:callPatches.entrySet()){Integer a=fnAddrs.get(e.getValue());if(a!=null&&a>=0)code.set(e.getKey(),new Instruction(OpCode.CALL,a));}}
    @Override public Void visitProgramNode(ProgramNode n){for(ASTNode s:n.statements)if(s!=null)s.accept(this);return null;}
    @Override public Void visitBlockNode(BlockNode n){for(ASTNode s:n.statements)if(s!=null)s.accept(this);return null;}
    @Override public Void visitPrintNode(PrintNode n){n.expression.accept(this);emit(OpCode.PRINTLN);return null;}
    @Override public Void visitLetNode(LetNode n){n.value.accept(this);emit(OpCode.STORE_VAR,n.name);return null;}
    @Override public Void visitAssignNode(AssignNode n){n.value.accept(this);emit(OpCode.STORE_VAR,n.name);return null;}
    @Override public Void visitVariableNode(VariableNode n){emit(OpCode.LOAD_VAR,n.name);return null;}
    @Override public Void visitStringNode(StringNode n){emit(OpCode.PUSH_STRING,n.value);return null;}
    @Override public Void visitBooleanNode(BooleanNode n){emit(OpCode.PUSH_BOOL,n.value);return null;}
    @Override public Void visitNullNode(NullNode n){emit(OpCode.PUSH_NULL);return null;}
    @Override public Void visitNumberNode(NumberNode n){if(n.value instanceof Double)emit(OpCode.PUSH_FLOAT,n.value);else emit(OpCode.PUSH_INT,n.value);return null;}
    @Override public Void visitBinaryNode(BinaryNode n){n.left.accept(this);n.right.accept(this);switch(n.operator){case PLUS->emit(OpCode.ADD);case MINUS->emit(OpCode.SUB);case MULTIPLY->emit(OpCode.MUL);case DIVIDE->emit(OpCode.DIV);case MODULO->emit(OpCode.MOD);case EQUALS->emit(OpCode.EQ);case NOT_EQUALS->emit(OpCode.NEQ);case LESS->emit(OpCode.LT);case GREATER->emit(OpCode.GT);case LESS_EQUAL->emit(OpCode.LE);case GREATER_EQUAL->emit(OpCode.GE);case AND->emit(OpCode.AND);case OR->emit(OpCode.OR);default->{}}return null;}
    @Override public Void visitUnaryNode(UnaryNode n){n.expression.accept(this);if(n.operator==TokenType.NOT)emit(OpCode.NOT);if(n.operator==TokenType.MINUS){emit(OpCode.PUSH_INT,-1);emit(OpCode.MUL);}return null;}
    @Override public Void visitIfNode(IfNode n){n.condition.accept(this);int jf=emitJump(OpCode.JMP_IF_FALSE);n.thenBranch.accept(this);if(n.elseBranch!=null){int je=emitJump(OpCode.JMP);patchJump(jf);n.elseBranch.accept(this);patchJump(je);}else patchJump(jf);return null;}
    @Override public Void visitWhileNode(WhileNode n){int ls=here();n.condition.accept(this);int je=emitJump(OpCode.JMP_IF_FALSE);n.body.accept(this);emit(OpCode.JMP,ls);patchJump(je);return null;}
    @Override public Void visitForNode(ForNode n){if(n.init!=null)n.init.accept(this);int ls=here();if(n.condition!=null){n.condition.accept(this);int je=emitJump(OpCode.JMP_IF_FALSE);n.body.accept(this);if(n.increment!=null){n.increment.accept(this);emit(OpCode.POP);}emit(OpCode.JMP,ls);patchJump(je);}else{n.body.accept(this);if(n.increment!=null){n.increment.accept(this);emit(OpCode.POP);}emit(OpCode.JMP,ls);}return null;}
    @Override public Void visitFunctionNode(FunctionNode n){int jo=emitJump(OpCode.JMP);fnAddrs.put(n.name,here());emit(OpCode.MAKE_FRAME,n.params);n.body.accept(this);emit(OpCode.PUSH_NULL);emit(OpCode.RET);patchJump(jo);return null;}
    @Override public Void visitCallNode(CallNode n){for(ASTNode a:n.arguments)a.accept(this);int ci=here();emit(OpCode.CALL,-1);callPatches.put(ci,n.callee);return null;}
    @Override public Void visitReturnNode(ReturnNode n){if(n.value!=null)n.value.accept(this);else emit(OpCode.PUSH_NULL);emit(OpCode.RET);return null;}
    @Override public Void visitScanNode(ScanNode n){n.url.accept(this);emit(OpCode.TNS_SCAN);return null;}
    @Override public Void visitDeepScanNode(DeepScanNode n){n.url.accept(this);if(n.depth!=null)n.depth.accept(this);else emit(OpCode.PUSH_INT,2);emit(OpCode.TNS_DEEPSCAN);return null;}
    @Override public Void visitAnalyzeNode(AnalyzeNode n){n.url.accept(this);emit(OpCode.TNS_ANALYZE);return null;}
    @Override public Void visitDownloadNode(DownloadNode n){n.url.accept(this);if(n.destination!=null)n.destination.accept(this);else emit(OpCode.PUSH_STRING,"./downloads");emit(OpCode.TNS_DOWNLOAD);return null;}
    @Override public Void visitGithubNode(GithubNode n){n.repo.accept(this);emit(OpCode.TNS_GITHUB);return null;}
    @Override public Void visitReportNode(ReportNode n){emit(OpCode.TNS_REPORT,n.format);return null;}
    @Override public Void visitSyncNode(SyncNode n){emit(OpCode.TNS_SYNC);return null;}
}

class VirtualMachine {
    private final Deque<Object> stack=new ArrayDeque<>();
    private final Deque<Map<String,Object>> envs=new ArrayDeque<>();
    private final Deque<Integer> retStack=new ArrayDeque<>();
    private final List<String> log=new ArrayList<>();
    private List<Instruction> code;
    private int pc=0;
    public void execute(List<Instruction> bc){
        code=bc;envs.push(new HashMap<>());boolean run=true;
        while(run&&pc<code.size()){
            Instruction ins=code.get(pc++);
            switch(ins.opCode){
                case PUSH_INT,PUSH_FLOAT,PUSH_STRING,PUSH_BOOL->push(ins.operand);
                case PUSH_NULL->push(null);
                case POP->pop(); case DUP->push(peek());
                case STORE_VAR->setVar((String)ins.operand,pop());
                case LOAD_VAR->{Object v=getVar((String)ins.operand);push(v);}
                case ADD->{Object b=pop(),a=pop();if(a instanceof String||b instanceof String)push(str(a)+str(b));else push(numOp(a,b,'+'));}
                case SUB->{Object b=pop(),a=pop();push(numOp(a,b,'-'));}
                case MUL->{Object b=pop(),a=pop();push(numOp(a,b,'*'));}
                case DIV->{Object b=pop(),a=pop();double d=dbl(b);push(d==0?0:numOp(a,b,'/'));}
                case MOD->{Object b=pop(),a=pop();push(numOp(a,b,'%'));}
                case EQ->{Object b=pop(),a=pop();push(eq(a,b));}
                case NEQ->{Object b=pop(),a=pop();push(!eq(a,b));}
                case LT->{Object b=pop(),a=pop();push(cmp(a,b)<0);}
                case GT->{Object b=pop(),a=pop();push(cmp(a,b)>0);}
                case LE->{Object b=pop(),a=pop();push(cmp(a,b)<=0);}
                case GE->{Object b=pop(),a=pop();push(cmp(a,b)>=0);}
                case AND->{Object b=pop(),a=pop();push(truthy(a)&&truthy(b));}
                case OR->{Object b=pop(),a=pop();push(truthy(a)||truthy(b));}
                case NOT->push(!truthy(pop()));
                case JMP->pc=(int)ins.operand;
                case JMP_IF_FALSE->{if(!truthy(pop()))pc=(int)ins.operand;}
                case JMP_IF_TRUE->{if(truthy(pop()))pc=(int)ins.operand;}
                case MAKE_FRAME->{
                    @SuppressWarnings("unchecked") List<String> ps=(List<String>)ins.operand;
                    Map<String,Object> frame=new HashMap<>();
                    for(int i=ps.size()-1;i>=0;i--)frame.put(ps.get(i),pop());
                    envs.push(frame);
                }
                case CALL->{retStack.push(pc);pc=(int)ins.operand;}
                case RET->{Object rv=pop();if(!envs.isEmpty())envs.pop();pc=retStack.pop();push(rv);}
                case POP_FRAME->{if(!envs.isEmpty())envs.pop();}
                case PRINTLN->{String s=str(pop());System.out.println(s);log.add(s);}
                case PRINT->{String s=str(pop());System.out.print(s);log.add(s);}
                case TNS_SCAN->System.out.println("[TNS SCAN] -> "+str(pop()));
                case TNS_DEEPSCAN->{int d=(int)dbl(pop());System.out.println("[TNS DEEPSCAN] -> "+str(pop())+" depth="+d);}
                case TNS_ANALYZE->System.out.println("[TNS ANALYZE] -> "+str(pop()));
                case TNS_DOWNLOAD->{String dst=str(pop());System.out.println("[TNS DOWNLOAD] -> "+str(pop())+" => "+dst);}
                case TNS_GITHUB->System.out.println("[TNS GITHUB] -> "+str(pop()));
                case TNS_REPORT->System.out.println("[TNS REPORT] formato="+ins.operand);
                case TNS_SYNC->System.out.println("[TNS SYNC] Sincronizando...");
                case HALT->run=false;
            }
        }
    }
    private void push(Object v){stack.push(v);}
    private Object pop(){return stack.isEmpty()?null:stack.pop();}
    private Object peek(){return stack.isEmpty()?null:stack.peek();}
    private void setVar(String n,Object v){envs.peek().put(n,v);}
    private Object getVar(String n){for(Map<String,Object> e:envs)if(e.containsKey(n))return e.get(n);return null;}
    private Object numOp(Object a,Object b,char op){double da=dbl(a),db=dbl(b);double r;switch(op){case '+'->r=da+db;case '-'->r=da-db;case '*'->r=da*db;case '/'->r=da/db;case '%'->r=da%db;default->r=0;}if(a instanceof Integer&&b instanceof Integer&&op!='/')return(int)r;return r;}
    private double dbl(Object v){if(v instanceof Integer i)return i;if(v instanceof Double d)return d;if(v instanceof String s){try{return Double.parseDouble(s);}catch(Exception e){return 0;}}return 0;}
    private boolean eq(Object a,Object b){if(a==null&&b==null)return true;if(a==null||b==null)return false;return a.equals(b);}
    private int cmp(Object a,Object b){return Double.compare(dbl(a),dbl(b));}
    private boolean truthy(Object v){if(v==null)return false;if(v instanceof Boolean bl)return bl;if(v instanceof Integer i)return i!=0;if(v instanceof Double d)return d!=0;if(v instanceof String s)return!s.isEmpty();return true;}
    private String str(Object v){if(v==null)return"nulo";if(v instanceof Boolean)return(Boolean)v?"verdadeiro":"falso";if(v instanceof Double d){if(d==Math.floor(d)&&!Double.isInfinite(d))return String.valueOf(d.intValue());return d.toString();}return v.toString();}
}
