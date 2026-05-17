package com.tonaco.tns;
import java.util.*;

interface ASTNode { <R> R accept(Visitor<R> visitor); }

interface Visitor<R> {
    R visitProgramNode(ProgramNode n); R visitPrintNode(PrintNode n);
    R visitLetNode(LetNode n); R visitAssignNode(AssignNode n); R visitVariableNode(VariableNode n);
    R visitStringNode(StringNode n); R visitNumberNode(NumberNode n); R visitBooleanNode(BooleanNode n);
    R visitNullNode(NullNode n); R visitBinaryNode(BinaryNode n); R visitUnaryNode(UnaryNode n);
    R visitIfNode(IfNode n); R visitWhileNode(WhileNode n); R visitForNode(ForNode n);
    R visitBlockNode(BlockNode n); R visitFunctionNode(FunctionNode n); R visitCallNode(CallNode n);
    R visitReturnNode(ReturnNode n);
    R visitScanNode(ScanNode n); R visitDeepScanNode(DeepScanNode n); R visitAnalyzeNode(AnalyzeNode n);
    R visitDownloadNode(DownloadNode n); R visitGithubNode(GithubNode n);
    R visitReportNode(ReportNode n); R visitSyncNode(SyncNode n);
}

class ProgramNode  implements ASTNode { public final List<ASTNode> statements; public ProgramNode(List<ASTNode> s){statements=s;} @Override public <R> R accept(Visitor<R> v){return v.visitProgramNode(this);} }
class PrintNode    implements ASTNode { public final ASTNode expression; public PrintNode(ASTNode e){expression=e;} @Override public <R> R accept(Visitor<R> v){return v.visitPrintNode(this);} }
class LetNode      implements ASTNode { public final String name; public final ASTNode value; public final String typeHint; public LetNode(String n,ASTNode v,String t){name=n;value=v;typeHint=t;} @Override public <R> R accept(Visitor<R> vi){return vi.visitLetNode(this);} }
class AssignNode   implements ASTNode { public final String name; public final ASTNode value; public AssignNode(String n,ASTNode v){name=n;value=v;} @Override public <R> R accept(Visitor<R> v){return v.visitAssignNode(this);} }
class IfNode       implements ASTNode { public final ASTNode condition,thenBranch,elseBranch; public IfNode(ASTNode c,ASTNode t,ASTNode e){condition=c;thenBranch=t;elseBranch=e;} @Override public <R> R accept(Visitor<R> v){return v.visitIfNode(this);} }
class WhileNode    implements ASTNode { public final ASTNode condition,body; public WhileNode(ASTNode c,ASTNode b){condition=c;body=b;} @Override public <R> R accept(Visitor<R> v){return v.visitWhileNode(this);} }
class ForNode      implements ASTNode { public final ASTNode init,condition,increment,body; public ForNode(ASTNode i,ASTNode c,ASTNode inc,ASTNode b){init=i;condition=c;increment=inc;body=b;} @Override public <R> R accept(Visitor<R> v){return v.visitForNode(this);} }
class BlockNode    implements ASTNode { public final List<ASTNode> statements; public BlockNode(List<ASTNode> s){statements=s;} @Override public <R> R accept(Visitor<R> v){return v.visitBlockNode(this);} }
class FunctionNode implements ASTNode { public final String name; public final List<String> params; public final ASTNode body; public FunctionNode(String n,List<String> p,ASTNode b){name=n;params=p;body=b;} @Override public <R> R accept(Visitor<R> v){return v.visitFunctionNode(this);} }
class CallNode     implements ASTNode { public final String callee; public final List<ASTNode> arguments; public CallNode(String c,List<ASTNode> a){callee=c;arguments=a;} @Override public <R> R accept(Visitor<R> v){return v.visitCallNode(this);} }
class ReturnNode   implements ASTNode { public final ASTNode value; public ReturnNode(ASTNode v){value=v;} @Override public <R> R accept(Visitor<R> v){return v.visitReturnNode(this);} }
class VariableNode implements ASTNode { public final String name; public VariableNode(String n){name=n;} @Override public <R> R accept(Visitor<R> v){return v.visitVariableNode(this);} }
class StringNode   implements ASTNode { public final String value; public StringNode(String v){value=v;} @Override public <R> R accept(Visitor<R> v){return v.visitStringNode(this);} }
class NumberNode   implements ASTNode { public final Object value; public NumberNode(Object v){value=v;} @Override public <R> R accept(Visitor<R> v){return v.visitNumberNode(this);} }
class BooleanNode  implements ASTNode { public final boolean value; public BooleanNode(boolean v){value=v;} @Override public <R> R accept(Visitor<R> v){return v.visitBooleanNode(this);} }
class NullNode     implements ASTNode { @Override public <R> R accept(Visitor<R> v){return v.visitNullNode(this);} }
class BinaryNode   implements ASTNode { public final TokenType operator; public final ASTNode left,right; public BinaryNode(TokenType op,ASTNode l,ASTNode r){operator=op;left=l;right=r;} @Override public <R> R accept(Visitor<R> v){return v.visitBinaryNode(this);} }
class UnaryNode    implements ASTNode { public final TokenType operator; public final ASTNode expression; public UnaryNode(TokenType op,ASTNode e){operator=op;expression=e;} @Override public <R> R accept(Visitor<R> v){return v.visitUnaryNode(this);} }
class ScanNode     implements ASTNode { public final ASTNode url; public ScanNode(ASTNode u){url=u;} @Override public <R> R accept(Visitor<R> v){return v.visitScanNode(this);} }
class DeepScanNode implements ASTNode { public final ASTNode url,depth; public DeepScanNode(ASTNode u,ASTNode d){url=u;depth=d;} @Override public <R> R accept(Visitor<R> v){return v.visitDeepScanNode(this);} }
class AnalyzeNode  implements ASTNode { public final ASTNode url; public AnalyzeNode(ASTNode u){url=u;} @Override public <R> R accept(Visitor<R> v){return v.visitAnalyzeNode(this);} }
class DownloadNode implements ASTNode { public final ASTNode url,destination; public DownloadNode(ASTNode u,ASTNode d){url=u;destination=d;} @Override public <R> R accept(Visitor<R> v){return v.visitDownloadNode(this);} }
class GithubNode   implements ASTNode { public final ASTNode repo; public GithubNode(ASTNode r){repo=r;} @Override public <R> R accept(Visitor<R> v){return v.visitGithubNode(this);} }
class ReportNode   implements ASTNode { public final String format; public ReportNode(String f){format=f;} @Override public <R> R accept(Visitor<R> v){return v.visitReportNode(this);} }
class SyncNode     implements ASTNode { @Override public <R> R accept(Visitor<R> v){return v.visitSyncNode(this);} }
