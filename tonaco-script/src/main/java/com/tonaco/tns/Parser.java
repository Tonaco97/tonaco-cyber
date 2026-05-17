package com.tonaco.tns;
import java.util.*;

public class Parser {
    private final List<Token> tokens;
    private int current=0;
    public Parser(List<Token> tokens){this.tokens=tokens;}

    public ProgramNode parse(){
        List<ASTNode> stmts=new ArrayList<>();
        while(!isAtEnd()){ASTNode s=declaration();if(s!=null)stmts.add(s);}
        return new ProgramNode(stmts);
    }
    private ASTNode declaration(){
        try{if(match(TokenType.FUNCTION))return functionDecl();return statement();}
        catch(RuntimeException e){synchronize();return null;}
    }
    private FunctionNode functionDecl(){
        Token name=consume(TokenType.IDENTIFIER,"Esperado nome da funcao");
        consume(TokenType.LPAREN,"Esperado '('");
        List<String> params=new ArrayList<>();
        if(!check(TokenType.RPAREN)){do{params.add(consume(TokenType.IDENTIFIER,"Esperado parametro").lexeme);}while(match(TokenType.COMMA));}
        consume(TokenType.RPAREN,"Esperado ')'");
        consume(TokenType.LBRACE,"Esperado '{'");
        return new FunctionNode(name.lexeme,params,blockStmt());
    }
    private ASTNode statement(){
        if(match(TokenType.PRINT))   return printStmt();
        if(match(TokenType.LET))     return letStmt();
        if(match(TokenType.IF))      return ifStmt();
        if(match(TokenType.WHILE))   return whileStmt();
        if(match(TokenType.FOR))     return forStmt();
        if(match(TokenType.RETURN))  return returnStmt();
        if(match(TokenType.LBRACE))  return blockStmt();
        if(match(TokenType.SCAN))    {ASTNode u=expr();semi();return new ScanNode(u);}
        if(match(TokenType.DEEPSCAN)){ASTNode u=expr();ASTNode d=match(TokenType.COMMA)?expr():null;semi();return new DeepScanNode(u,d);}
        if(match(TokenType.ANALYZE)) {ASTNode u=expr();semi();return new AnalyzeNode(u);}
        if(match(TokenType.DOWNLOAD)){ASTNode u=expr();ASTNode d=match(TokenType.COMMA)?expr():null;semi();return new DownloadNode(u,d);}
        if(match(TokenType.GITHUB))  {ASTNode r=expr();semi();return new GithubNode(r);}
        if(match(TokenType.REPORT))  {Token f=consume(TokenType.IDENTIFIER,"Esperado formato");semi();return new ReportNode(f.lexeme.toLowerCase());}
        if(match(TokenType.SYNC))    {semi();return new SyncNode();}
        return exprStmt();
    }
    private PrintNode printStmt(){ASTNode e=expr();semi();return new PrintNode(e);}
    private LetNode letStmt(){Token n=consume(TokenType.IDENTIFIER,"Esperado nome");String th=null;if(match(TokenType.COLON))th=consume(TokenType.IDENTIFIER,"Esperado tipo").lexeme;consume(TokenType.ASSIGN,"Esperado '='");ASTNode v=expr();semi();return new LetNode(n.lexeme,v,th);}
    private IfNode ifStmt(){consume(TokenType.LPAREN,"Esperado '('");ASTNode c=expr();consume(TokenType.RPAREN,"Esperado ')'");ASTNode t=statement();ASTNode e=null;if(match(TokenType.ELSE))e=statement();return new IfNode(c,t,e);}
    private WhileNode whileStmt(){consume(TokenType.LPAREN,"Esperado '('");ASTNode c=expr();consume(TokenType.RPAREN,"Esperado ')'");return new WhileNode(c,statement());}
    private ForNode forStmt(){
        consume(TokenType.LPAREN,"Esperado '('");
        ASTNode init=null;
        if(!check(TokenType.SEMICOLON)){if(match(TokenType.LET))init=letStmt();else init=exprStmt();}else consume(TokenType.SEMICOLON,"");
        ASTNode cond=check(TokenType.SEMICOLON)?new BooleanNode(true):expr();
        consume(TokenType.SEMICOLON,"Esperado ';'");
        ASTNode inc=check(TokenType.RPAREN)?null:expr();
        consume(TokenType.RPAREN,"Esperado ')'");
        return new ForNode(init,cond,inc,statement());
    }
    private ReturnNode returnStmt(){ASTNode v=(!check(TokenType.SEMICOLON)&&!isAtEnd())?expr():null;semi();return new ReturnNode(v);}
    private BlockNode blockStmt(){List<ASTNode> s=new ArrayList<>();while(!check(TokenType.RBRACE)&&!isAtEnd()){ASTNode d=declaration();if(d!=null)s.add(d);}consume(TokenType.RBRACE,"Esperado '}'");return new BlockNode(s);}
    private ASTNode exprStmt(){ASTNode e=expr();semi();return e;}

