package com.tonaco.tns;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.security.MessageDigest;
import com.google.gson.*;

public class AutoEvolucao {
    private static final Gson GSON=new GsonBuilder().setPrettyPrinting().create();
    private static final String DIR=".tonaco_comandos/";
    private static final String ARQ=DIR+"comandos.json";
    private Map<String,ComandoPersonalizado> comandos=new LinkedHashMap<>();
    public AutoEvolucao(){try{Files.createDirectories(Path.of(DIR));}catch(Exception ignored){}carregarComandos();}
    private void carregarComandos(){
        try{Path p=Path.of(ARQ);if(!Files.exists(p))return;
            JsonObject obj=GSON.fromJson(Files.readString(p),JsonObject.class);
            if(obj==null)return;
            for(var e:obj.entrySet())comandos.put(e.getKey(),GSON.fromJson(e.getValue(),ComandoPersonalizado.class));
            System.out.printf("[AutoEvolucao] %d comandos carregados.%n",comandos.size());
        }catch(Exception e){System.err.println("[AutoEvolucao] Aviso: "+e.getMessage());}
    }
    public void criarComando(String nome,String desc,String acao){
        if(nome==null||nome.isBlank())return;
        ComandoPersonalizado c=new ComandoPersonalizado();
        c.nome=nome;c.descricao=desc;c.acao=acao;c.criador=System.getProperty("user.name","tonaco");c.dataCriacao=System.currentTimeMillis();c.hash=sha256(nome+acao);
        comandos.put(nome,c);salvar();
        System.out.printf("[AutoEvolucao] Comando '%s' criado.%n",nome);
    }
    public void listarComandos(){
        System.out.println("\nNATIVOS: scan, deepscan, analyze, download, github, report, sync, share, print, let");
        if(!comandos.isEmpty()){System.out.printf("PERSONALIZADOS (%d):%n",comandos.size());comandos.forEach((k,v)->System.out.printf("  %-20s -> %s%n",k,v.descricao));}
    }
    public void sincronizar(){System.out.println("[AutoEvolucao] Sincronizando... (requer endpoint configurado)");}
    public void compartilharComando(String n){if(comandos.containsKey(n))System.out.println("[AutoEvolucao] Compartilhando '"+n+"': "+comandos.get(n).hash);}
    private void salvar(){try{JsonObject o=new JsonObject();for(var e:comandos.entrySet())o.add(e.getKey(),GSON.toJsonTree(e.getValue()));Files.writeString(Path.of(ARQ),GSON.toJson(o));}catch(Exception ignored){}}
    private static String sha256(String s){try{MessageDigest md=MessageDigest.getInstance("SHA-256");byte[]h=md.digest(s.getBytes());StringBuilder sb=new StringBuilder();for(byte b:h)sb.append(String.format("%02x",b));return sb.substring(0,16);}catch(Exception e){return Integer.toHexString(s.hashCode());}}
    private static class ComandoPersonalizado{String nome,descricao,acao,criador,hash;long dataCriacao;int votos=0,downloads=0;}
}
