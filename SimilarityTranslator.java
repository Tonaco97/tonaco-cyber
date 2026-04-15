// SimilarityTranslator.java
// Tradutor baseado em similaridade semântica
// Não usa regras fixas - usa embeddings e similaridade
// Criado por: Guilherme Lucas Tonaco Carvalho

package com.tonaco.tns;

import java.util.*;

public class SimilarityTranslator {
    
    // Embeddings manuais para palavras (vetores de características semânticas)
    // Em uma versão real, isso seria treinado com dados reais
    private static final Map<String, double[]> embeddings = new HashMap<>();
    private static final Map<String, String> commandMap = new HashMap<>();
    
    static {
        // Embeddings dimensionais (8 dimensões para simplicidade)
        // Cada dimensão representa uma característica semântica
        initEmbeddings();
        initCommandMapping();
    }
    
    private static void initEmbeddings() {
        // Ação de busca/escaneamento
        embeddings.put("buscar", new double[]{1.0, 0.9, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0});
        embeddings.put("procurar", new double[]{0.95, 0.85, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0});
        embeddings.put("search", new double[]{0.98, 0.92, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0});
        embeddings.put("scan", new double[]{1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0});
        embeddings.put("sken", new double[]{0.9, 0.88, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0});
        embeddings.put("chercher", new double[]{0.92, 0.86, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0});
        embeddings.put("suchen", new double[]{0.91, 0.87, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0});
        embeddings.put("cercare", new double[]{0.93, 0.84, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0});
        
        // Ação de análise/inspeção
        embeddings.put("analisar", new double[]{0.0, 0.0, 1.0, 0.9, 0.0, 0.0, 0.0, 0.0});
        embeddings.put("inspecionar", new double[]{0.0, 0.0, 0.95, 0.85, 0.0, 0.0, 0.0, 0.0});
        embeddings.put("analyze", new double[]{0.0, 0.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0});
        embeddings.put("analyser", new double[]{0.0, 0.0, 0.98, 0.92, 0.0, 0.0, 0.0, 0.0});
        embeddings.put("analysieren", new double[]{0.0, 0.0, 0.97, 0.91, 0.0, 0.0, 0.0, 0.0});
        
        // Ação de download
        embeddings.put("baixar", new double[]{0.0, 0.0, 0.0, 0.0, 1.0, 0.9, 0.0, 0.0});
        embeddings.put("download", new double[]{0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 0.0, 0.0});
        embeddings.put("descargar", new double[]{0.0, 0.0, 0.0, 0.0, 0.95, 0.88, 0.0, 0.0});
        embeddings.put("telecharger", new double[]{0.0, 0.0, 0.0, 0.0, 0.96, 0.89, 0.0, 0.0});
        
        // Ação de escrita/impressão
        embeddings.put("escrever", new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.9});
        embeddings.put("print", new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0});
        embeddings.put("mostrar", new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.95, 0.85});
        embeddings.put("echar", new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.92, 0.88});
        
        // Variável
        embeddings.put("variavel", new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0});
        embeddings.put("variable", new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0});
        embeddings.put("variable", new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0});
        embeddings.put("variabile", new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.98});
        embeddings.put("variable", new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.99});
    }
    
    private static void initCommandMapping() {
        commandMap.put("scan", "scan");
        commandMap.put("deepscan", "deepscan");
        commandMap.put("download", "download");
        commandMap.put("analyze", "analyze");
        commandMap.put("print", "print");
        commandMap.put("variable", "variable");
        commandMap.put("report", "report");
        commandMap.put("sync", "sync");
        commandMap.put("share", "share");
        commandMap.put("github", "github");
    }
    
    public static String translate(String word) {
        word = word.toLowerCase().trim();
        
        // Se já é um comando conhecido
        if (commandMap.containsKey(word)) {
            return word;
        }
        
        // Procura por similaridade de embedding
        double[] targetEmbedding = embeddings.get(word);
        if (targetEmbedding == null) {
            // Tenta encontrar a palavra mais similar
            return findMostSimilar(word);
        }
        
        // Calcula similaridade com cada comando
        String bestMatch = null;
        double bestScore = -1;
        
        for (var entry : embeddings.entrySet()) {
            if (!commandMap.containsKey(entry.getKey())) continue;
            
            double score = cosineSimilarity(targetEmbedding, entry.getValue());
            if (score > bestScore) {
                bestScore = score;
                bestMatch = entry.getKey();
            }
        }
        
        if (bestScore > 0.6) {
            return commandMap.getOrDefault(bestMatch, word);
        }
        
        return word;
    }
    
    private static String findMostSimilar(String word) {
        // Características da palavra (análise simples)
        double[] target = extractFeatures(word);
        
        String bestMatch = null;
        double bestScore = -1;
        
        for (var entry : embeddings.entrySet()) {
            double score = cosineSimilarity(target, entry.getValue());
            if (score > bestScore && score > 0.3) {
                bestScore = score;
                bestMatch = entry.getKey();
            }
        }
        
        if (bestMatch != null && commandMap.containsKey(bestMatch)) {
            return commandMap.get(bestMatch);
        }
        
        return word;
    }
    
    private static double[] extractFeatures(String word) {
        // Extrai características baseadas em padrões da palavra
        double[] features = new double[8];
        
        // Contém 'c' ou 'k'? (busca/scan)
        if (word.contains("c") || word.contains("k") || word.contains("q")) {
            features[0] = 0.5;
        }
        // Contém 's'? (scan/search)
        if (word.contains("s")) {
            features[1] = 0.4;
        }
        // Contém 'a'? (analyze)
        if (word.contains("a") && word.length() > 3) {
            features[2] = 0.3;
        }
        // Contém 'l'? (analyze/analisar)
        if (word.contains("l")) {
            features[3] = 0.3;
        }
        // Contém 'd'? (download)
        if (word.contains("d")) {
            features[4] = 0.4;
        }
        // Contém 'w'? (download)
        if (word.contains("w")) {
            features[5] = 0.5;
        }
        // Termina com 'r'? (escrever/print)
        if (word.endsWith("r")) {
            features[6] = 0.6;
        }
        // Tamanho médio
        features[7] = Math.min(1.0, word.length() / 10.0);
        
        return features;
    }
    
    private static double cosineSimilarity(double[] a, double[] b) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        
        for (int i = 0; i < a.length; i++) {
            dotProduct += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        
        if (normA == 0 || normB == 0) return 0;
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
    
    public static String translateCommand(String originalCommand) {
        String[] parts = originalCommand.trim().split("\\s+", 2);
        String firstWord = parts[0];
        String rest = parts.length > 1 ? parts[1] : "";
        
        String translated = translate(firstWord);
        
        if (rest.isEmpty()) {
            return translated;
        }
        return translated + " " + rest;
    }
    
    public static double getSimilarityScore(String word1, String word2) {
        double[] emb1 = embeddings.getOrDefault(word1.toLowerCase(), extractFeatures(word1.toLowerCase()));
        double[] emb2 = embeddings.getOrDefault(word2.toLowerCase(), extractFeatures(word2.toLowerCase()));
        return cosineSimilarity(emb1, emb2);
    }
}