    private ASTNode expr(){return assign();}
    private ASTNode assign(){ASTNode e=or();if(match(TokenType.ASSIGN)){ASTNode v=assign();if(e instanceof VariableNode va)return new AssignNode(va.name,v);throw err("Atribuicao invalida");}return e;}
    private ASTNode or(){ASTNode e=and();while(match(TokenType.OR)){e=new BinaryNode(TokenType.OR,e,and());}return e;}
    private ASTNode and(){ASTNode e=eq();while(match(TokenType.AND)){e=new BinaryNode(TokenType.AND,e,eq());}return e;}
    private ASTNode eq(){ASTNode e=cmp();while(match(TokenType.EQUALS,TokenType.NOT_EQUALS)){e=new BinaryNode(previous().type,e,cmp());}return e;}
    private ASTNode cmp(){ASTNode e=add();while(match(TokenType.LESS,TokenType.GREATER,TokenType.LESS_EQUAL,TokenType.GREATER_EQUAL)){e=new BinaryNode(previous().type,e,add());}return e;}
    private ASTNode add(){ASTNode e=mul();while(match(TokenType.PLUS,TokenType.MINUS)){e=new BinaryNode(previous().type,e,mul());}return e;}
    private ASTNode mul(){ASTNode e=unary();while(match(TokenType.MULTIPLY,TokenType.DIVIDE,TokenType.MODULO)){e=new BinaryNode(previous().type,e,unary());}return e;}
    private ASTNode unary(){if(match(TokenType.NOT,TokenType.MINUS))return new UnaryNode(previous().type,unary());return call();}
    private ASTNode call(){ASTNode e=primary();if(e instanceof VariableNode va&&match(TokenType.LPAREN)){List<ASTNode> args=new ArrayList<>();if(!check(TokenType.RPAREN)){do{args.add(expr());}while(match(TokenType.COMMA));}consume(TokenType.RPAREN,"Esperado ')'");return new CallNode(va.name,args);}return e;}
    private ASTNode primary(){
        if(match(TokenType.NUMBER)) return new NumberNode(previous().literal);
        if(match(TokenType.STRING)) return new StringNode((String)previous().literal);
        if(match(TokenType.TRUE))   return new BooleanNode(true);
        if(match(TokenType.FALSE))  return new BooleanNode(false);
        if(match(TokenType.NULL))   return new NullNode();
        if(match(TokenType.IDENTIFIER)) return new VariableNode(previous().lexeme);
        if(match(TokenType.LPAREN)){ASTNode e=expr();consume(TokenType.RPAREN,"Esperado ')'");return e;}
        throw err("Expressao invalida: "+peek().lexeme);
    }
    private boolean match(TokenType... ts){for(TokenType t:ts){if(check(t)){advance();return true;}}return false;}
    private Token consume(TokenType t,String msg){if(check(t))return advance();throw err(msg);}
    private void semi(){match(TokenType.SEMICOLON);}
    private boolean check(TokenType t){return!isAtEnd()&&peek().type==t;}
    private Token advance(){if(!isAtEnd())current++;return previous();}
    private boolean isAtEnd(){return peek().type==TokenType.EOF;}
    private Token peek(){return tokens.get(current);}
    private Token previous(){return tokens.get(current-1);}
    private RuntimeException err(String msg){String m=String.format("[Parser] L%d: %s",peek().line,msg);System.err.println(m);return new RuntimeException(m);}
    private void synchronize(){advance();while(!isAtEnd()){if(previous().type==TokenType.SEMICOLON)return;switch(peek().type){case FUNCTION,LET,IF,WHILE,FOR,PRINT,RETURN->{return;}default->advance();}}}
}
