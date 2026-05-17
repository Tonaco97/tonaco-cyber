package com.tonaco.tns;
import java.util.*;

public class SimilarityTranslator {
    private static final Map<String,String> DIRECT=new HashMap<>();
    static {
        for(String w:new String[]{"print","escrever","imprimir","mostrar","exibir","echo","afficher","anzeigen","mostrare","escribir","write","output","show","display"})DIRECT.put(w,"print");
        for(String w:new String[]{"let","var","variavel","variavel","variable","variabile","declare","definir","def","set","const"})DIRECT.put(w,"let");
        for(String w:new String[]{"if","se","si","wenn"})DIRECT.put(w,"if");
        for(String w:new String[]{"else","senao","senao","sinon","sonst","altrimenti","sino"})DIRECT.put(w,"else");
        for(String w:new String[]{"while","enquanto","mientras","pendant","waehrend","mentre"})DIRECT.put(w,"while");
        for(String w:new String[]{"for","para","pour","fuer","per"})DIRECT.put(w,"for");
        for(String w:new String[]{"function","funcao","funcao","funcion","fonction","funzione","func","def","sub"})DIRECT.put(w,"function");
        for(String w:new String[]{"return","retornar","retourne","restituire","devolver","ret"})DIRECT.put(w,"return");
        for(String w:new String[]{"true","verdadeiro","verdadero","vrai","wahr","vero"})DIRECT.put(w,"true");
        for(String w:new String[]{"false","falso","faux","falsch"})DIRECT.put(w,"false");
        for(String w:new String[]{"null","nulo","nul","nichts","nullo","ninguno"})DIRECT.put(w,"null");
        for(String w:new String[]{"scan","buscar","procurar","search","chercher","suchen","cercare","busca","varrer"})DIRECT.put(w,"scan");
        for(String w:new String[]{"deepscan","scanear","varredura","deep_scan","deepcrawl"})DIRECT.put(w,"deepscan");
        for(String w:new String[]{"analyze","analisar","inspecionar","verificar","analyser","analysieren","analizzare","analizar"})DIRECT.put(w,"analyze");
        for(String w:new String[]{"download","baixar","transferir","descargar","herunterladen","scaricare"})DIRECT.put(w,"download");
        for(String w:new String[]{"github","git","gh"})DIRECT.put(w,"github");
        for(String w:new String[]{"report","relatorio","relatorio","informe","rapport","bericht","rapporto"})DIRECT.put(w,"report");
        for(String w:new String[]{"sync","sincronizar","synchroniser","synchronisieren","sincronizzare"})DIRECT.put(w,"sync");
        for(String w:new String[]{"share","compartilhar","partager","teilen","condividere","compartir"})DIRECT.put(w,"share");
    }
    public static String translateCommand(String line){
        if(line==null||line.isBlank())return line;
        String trimmed=line.stripLeading();
        int indent=line.length()-trimmed.length();
        String indentStr=line.substring(0,indent);
        String[] parts=trimmed.split("\\s+",2);
        String word=parts[0];
        String rest=parts.length>1?" "+parts[1]:"";
        String translated=translateWord(word);
        return indentStr+translated+rest;
    }
    public static String translateWord(String w){
        if(w==null||w.isBlank())return w;
        String d=DIRECT.get(w.toLowerCase().trim());
        return d!=null?d:w;
    }
    public static double getSimilarity(String a,String b){return a.equalsIgnoreCase(b)?1.0:0.0;}
}
