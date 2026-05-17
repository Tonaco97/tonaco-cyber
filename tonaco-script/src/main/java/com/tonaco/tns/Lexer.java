package com.tonaco.tns;
import java.util.*;

public class Lexer {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start=0,current=0,line=1,column=1;
    private static final Map<String,TokenType> KW = new HashMap<>();
    static {
        // v2 canonical
        KW.put("print",TokenType.PRINT); KW.put("let",TokenType.LET);
        KW.put("if",TokenType.IF); KW.put("else",TokenType.ELSE);
        KW.put("while",TokenType.WHILE); KW.put("for",TokenType.FOR);
        KW.put("function",TokenType.FUNCTION); KW.put("return",TokenType.RETURN);
        KW.put("true",TokenType.TRUE); KW.put("false",TokenType.FALSE); KW.put("null",TokenType.NULL);
        KW.put("scan",TokenType.SCAN); KW.put("deepscan",TokenType.DEEPSCAN);
        KW.put("download",TokenType.DOWNLOAD); KW.put("analyze",TokenType.ANALYZE);
        KW.put("github",TokenType.GITHUB); KW.put("report",TokenType.REPORT);
        KW.put("sync",TokenType.SYNC); KW.put("share",TokenType.SHARE);
        // v1 aliases -> v2 tokens
        KW.put("escrever",TokenType.PRINT); KW.put("imprimir",TokenType.PRINT); KW.put("mostrar",TokenType.PRINT);
        KW.put("variavel",TokenType.LET); KW.put("var",TokenType.LET);
        KW.put("se",TokenType.IF); KW.put("senao",TokenType.ELSE);
        KW.put("enquanto",TokenType.WHILE); KW.put("funcao",TokenType.FUNCTION);
        KW.put("retornar",TokenType.RETURN);
        KW.put("verdadeiro",TokenType.TRUE); KW.put("falso",TokenType.FALSE); KW.put("nulo",TokenType.NULL);
        KW.put("buscar",TokenType.SCAN); KW.put("scanear",TokenType.DEEPSCAN);
        KW.put("analisar",TokenType.ANALYZE); KW.put("baixar",TokenType.DOWNLOAD);
        KW.put("relatorio",TokenType.REPORT); KW.put("sincronizar",TokenType.SYNC);
        KW.put("compartilhar",TokenType.SHARE);
    }
    public Lexer(String source){ this.source=source; }
    public List<Token> scanTokens(){
        while(!isAtEnd()){start=current;scanToken();}
        tokens.add(new Token(TokenType.EOF,"",null,line,column));
        return tokens;
    }
    private void scanToken(){
        char c=advance();
        switch(c){
            case '('->addToken(TokenType.LPAREN); case ')'->addToken(TokenType.RPAREN);
            case '{'->addToken(TokenType.LBRACE); case '}'->addToken(TokenType.RBRACE);
            case '['->addToken(TokenType.LBRACKET); case ']'->addToken(TokenType.RBRACKET);
            case ','->addToken(TokenType.COMMA); case ';'->addToken(TokenType.SEMICOLON);
            case ':'->addToken(TokenType.COLON); case '.'->addToken(TokenType.DOT);
            case '+'->addToken(TokenType.PLUS); case '-'->addToken(TokenType.MINUS);
            case '*'->addToken(TokenType.MULTIPLY); case '%'->addToken(TokenType.MODULO);
            case '='->addToken(match('=')?TokenType.EQUALS:TokenType.ASSIGN);
            case '!'->addToken(match('=')?TokenType.NOT_EQUALS:TokenType.NOT);
            case '<'->addToken(match('=')?TokenType.LESS_EQUAL:TokenType.LESS);
            case '>'->addToken(match('=')?TokenType.GREATER_EQUAL:TokenType.GREATER);
            case '&'->{ if(match('&'))addToken(TokenType.AND); }
            case '|'->{ if(match('|'))addToken(TokenType.OR); }
            case '/'->{
                if(match('/')){while(peek()!='\n'&&!isAtEnd())advance();}
                else if(match('*')){blockComment();}
                else addToken(TokenType.DIVIDE);
            }
            case '#'->{while(peek()!='\n'&&!isAtEnd())advance();}
            case ' ','\r','\t'->{}
            case '\n'->{line++;column=1;}
            case '"'->string('"'); case '\''->string('\'');
            default->{
                if(isDigit(c))number();
                else if(isAlpha(c))identifier();
                else System.err.printf("[Lexer] L%d: char desconhecido '%c'%n",line,c);
            }
        }
    }
    private void blockComment(){ while(!(peek()=='*'&&peekNext()=='/')&&!isAtEnd()){if(peek()=='\n'){line++;column=1;}advance();}if(!isAtEnd()){advance();advance();} }
    private void identifier(){ while(isAlphaNumeric(peek()))advance(); String t=source.substring(start,current); addToken(KW.getOrDefault(t.toLowerCase(),TokenType.IDENTIFIER)); }
    private void number(){ while(isDigit(peek()))advance(); if(peek()=='.'&&isDigit(peekNext())){advance();while(isDigit(peek()))advance();} String t=source.substring(start,current); try{addToken(TokenType.NUMBER,t.contains(".")?Double.parseDouble(t):Integer.parseInt(t));}catch(NumberFormatException e){addToken(TokenType.NUMBER,t);} }
    private void string(char d){ while(peek()!=d&&!isAtEnd()){if(peek()=='\n')line++;advance();}if(isAtEnd()){System.err.printf("[Lexer] String nao fechada L%d%n",line);return;}advance();addToken(TokenType.STRING,source.substring(start+1,current-1)); }
    private boolean match(char e){if(isAtEnd()||source.charAt(current)!=e)return false;current++;column++;return true;}
    private char advance(){char c=source.charAt(current++);column++;return c;}
    private char peek(){return isAtEnd()?'\0':source.charAt(current);}
    private char peekNext(){return(current+1>=source.length())?'\0':source.charAt(current+1);}
    private boolean isAtEnd(){return current>=source.length();}
    private boolean isDigit(char c){return c>='0'&&c<='9';}
    private boolean isAlpha(char c){return(c>='a'&&c<='z')||(c>='A'&&c<='Z')||c=='_';}
    private boolean isAlphaNumeric(char c){return isAlpha(c)||isDigit(c);}
    private void addToken(TokenType t){addToken(t,null);}
    private void addToken(TokenType t,Object lit){tokens.add(new Token(t,source.substring(start,current),lit,line,column-(current-start)));}
}
