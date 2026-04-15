// SimilarityTranslator.java
// Tradutor baseado em similaridade semântica — TONACO SCRIPT v2.0
// Permite escrever scripts em português, inglês, espanhol, francês, alemão e italiano
// Criado por: Guilherme Lucas Tonaco Carvalho

package com.tonaco.tns;

import java.util.*;

public class SimilarityTranslator {

    // ============================================================
    // MAPEAMENTO DIRETO — cobre 100% dos casos v1 e multilíngue
    // ============================================================

    // comando original (qualquer idioma) → keyword canônica v2
    private static final Map<String, String> DIRECT = new HashMap<>();
    // keyword v2 → token v2 canônico para reconstrução de linha
    private static final Map<String, String> CANONICAL = new HashMap<>();

    static {
        // --- PRINT ---
        for (String w : new String[]{
            "print","escrever","imprimir","mostrar","exibir","echo",
            "afficher","affiche","ecrire","anzeigen","mostrare","mostrar",
            "escribir","write","output","show","display","puts"
        }) DIRECT.put(w, "print");

        // --- LET (declaração de variável) ---
        for (String w : new String[]{
            "let","var","variavel","variável","variable","variabile",
            "declare","definir","def","set","const"
        }) DIRECT.put(w, "let");

        // --- IF ---
        for (String w : new String[]{
            "if","se","si","wenn","wenn","se"
        }) DIRECT.put(w, "if");

        // --- ELSE ---
        for (String w : new String[]{
            "else","senao","senão","sinon","sonst","altrimenti","sino"
        }) DIRECT.put(w, "else");

        // --- WHILE ---
        for (String w : new String[]{
            "while","enquanto","mientras","pendant","während","mentre"
        }) DIRECT.put(w, "while");

        // --- FOR ---
        for (String w : new String[]{
            "for","para","pour","für","per"
        }) DIRECT.put(w, "for");

        // --- FUNCTION ---
        for (String w : new String[]{
            "function","funcao","função","funcion","función","fonction","funzione","func","def","sub"
        }) DIRECT.put(w, "function");

        // --- RETURN ---
        for (String w : new String[]{
            "return","retornar","retourne","zurück","restituire","devolver","ret"
        }) DIRECT.put(w, "return");

        // --- TRUE/FALSE/NULL ---
        for (String w : new String[]{"true","verdadeiro","verdadero","vrai","wahr","vero"})
            DIRECT.put(w, "true");
        for (String w : new String[]{"false","falso","faux","falsch","falso"})
            DIRECT.put(w, "false");
        for (String w : new String[]{"null","nulo","nul","nichts","nullo","ninguno"})
            DIRECT.put(w, "null");

        // --- TNS: SCAN ---
        for (String w : new String[]{
            "scan","buscar","procurar","search","chercher","suchen","cercare","busca","sken","varrer"
        }) DIRECT.put(w, "scan");

        // --- TNS: DEEPSCAN ---
        for (String w : new String[]{
            "deepscan","scanear","scanear","varredura","escaneamento","deep_scan","deep-scan","deepcrawl"
        }) DIRECT.put(w, "deepscan");

        // --- TNS: ANALYZE ---
        for (String w : new String[]{
            "analyze","analisar","inspecionar","verificar","analyser","analysieren","analizzare","analizar"
        }) DIRECT.put(w, "analyze");

        // --- TNS: DOWNLOAD ---
        for (String w : new String[]{
            "download","baixar","transferir","descargar","télécharger","herunterladen","scaricare","baixa"
        }) DIRECT.put(w, "download");

        // --- TNS: GITHUB ---
        for (String w : new String[]{"github","git","gh"})
            DIRECT.put(w, "github");

        // --- TNS: REPORT ---
        for (String w : new String[]{
            "report","relatorio","relatório","informe","rapport","bericht","rapporto","gerar_relatorio"
        }) DIRECT.put(w, "report");

        // --- TNS: SYNC ---
        for (String w : new String[]{
            "sync","sincronizar","synchroniser","synchronisieren","sincronizzare","sincronizar"
        }) DIRECT.put(w, "sync");

        // --- TNS: SHARE ---
        for (String w : new String[]{
            "share","compartilhar","partager","teilen","condividere","compartir"
        }) DIRECT.put(w, "share");

        // --- Booleanos inline ---
        CANONICAL.put("verdadeiro", "true");
        CANONICAL.put("verdadero",  "true");
        CANONICAL.put("falso",      "false");
        CANONICAL.put("nulo",       "null");
    }

    // ============================================================
    // Embeddings semânticos de 8 dimensões para fallback
    // ============================================================

    private static final Map<String, double[]> EMBEDDINGS = new HashMap<>();

