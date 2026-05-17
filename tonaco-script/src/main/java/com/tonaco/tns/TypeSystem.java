package com.tonaco.tns;
import java.util.*;

enum TNSType {
    INTEGER,FLOAT,STRING,BOOLEAN,ARRAY,FUNCTION,NULL,UNKNOWN;
    public static TNSType fromValue(Object v){if(v==null)return NULL;if(v instanceof Integer)return INTEGER;if(v instanceof Double)return FLOAT;if(v instanceof String)return STRING;if(v instanceof Boolean)return BOOLEAN;if(v instanceof List)return ARRAY;return UNKNOWN;}
    public static boolean isCompatible(TNSType a,TNSType b){if(a==UNKNOWN||b==UNKNOWN)return true;if(a==NULL||b==NULL)return true;if((a==INTEGER&&b==FLOAT)||(a==FLOAT&&b==INTEGER))return true;return a==b;}
    public static TNSType promote(TNSType a,TNSType b){if(a==INTEGER&&b==INTEGER)return INTEGER;if((a==INTEGER||a==FLOAT)&&(b==INTEGER||b==FLOAT))return FLOAT;if(a==STRING||b==STRING)return STRING;if(a==UNKNOWN)return b;if(b==UNKNOWN)return a;return UNKNOWN;}
    public static TNSType fromHint(String h){return switch(h.toLowerCase()){case"int","inteiro"->INTEGER;case"float","decimal"->FLOAT;case"string","texto"->STRING;case"bool","booleano"->BOOLEAN;case"array","lista"->ARRAY;default->UNKNOWN;};}
}

class SemanticAnalyzer implements Visitor<Void> {
    private final Deque<Map<String,TNSType>> scopes=new ArrayDeque<>();
    private final List<String> errors=new ArrayList<>(),warnings=new ArrayList<>();
    public SemanticAnalyzer(){scopes.push(new HashMap<>());}
    public List<String> getErrors(){return errors;}
    public List<String> getWarnings(){return warnings;}
    public boolean analyze(ProgramNode p){p.accept(this);return errors.isEmpty();}
    private void define(String n,TNSType t){scopes.peek().put(n,t);}
    private TNSType lookup(String n){for(Map<String,TNSType> s:scopes)if(s.containsKey(n))return s.get(n);return null;}
    private void push(){scopes.push(new HashMap<>());}
    private void pop(){scopes.pop();}
    private void err(String m){errors.add("  x "+m);}
    private void warn(String m){warnings.add("  ! "+m);}
    @Override public Void visitProgramNode(ProgramNode n){for(ASTNode s:n.statements)if(s!=null)s.accept(this);return null;}
    @Override public Void visitBlockNode(BlockNode n){push();for(ASTNode s:n.statements)if(s!=null)s.accept(this);pop();return null;}
    @Override public Void visitPrintNode(PrintNode n){n.expression.accept(this);return null;}
    @Override public Void visitLetNode(LetNode n){
        TNSType vt=infer(n.value);n.value.accept(this);
        if(n.typeHint!=null){TNSType d=TNSType.fromHint(n.typeHint);if(!TNSType.isCompatible(d,vt))err("'"+n.name+"' declarada como "+d+" recebe "+vt);}
        if(scopes.peek().containsKey(n.name))warn("'"+n.name+"' redeclarada no mesmo escopo");
        define(n.name,vt);return null;
    }
    @Override public Void visitAssignNode(AssignNode n){TNSType cur=lookup(n.name);if(cur==null)err("'"+n.name+"' nao declarada");TNSType vt=infer(n.value);n.value.accept(this);if(cur!=null&&!TNSType.isCompatible(cur,vt))warn("Atribuicao incompativel '"+n.name+"': "+cur+" vs "+vt);return null;}
    @Override public Void visitVariableNode(VariableNode n){if(lookup(n.name)==null)err("'"+n.name+"' nao declarada");return null;}
    @Override public Void visitBinaryNode(BinaryNode n){n.left.accept(this);n.right.accept(this);return null;}
    @Override public Void visitUnaryNode(UnaryNode n){n.expression.accept(this);return null;}
    @Override public Void visitIfNode(IfNode n){n.condition.accept(this);n.thenBranch.accept(this);if(n.elseBranch!=null)n.elseBranch.accept(this);return null;}
    @Override public Void visitWhileNode(WhileNode n){n.condition.accept(this);n.body.accept(this);return null;}
    @Override public Void visitForNode(ForNode n){push();if(n.init!=null)n.init.accept(this);if(n.condition!=null)n.condition.accept(this);if(n.increment!=null)n.increment.accept(this);n.body.accept(this);pop();return null;}
    @Override public Void visitFunctionNode(FunctionNode n){define(n.name,TNSType.FUNCTION);push();for(String p:n.params)define(p,TNSType.UNKNOWN);n.body.accept(this);pop();return null;}
    @Override public Void visitCallNode(CallNode n){if(lookup(n.callee)==null)err("Funcao '"+n.callee+"' nao declarada");for(ASTNode a:n.arguments)a.accept(this);return null;}
    @Override public Void visitReturnNode(ReturnNode n){if(n.value!=null)n.value.accept(this);return null;}
    @Override public Void visitStringNode(StringNode n){return null;}
    @Override public Void visitNumberNode(NumberNode n){return null;}
    @Override public Void visitBooleanNode(BooleanNode n){return null;}
    @Override public Void visitNullNode(NullNode n){return null;}
    @Override public Void visitScanNode(ScanNode n){n.url.accept(this);return null;}
    @Override public Void visitDeepScanNode(DeepScanNode n){n.url.accept(this);if(n.depth!=null)n.depth.accept(this);return null;}
    @Override public Void visitAnalyzeNode(AnalyzeNode n){n.url.accept(this);return null;}
    @Override public Void visitDownloadNode(DownloadNode n){n.url.accept(this);if(n.destination!=null)n.destination.accept(this);return null;}
    @Override public Void visitGithubNode(GithubNode n){n.repo.accept(this);return null;}
    @Override public Void visitReportNode(ReportNode n){return null;}
    @Override public Void visitSyncNode(SyncNode n){return null;}
    private TNSType infer(ASTNode n){
        if(n instanceof StringNode)return TNSType.STRING;
        if(n instanceof NumberNode nu)return(nu.value instanceof Double)?TNSType.FLOAT:TNSType.INTEGER;
        if(n instanceof BooleanNode)return TNSType.BOOLEAN;
        if(n instanceof NullNode)return TNSType.NULL;
        if(n instanceof VariableNode va){TNSType t=lookup(va.name);return t!=null?t:TNSType.UNKNOWN;}
        if(n instanceof BinaryNode bi)return TNSType.promote(infer(bi.left),infer(bi.right));
        return TNSType.UNKNOWN;
    }
}
