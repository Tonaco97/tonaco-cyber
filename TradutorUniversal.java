// TradutorUniversal.java
// Módulo de tradução multilíngue para TONACO SCRIPT
// Criado por: Guilherme Lucas Tonaco Carvalho

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import com.google.gson.*;

public class TradutorUniversal {
    
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final String PASTA_IDIOMAS = ".tonaco_idiomas/";
    private static final String ARQUIVO_TRADUCOES = PASTA_IDIOMAS + "traducoes.json";
    
    private Map<String, Map<String, String>> dicionarioGlobal;
    private Map<String, String> mapaComandos;
    private String idiomaDetectado;
    
    // Mapeamento de comandos nativos (em inglês como padrão)
    private static final Map<String, String> COMANDOS_PADRAO = new LinkedHashMap<>();
    
    static {
        COMANDOS_PADRAO.put("buscar", "scan");
        COMANDOS_PADRAO.put("scanear", "deepscan");
        COMANDOS_PADRAO.put("baixar", "download");
        COMANDOS_PADRAO.put("analisar", "analyze");
        COMANDOS_PADRAO.put("github", "github");
        COMANDOS_PADRAO.put("relatorio", "report");
        COMANDOS_PADRAO.put("escrever", "print");
        COMANDOS_PADRAO.put("variavel", "variable");
        COMANDOS_PADRAO.put("imprimir", "echo");
        COMANDOS_PADRAO.put("sincronizar", "sync");
        COMANDOS_PADRAO.put("criar_comando", "create_command");
        COMANDOS_PADRAO.put("compartilhar", "share");
        COMANDOS_PADRAO.put("relatorio_evolucao", "evolution_report");
        COMANDOS_PADRAO.put("recomendar", "recommend");
        COMANDOS_PADRAO.put("comandos", "commands");
    }
    
    public TradutorUniversal() {
        this.dicionarioGlobal = new HashMap<>();
        this.mapaComandos = new HashMap<>();
        this.idiomaDetectado = "portugues";
        criarPastas();
        carregarTraducoes();
        inicializarDicionarioBase();
    }
    
    private void criarPastas() {
        try {
            Files.createDirectories(Path.of(PASTA_IDIOMAS));
        } catch (IOException e) {
            System.err.println("Erro ao criar pastas de idioma: " + e.getMessage());
        }
    }
    
