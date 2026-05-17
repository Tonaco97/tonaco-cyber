package com.tonaco.tns;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import com.google.gson.*;

public class TradutorUniversal {
    private static final Gson GSON=new GsonBuilder().setPrettyPrinting().create();
    private static final String DIR=".tonaco_idiomas/";
    private final Map<String,Map<String,String>> dics=new LinkedHashMap<>();
    private String idioma="portugues";
    static{
    }
    public TradutorUniversal(){
        Map<String,String> pt=new LinkedHashMap<>();
        pt.put("escrever","print");pt.put("imprimir","print");pt.put("mostrar","print");
        pt.put("variavel","let");pt.put("variavel","let");pt.put("declarar","let");
        pt.put("se","if");pt.put("senao","else");pt.put("enquanto","while");
        pt.put("funcao","function");pt.put("retornar","return");
        pt.put("verdadeiro","true");pt.put("falso","false");pt.put("nulo","null");
        pt.put("buscar","scan");pt.put("scanear","deepscan");pt.put("analisar","analyze");
        pt.put("baixar","download");pt.put("relatorio","report");pt.put("sincronizar","sync");
        dics.put("portugues",pt);
        try{Files.createDirectories(Path.of(DIR));}catch(Exception ignored){}
    }
    public String detectarIdioma(String code){return idioma;}
    public String traduzirCodigo(String code){
        StringBuilder sb=new StringBuilder();
        for(String l:code.split("\n"))sb.append(SimilarityTranslator.translateCommand(l)).append("\n");
        return sb.toString();
    }
    public void listarIdiomas(){dics.forEach((k,v)->System.out.printf("  %-15s %d palavras%n",k,v.size()));}
    public String getIdiomaDetectado(){return idioma;}
    public Set<String> getIdiomasSuportados(){return dics.keySet();}
}