    static {
        // Dimensões: [scan, deepscan, analyze, download, print, let, report, sync]
        EMBEDDINGS.put("scan",     new double[]{1.0, 0.6, 0.1, 0.0, 0.0, 0.0, 0.0, 0.0});
        EMBEDDINGS.put("deepscan", new double[]{0.7, 1.0, 0.2, 0.0, 0.0, 0.0, 0.0, 0.0});
        EMBEDDINGS.put("analyze",  new double[]{0.2, 0.2, 1.0, 0.0, 0.0, 0.0, 0.2, 0.0});
        EMBEDDINGS.put("download", new double[]{0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0});
        EMBEDDINGS.put("print",    new double[]{0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0});
        EMBEDDINGS.put("let",      new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0});
        EMBEDDINGS.put("report",   new double[]{0.1, 0.1, 0.3, 0.0, 0.2, 0.0, 1.0, 0.0});
        EMBEDDINGS.put("sync",     new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0});
    }

    // ============================================================
    // API PÚBLICA
    // ============================================================

    /**
     * Traduz a primeira palavra de um comando para a keyword v2 canônica.
     * Retorna a linha original se nenhuma tradução for encontrada.
     */
    public static String translateCommand(String originalLine) {
        if (originalLine == null || originalLine.isBlank()) return originalLine;

        // Preserva indentação
        String trimmed = originalLine.stripLeading();
        int indent = originalLine.length() - trimmed.length();
        String indentStr = originalLine.substring(0, indent);

        // Divide em palavra de comando + resto
        String[] parts = trimmed.split("\\s+", 2);
        String word  = parts[0];
        String rest  = parts.length > 1 ? " " + parts[1] : "";

        String translated = translateWord(word);

        // Traduz também booleanos inline no resto da linha
        if (!rest.isEmpty()) {
            rest = translateInlineValues(rest);
        }

        return indentStr + translated + rest;
    }

    /**
     * Traduz uma palavra isolada para sua keyword v2 canônica.
     * Se não houver tradução direta, tenta similaridade por embedding.
     */
    public static String translateWord(String word) {
        if (word == null || word.isBlank()) return word;
        String lower = word.toLowerCase().trim();

        // 1. Tradução direta
        String direct = DIRECT.get(lower);
        if (direct != null) return direct;

        // 2. Fallback: similaridade de embedding
        double[] features = extractFeatures(lower);
        String best = null;
        double bestScore = 0.5; // threshold mínimo

        for (var entry : EMBEDDINGS.entrySet()) {
            double score = cosineSimilarity(features, entry.getValue());
            if (score > bestScore) {
                bestScore = score;
                best = entry.getKey();
            }
        }

        return best != null ? best : word; // retorna original se não achar nada
    }

    /**
     * Retorna o score de similaridade entre duas palavras (0.0 a 1.0).
     */
    public static double getSimilarity(String a, String b) {
        double[] ea = EMBEDDINGS.getOrDefault(a.toLowerCase(), extractFeatures(a.toLowerCase()));
        double[] eb = EMBEDDINGS.getOrDefault(b.toLowerCase(), extractFeatures(b.toLowerCase()));
        return cosineSimilarity(ea, eb);
    }

    // ============================================================
    // PRIVADOS
    // ============================================================

    private static String translateInlineValues(String text) {
        for (var entry : CANONICAL.entrySet()) {
            text = text.replaceAll("(?i)\\b" + entry.getKey() + "\\b", entry.getValue());
        }
        return text;
    }

    private static double[] extractFeatures(String word) {
        double[] f = new double[8];
        // Heurísticas fonéticas simples para identificar grupo semântico
        boolean hasSc = word.contains("sc") || word.contains("sk") || word.contains("sq");
        boolean hasAn = word.contains("an") || word.contains("al");
        boolean hasOw = word.contains("ow") || word.contains("oad") || word.contains("ow");
        boolean hasPr = word.startsWith("pr") || word.startsWith("es") || word.startsWith("wr");
        boolean hasVa = word.startsWith("va") || word.startsWith("de") || word.startsWith("le");
        boolean hasRe = word.startsWith("re") || word.contains("port") || word.contains("lat");
        boolean hasSy = word.startsWith("sy") || word.contains("sync") || word.contains("sin");

        f[0] = hasSc ? 0.7 : 0.0;
        f[1] = hasSc && word.length() > 5 ? 0.6 : 0.0;
        f[2] = hasAn ? 0.5 : 0.0;
        f[3] = hasOw ? 0.6 : 0.0;
        f[4] = hasPr ? 0.6 : 0.0;
        f[5] = hasVa ? 0.5 : 0.0;
        f[6] = hasRe ? 0.5 : 0.0;
        f[7] = hasSy ? 0.7 : 0.0;
        return f;
    }

    private static double cosineSimilarity(double[] a, double[] b) {
        double dot = 0, na = 0, nb = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            na  += a[i] * a[i];
            nb  += b[i] * b[i];
        }
        if (na == 0 || nb == 0) return 0;
        return dot / (Math.sqrt(na) * Math.sqrt(nb));
    }
}