    private void inicializarDicionarioBase() {
        // Dicionário Português -> Inglês
        Map<String, String> portugues = new HashMap<>();
        portugues.put("buscar", "scan");
        portugues.put("procure", "scan");
        portugues.put("ache", "scan");
        portugues.put("escaneie", "deepscan");
        portugues.put("varredura", "deepscan");
        portugues.put("baixar", "download");
        portugues.put("transferir", "download");
        portugues.put("analisar", "analyze");
        portugues.put("inspecionar", "analyze");
        portugues.put("verificar", "analyze");
        portugues.put("escrever", "print");
        portugues.put("mostrar", "print");
        portugues.put("exibir", "print");
        portugues.put("variavel", "variable");
        portugues.put("declare", "variable");
        portugues.put("imprimir", "echo");
        portugues.put("exiba", "echo");
        portugues.put("relatorio", "report");
        portugues.put("relate", "report");
        portugues.put("sincronizar", "sync");
        portugues.put("atualizar", "sync");
        portugues.put("criar_comando", "create_command");
        portugues.put("novo_comando", "create_command");
        portugues.put("compartilhar", "share");
        portugues.put("enviar", "share");
        dicionarioGlobal.put("portugues", portugues);
        
        // Dicionário Espanhol -> Inglês
        Map<String, String> espanhol = new HashMap<>();
        espanhol.put("buscar", "scan");
        espanhol.put("busque", "scan");
        espanhol.put("escanear", "deepscan");
        espanhol.put("descargar", "download");
        espanhol.put("analizar", "analyze");
        espanhol.put("escribir", "print");
        espanhol.put("variable", "variable");
        espanhol.put("imprimir", "echo");
        espanhol.put("informe", "report");
        espanhol.put("sincronizar", "sync");
        espanhol.put("crear_comando", "create_command");
        espanhol.put("compartir", "share");
        dicionarioGlobal.put("espanhol", espanhol);
        
        // Dicionário Francês -> Inglês
        Map<String, String> frances = new HashMap<>();
        frances.put("chercher", "scan");
        frances.put("analyser", "analyze");
        frances.put("telecharger", "download");
        frances.put("ecrire", "print");
        frances.put("variable", "variable");
        frances.put("afficher", "echo");
        frances.put("rapport", "report");
        frances.put("synchroniser", "sync");
        frances.put("creer_commande", "create_command");
        frances.put("partager", "share");
        dicionarioGlobal.put("frances", frances);
        
        // Dicionário Alemão -> Inglês
        Map<String, String> alemao = new HashMap<>();
        alemao.put("suchen", "scan");
        alemao.put("scannen", "deepscan");
        alemao.put("herunterladen", "download");
        alemao.put("analysieren", "analyze");
        alemao.put("schreiben", "print");
        alemao.put("variable", "variable");
        alemao.put("zeigen", "echo");
        alemao.put("bericht", "report");
        alemao.put("synchronisieren", "sync");
        alemao.put("befehl_erstellen", "create_command");
        alemao.put("teilen", "share");
        dicionarioGlobal.put("alemao", alemao);
        
        // Dicionário Italiano -> Inglês
        Map<String, String> italiano = new HashMap<>();
        italiano.put("cercare", "scan");
        italiano.put("scansionare", "deepscan");
        italiano.put("scaricare", "download");
        italiano.put("analizzare", "analyze");
        italiano.put("scrivere", "print");
        italiano.put("variabile", "variable");
        italiano.put("mostrare", "echo");
        italiano.put("rapporto", "report");
        italiano.put("sincronizzare", "sync");
        italiano.put("crea_comando", "create_command");
        italiano.put("condividere", "share");
        dicionarioGlobal.put("italiano", italiano);
        
        // Dicionário Russo -> Inglês (transliterado)
        Map<String, String> russo = new HashMap<>();
        russo.put("poisk", "scan");
        russo.put("skanirovat", "deepscan");
        russo.put("skachat", "download");
        russo.put("analizirovat", "analyze");
        russo.put("napisat", "print");
        russo.put("peremennaya", "variable");
        russo.put("vyvesti", "echo");
        russo.put("otchet", "report");
        russo.put("sinkhronizirovat", "sync");
        dicionarioGlobal.put("russo", russo);
        
        // Dicionário Chinês (Mandarin simplificado) -> Inglês
        Map<String, String> chines = new HashMap<>();
        chines.put("sousuo", "scan");
        chines.put("saomiao", "deepscan");
        chines.put("xiazai", "download");
        chines.put("fenxi", "analyze");
        chines.put("shuru", "print");
        chines.put("bianliang", "variable");
        chines.put("xianshi", "echo");
        chines.put("baogao", "report");
        chines.put("tongbu", "sync");
        dicionarioGlobal.put("chines", chines);
        
        // Dicionário Japonês -> Inglês
        Map<String, String> japones = new HashMap<>();
        japones.put("kensaku", "scan");
        japones.put("sukyan", "deepscan");
        japones.put("daunrōdo", "download");
        japones.put("bunseki", "analyze");
        japones.put("hyōji", "print");
        japones.put("hensū", "variable");
        japones.put("hyōji_suru", "echo");
        japones.put("hōkoku", "report");
        japones.put("dōki", "sync");
        dicionarioGlobal.put("japones", japones);
        
        // Dicionário Árabe -> Inglês (transliterado)
        Map<String, String> arabe = new HashMap<>();
        arabe.put("bahth", "scan");
        arabe.put("mash", "deepscan");
        arabe.put("tanzil", "download");
        arabe.put("tahlil", "analyze");
        arabe.put("kitabah", "print");
        arabe.put("mutaghayir", "variable");
        arabe.put("iradah", "echo");
        arabe.put("tachrir", "report");
        arabe.put("muzayadah", "sync");
        dicionarioGlobal.put("arabe", arabe);
        
        // Dicionário Hindi -> Inglês (transliterado)
        Map<String, String> hindi = new HashMap<>();
        hindi.put("khoj", "scan");
        hindi.put("sken", "deepscan");
        hindi.put("daunlod", "download");
        hindi.put("vishleshan", "analyze");
        hindi.put("likho", "print");
        hindi.put("char", "variable");
        hindi.put("dikhao", "echo");
        hindi.put("riport", "report");
        hindi.put("synchronize", "sync");
        dicionarioGlobal.put("hindi", hindi);
        
        // Dicionário Coreano -> Inglês (transliterado)
        Map<String, String> coreano = new HashMap<>();
        coreano.put("geomsaek", "scan");
        coreano.put("seuken", "deepscan");
        coreano.put("daunrodeu", "download");
        coreano.put("bunseok", "analyze");
        coreano.put("sseugi", "print");
        coreano.put("byeonsu", "variable");
        coreano.put("pyosi", "echo");
        coreano.put("bogoseo", "report");
        coreano.put("donggi", "sync");
        dicionarioGlobal.put("coreano", coreano);
    }
    
