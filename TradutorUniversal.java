// TradutorUniversal.java
// Módulo de tradução multilíngue para TONACO SCRIPT v2.0
// Detecta idioma e normaliza comandos para sintaxe v2 canônica
// Criado por: Guilherme Lucas Tonaco Carvalho

package com.tonaco.tns;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import com.google.gson.*;

public class TradutorUniversal {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String DIR_IDIOMAS   = ".tonaco_idiomas/";
    private static final String ARQ_TRADUCOES = DIR_IDIOMAS + "traducoes.json";

    // Idiomas suportados → Map<palavra, keyword_v2>
    private final Map<String, Map<String, String>> dicionarios = new LinkedHashMap<>();
    private String idiomaDetectado = "portugues";

    // ============================================================
    // Dicionários embutidos
    // ============================================================

    private static final Map<String, Map<String, String>> DICS_BUILTIN = new LinkedHashMap<>();

    static {
        Map<String, String> pt = new LinkedHashMap<>();
        pt.put("escrever",   "print");    pt.put("imprimir",   "print");
        pt.put("mostrar",    "print");    pt.put("exibir",     "print");
        pt.put("variavel",   "let");      pt.put("variável",   "let");
        pt.put("declarar",   "let");      pt.put("definir",    "let");
        pt.put("se",         "if");       pt.put("senao",      "else");
        pt.put("senão",      "else");     pt.put("enquanto",   "while");
        pt.put("para",       "for");      pt.put("funcao",     "function");
        pt.put("função",     "function"); pt.put("retornar",   "return");
        pt.put("verdadeiro", "true");     pt.put("falso",      "false");
        pt.put("nulo",       "null");
        pt.put("buscar",     "scan");     pt.put("procurar",   "scan");
        pt.put("scanear",    "deepscan"); pt.put("varrer",     "deepscan");
        pt.put("analisar",   "analyze");  pt.put("inspecionar","analyze");
        pt.put("baixar",     "download"); pt.put("transferir", "download");
        pt.put("relatorio",  "report");   pt.put("relatório",  "report");
        pt.put("sincronizar","sync");     pt.put("compartilhar","share");
        DICS_BUILTIN.put("portugues", pt);

        Map<String, String> es = new LinkedHashMap<>();
        es.put("imprimir",   "print");    es.put("mostrar",    "print");
        es.put("variable",   "let");      es.put("si",         "if");
        es.put("sino",       "else");     es.put("mientras",   "while");
        es.put("para",       "for");      es.put("función",    "function");
        es.put("retornar",   "return");   es.put("verdadero",  "true");
        es.put("falso",      "false");    es.put("nulo",       "null");
        es.put("buscar",     "scan");     es.put("analizar",   "analyze");
        es.put("descargar",  "download"); es.put("informe",    "report");
        es.put("sincronizar","sync");     es.put("compartir",  "share");
        DICS_BUILTIN.put("espanhol", es);

        Map<String, String> fr = new LinkedHashMap<>();
        fr.put("afficher",   "print");    fr.put("ecrire",     "print");
        fr.put("variable",   "let");      fr.put("si",         "if");
        fr.put("sinon",      "else");     fr.put("pendant",    "while");
        fr.put("pour",       "for");      fr.put("fonction",   "function");
        fr.put("retourner",  "return");   fr.put("vrai",       "true");
        fr.put("faux",       "false");    fr.put("nul",        "null");
        fr.put("chercher",   "scan");     fr.put("analyser",   "analyze");
        fr.put("telecharger","download"); fr.put("rapport",    "report");
        fr.put("synchroniser","sync");    fr.put("partager",   "share");
        DICS_BUILTIN.put("frances", fr);

        Map<String, String> de = new LinkedHashMap<>();
        de.put("anzeigen",   "print");    de.put("ausgeben",   "print");
        de.put("variable",   "let");      de.put("wenn",       "if");
        de.put("sonst",      "else");     de.put("waehrend",   "while");
        de.put("fuer",       "for");      de.put("funktion",   "function");
        de.put("zurueck",    "return");   de.put("wahr",       "true");
        de.put("falsch",     "false");    de.put("nichts",     "null");
        de.put("suchen",     "scan");     de.put("analysieren","analyze");
        de.put("herunterladen","download");de.put("bericht",   "report");
        de.put("synchronisieren","sync"); de.put("teilen",     "share");
        DICS_BUILTIN.put("alemao", de);
    }

    // ============================================================
    // Construtor
    // ============================================================

    public TradutorUniversal() {
        dicionarios.putAll(DICS_BUILTIN);
        criarDiretorios();
        carregarTraducoesExternas();
    }

    private void criarDiretorios() {
        try { Files.createDirectories(Path.of(DIR_IDIOMAS)); }
        catch (IOException ignored) {}
    }

    private void carregarTraducoesExternas() {
        try {
            Path p = Path.of(ARQ_TRADUCOES);
            if (!Files.exists(p)) return;
            @SuppressWarnings("unchecked")
            Map<String, Map<String, String>> externo =
                GSON.fromJson(Files.readString(p), LinkedHashMap.class);
            if (externo != null) dicionarios.putAll(externo);
        } catch (Exception ignored) {}
    }

    // ============================================================
    // API pública
    // ============================================================

    /**
     * Detecta o idioma dominante de um trecho de código.
     */
    public String detectarIdioma(String codigo) {
        Map<String, Integer> scores = new LinkedHashMap<>();
        for (String idioma : dicionarios.keySet()) scores.put(idioma, 0);

        String[] palavras = codigo.toLowerCase().split("[\\s;(){}\\[\\],\"']+");
        for (String p : palavras) {
            for (var entry : dicionarios.entrySet()) {
                if (entry.getValue().containsKey(p)) {
                    scores.merge(entry.getKey(), 1, Integer::sum);
                }
            }
        }

        idiomaDetectado = scores.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("portugues");

        return idiomaDetectado;
    }

    /**
     * Traduz uma linha de código para sintaxe v2 canônica.
     */
    public String traduzirLinha(String linha) {
        return SimilarityTranslator.translateCommand(linha);
    }

    /**
     * Traduz um bloco inteiro de código.
     */
    public String traduzirCodigo(String codigo) {
        detectarIdioma(codigo);
        StringBuilder sb = new StringBuilder();
        for (String linha : codigo.split("\n")) {
            sb.append(traduzirLinha(linha)).append("\n");
        }
        return sb.toString();
    }

    /**
     * Adiciona traduções externas para um idioma.
     */
    public void adicionarTraducoes(String idioma, Map<String, String> traducoes) {
        dicionarios.computeIfAbsent(idioma, k -> new LinkedHashMap<>()).putAll(traducoes);
        try {
            Files.writeString(Path.of(ARQ_TRADUCOES), GSON.toJson(dicionarios));
        } catch (IOException ignored) {}
    }

    public String getIdiomaDetectado() { return idiomaDetectado; }
    public Set<String> getIdiomasSuportados() { return dicionarios.keySet(); }

    public void listarIdiomas() {
        System.out.println("[TradutorUniversal] Idiomas suportados:");
        for (var entry : dicionarios.entrySet()) {
            System.out.printf("  %-15s → %d palavras mapeadas%n", entry.getKey(), entry.getValue().size());
        }
    }
}