    private void carregarTraducoes() {
        try {
            Path arquivo = Path.of(ARQUIVO_TRADUCOES);
            if (Files.exists(arquivo)) {
                String conteudo = Files.readString(arquivo);
                JsonObject obj = gson.fromJson(conteudo, JsonObject.class);
                for (var entry : obj.entrySet()) {
                    String idioma = entry.getKey();
                    JsonObject dict = entry.getValue().getAsJsonObject();
                    Map<String, String> mapa = new HashMap<>();
                    for (var e : dict.entrySet()) {
                        mapa.put(e.getKey(), e.getValue().getAsString());
                    }
                    dicionarioGlobal.put(idioma, mapa);
                }
                System.out.println("[TRADUTOR] " + dicionarioGlobal.size() + " idiomas carregados.");
            }
        } catch (Exception e) {
            // Arquivo não existe, usa dicionário base
        }
    }
    
    public void salvarTraducoes() {
        try {
            JsonObject obj = new JsonObject();
            for (var entry : dicionarioGlobal.entrySet()) {
                JsonObject dict = new JsonObject();
                for (var e : entry.getValue().entrySet()) {
                    dict.addProperty(e.getKey(), e.getValue());
                }
                obj.add(entry.getKey(), dict);
            }
            Files.writeString(Path.of(ARQUIVO_TRADUCOES), gson.toJson(obj));
        } catch (IOException e) {
            System.err.println("Erro ao salvar traduções: " + e.getMessage());
        }
    }
    
    public String detectarIdioma(String comando) {
        String comandoLower = comando.toLowerCase().trim();
        String primeiraPalavra = comandoLower.split(" ")[0];
        
        // Verifica se já está em inglês (comando nativo)
        if (COMANDOS_PADRAO.containsValue(primeiraPalavra) || 
            COMANDOS_PADRAO.containsKey(primeiraPalavra)) {
            return "ingles";
        }
        
        // Procura em todos os dicionários
        for (var entry : dicionarioGlobal.entrySet()) {
            if (entry.getValue().containsKey(primeiraPalavra)) {
                this.idiomaDetectado = entry.getKey();
                return entry.getKey();
            }
        }
        
        // Se não encontrou, tenta detectar por caracteres especiais
        if (comando.matches(".*[\\u0600-\\u06FF].*")) return "arabe";
        if (comando.matches(".*[\\u4E00-\\u9FFF].*")) return "chines";
        if (comando.matches(".*[\\u3040-\\u309F\\u30A0-\\u30FF].*")) return "japones";
        if (comando.matches(".*[\\uAC00-\\uD7AF].*")) return "coreano";
        if (comando.matches(".*[\\u0400-\\u04FF].*")) return "russo";
        if (comando.matches(".*[\\u0900-\\u097F].*")) return "hindi";
        
        return "portugues";
    }
    
    public String traduzirComando(String comandoOriginal) {
        String comandoLower = comandoOriginal.toLowerCase().trim();
        String primeiraPalavra = comandoLower.split(" ")[0];
        String resto = comandoLower.substring(primeiraPalavra.length()).trim();
        
        // Se já é inglês, mantém
        if (COMANDOS_PADRAO.containsValue(primeiraPalavra)) {
            return comandoOriginal;
        }
        
        // Procura tradução
        String traducao = null;
        for (var dict : dicionarioGlobal.values()) {
            if (dict.containsKey(primeiraPalavra)) {
                traducao = dict.get(primeiraPalavra);
                break;
            }
        }
        
        // Se não encontrou, mantém original (pode ser variável)
        if (traducao == null) {
            return comandoOriginal;
        }
        
        // Reconstrói o comando traduzido
        String comandoTraduzido = traducao;
        if (!resto.isEmpty()) {
            comandoTraduzido += " " + resto;
        }
        
        // Substitui placeholders traduzidos
        comandoTraduzido = traduzirParametros(comandoTraduzido);
        
        return comandoTraduzido;
    }
    
    private String traduzirParametros(String comando) {
        // Traduz parâmetros comuns
        Map<String, String> parametros = new HashMap<>();
        parametros.put("--profundidade", "--depth");
        parametros.put("--formato", "--format");
        parametros.put("--destino", "--dest");
        
        for (var entry : parametros.entrySet()) {
            comando = comando.replace(entry.getKey(), entry.getValue());
        }
        
        return comando;
    }
    
    public void aprenderPalavra(String idioma, String palavraOriginal, String traducao) {
        if (!dicionarioGlobal.containsKey(idioma)) {
            dicionarioGlobal.put(idioma, new HashMap<>());
        }
        
        dicionarioGlobal.get(idioma).put(palavraOriginal.toLowerCase(), traducao);
        salvarTraducoes();
        
        System.out.println("[TRADUTOR] Nova palavra aprendida: " + palavraOriginal + " -> " + traducao);
    }
    
    public void aprenderComContexto(String comandoOriginal, String comandoExecutado) {
        String primeiraOriginal = comandoOriginal.toLowerCase().split(" ")[0];
        String primeiraExecutado = comandoExecutado.toLowerCase().split(" ")[0];
        
        if (!primeiraOriginal.equals(primeiraExecutado) && 
            !COMANDOS_PADRAO.containsKey(primeiraOriginal)) {
            
            String idioma = detectarIdioma(comandoOriginal);
            aprenderPalavra(idioma, primeiraOriginal, primeiraExecutado);
        }
    }
    
    public String getIdiomaAtual() {
        return this.idiomaDetectado;
    }
    
    public void listarIdiomasSuportados() {
        System.out.println("");
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║        IDIOMAS SUPORTADOS PELO TONACO SCRIPT             ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
        System.out.println("");
        
        for (String idioma : dicionarioGlobal.keySet()) {
            int numPalavras = dicionarioGlobal.get(idioma).size();
            System.out.println("  • " + idioma.substring(0, 1).toUpperCase() + idioma.substring(1) + " (" + numPalavras + " palavras)");
        }
        
        System.out.println("");
        System.out.println("  → O sistema aprende novas palavras automaticamente!");
        System.out.println("  → Programe em qualquer idioma, execute sem preocupação.");
        System.out.println("");
    }
    
    public String obterAjuda(String idioma) {
        Map<String, String> dict = dicionarioGlobal.getOrDefault(idioma, dicionarioGlobal.get("portugues"));
        
        StringBuilder ajuda = new StringBuilder();
        ajuda.append("\n");
        ajuda.append("╔══════════════════════════════════════════════════════════╗\n");
        ajuda.append("║              COMANDOS EM ").append(idioma.toUpperCase()).append("                  ║\n");
        ajuda.append("╚══════════════════════════════════════════════════════════╝\n");
        ajuda.append("\n");
        
        for (var entry : COMANDOS_PADRAO.entrySet()) {
            String comandoOriginal = entry.getKey();
            String comandoIngles = entry.getValue();
            String traducao = dict.getOrDefault(comandoOriginal, comandoOriginal);
            
            ajuda.append("  • ").append(traducao).append(" → ").append(comandoIngles).append("\n");
        }
        
        return ajuda.toString();
    }
}
