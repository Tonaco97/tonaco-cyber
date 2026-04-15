// AutoEvolucao.java
// Módulo de auto-evolução do TONACO SCRIPT v2.0
// Permite criar, compartilhar e sincronizar comandos personalizados da comunidade
// Criado por: Guilherme Lucas Tonaco Carvalho

package com.tonaco.tns;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.security.MessageDigest;
import com.google.gson.*;

public class AutoEvolucao {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String REPO_COMANDOS = "https://raw.githubusercontent.com/Tonaco97/tonaco-comandos/main/";
    private static final String DIR_COMANDOS  = ".tonaco_comandos/";
    private static final String ARQ_COMANDOS  = DIR_COMANDOS + "comandos.json";
    private static final String ARQ_LOG       = DIR_COMANDOS + "uso.json";

    private Map<String, ComandoPersonalizado> comandos = new LinkedHashMap<>();
    private List<RegistroUso> historico = new ArrayList<>();

    public AutoEvolucao() {
        criarDiretorios();
        carregarComandos();
        carregarHistorico();
    }

    // ============================================================
    // INICIALIZAÇÃO
    // ============================================================

    private void criarDiretorios() {
        try { Files.createDirectories(Path.of(DIR_COMANDOS)); }
        catch (IOException e) { System.err.println("[AutoEvolucao] Erro ao criar diretórios: " + e.getMessage()); }
    }

    private void carregarComandos() {
        try {
            Path p = Path.of(ARQ_COMANDOS);
            if (!Files.exists(p)) return;
            JsonObject obj = GSON.fromJson(Files.readString(p), JsonObject.class);
            for (var entry : obj.entrySet()) {
                ComandoPersonalizado cmd = GSON.fromJson(entry.getValue(), ComandoPersonalizado.class);
                comandos.put(entry.getKey(), cmd);
            }
            System.out.printf("[AutoEvolucao] %d comandos personalizados carregados.%n", comandos.size());
        } catch (Exception e) {
            System.err.println("[AutoEvolucao] Aviso: não foi possível carregar comandos: " + e.getMessage());
        }
    }

    private void carregarHistorico() {
        try {
            Path p = Path.of(ARQ_LOG);
            if (!Files.exists(p)) return;
            JsonArray arr = GSON.fromJson(Files.readString(p), JsonArray.class);
            for (var el : arr) {
                historico.add(GSON.fromJson(el, RegistroUso.class));
            }
        } catch (Exception ignored) {}
    }

    // ============================================================
    // CRIAÇÃO E COMPARTILHAMENTO
    // ============================================================

    public void criarComando(String nome, String descricao, String acao) {
        if (nome == null || nome.isBlank()) {
            System.err.println("[AutoEvolucao] Nome do comando não pode ser vazio.");
            return;
        }
        ComandoPersonalizado cmd = new ComandoPersonalizado();
        cmd.nome        = nome;
        cmd.descricao   = descricao;
        cmd.acao        = acao;
        cmd.criador     = System.getProperty("user.name", "tonaco-user");
        cmd.dataCriacao = System.currentTimeMillis();
        cmd.hash        = sha256(nome + acao + cmd.dataCriacao);

        comandos.put(nome, cmd);
        salvarComandos();

        System.out.printf("[AutoEvolucao] Comando '%s' criado com sucesso.%n", nome);
    }

    public void compartilharComando(String nome) {
        ComandoPersonalizado cmd = comandos.get(nome);
        if (cmd == null) {
            System.err.println("[AutoEvolucao] Comando '" + nome + "' não encontrado.");
            return;
        }
        // Simulação de upload — em produção enviaria para API
        System.out.printf("[AutoEvolucao] Compartilhando '%s' com a comunidade...%n", nome);
        System.out.println("  Hash: " + cmd.hash);
        System.out.println("  (Funcionalidade de upload requer endpoint de API configurado)");
    }

    // ============================================================
    // SINCRONIZAÇÃO
    // ============================================================

    public void sincronizar() {
        System.out.println("[AutoEvolucao] Sincronizando com repositório da comunidade...");
        try {
            URL url = new URL(REPO_COMANDOS + "comandos_iniciais.json");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestProperty("User-Agent", "TONACO-SCRIPT/2.0");

            if (conn.getResponseCode() == 200) {
                String json = new String(conn.getInputStream().readAllBytes());
                JsonObject obj = GSON.fromJson(json, JsonObject.class);
                int novos = 0;
                for (var entry : obj.entrySet()) {
                    if (!comandos.containsKey(entry.getKey())) {
                        ComandoPersonalizado cmd = GSON.fromJson(entry.getValue(), ComandoPersonalizado.class);
                        comandos.put(entry.getKey(), cmd);
                        novos++;
                    }
                }
                salvarComandos();
                System.out.printf("[AutoEvolucao] %d novos comandos adicionados.%n", novos);
            } else {
                System.out.println("[AutoEvolucao] Repositório indisponível (HTTP " + conn.getResponseCode() + ")");
            }
        } catch (Exception e) {
            System.out.println("[AutoEvolucao] Sem conexão com repositório: " + e.getMessage());
        }
    }

    // ============================================================
    // EXECUÇÃO
    // ============================================================

    public boolean executar(String nome, String parametro) {
        ComandoPersonalizado cmd = comandos.get(nome);
        if (cmd == null) return false;

        System.out.printf("[AutoEvolucao] Executando '%s': %s%n", nome, cmd.descricao);
        String acao = cmd.acao.replace("{param}", parametro != null ? parametro : "");

        long inicio = System.currentTimeMillis();
        boolean sucesso = false;
        try {
            ProcessBuilder pb = new ProcessBuilder("sh", "-c", acao);
            pb.inheritIO();
            sucesso = pb.start().waitFor() == 0;
        } catch (Exception e) {
            System.err.println("[AutoEvolucao] Erro na execução: " + e.getMessage());
        }

        registrarUso(nome, sucesso, System.currentTimeMillis() - inicio);
        return sucesso;
    }

    // ============================================================
    // RELATÓRIO / LISTAGEM
    // ============================================================

    public void listarComandos() {
        System.out.println("\n╔══════════════════════════════════════════════════════════╗");
        System.out.println("║         COMANDOS DISPONÍVEIS NO TONACO SCRIPT v2.0       ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝\n");

        System.out.println("NATIVOS:");
        System.out.println("  scan, deepscan, analyze, download, github, report, sync, share");
        System.out.println("  print, let, if, else, while, for, function, return\n");

        System.out.println("ALIASES v1 (compatibilidade):");
        System.out.println("  escrever=print, variavel=let, buscar=scan, scanear=deepscan");
        System.out.println("  analisar=analyze, baixar=download, relatorio=report, sincronizar=sync\n");

        if (!comandos.isEmpty()) {
            System.out.printf("PERSONALIZADOS (%d):%n", comandos.size());
            comandos.forEach((k, v) -> System.out.printf("  %-20s → %s%n", k, v.descricao));
            System.out.println();
        }

        System.out.println("AUTOEVOLUÇÃO:");
        System.out.println("  sincronizar             → Baixa comandos da comunidade");
        System.out.println("  criar_comando <n> <d> <a> → Cria comando personalizado");
        System.out.println("  compartilhar <nome>     → Envia para comunidade");
    }

    public String gerarRelatorioEvolucao() {
        Map<String, Object> rel = new LinkedHashMap<>();
        rel.put("total_comandos", comandos.size());
        rel.put("total_usos", historico.size());

        Map<String, Long> usosPorComando = new LinkedHashMap<>();
        for (RegistroUso r : historico) {
            usosPorComando.merge(r.comando, 1L, Long::sum);
        }
        rel.put("usos_por_comando", usosPorComando);

        long sucessos = historico.stream().filter(r -> r.sucesso).count();
        rel.put("taxa_sucesso", historico.isEmpty() ? 1.0 : (double) sucessos / historico.size());

        // Top 3 mais usados
        List<String> top = usosPorComando.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(3)
            .map(Map.Entry::getKey)
            .toList();
        rel.put("top_comandos", top);

        return GSON.toJson(rel);
    }

    // ============================================================
    // PERSISTÊNCIA
    // ============================================================

    private void salvarComandos() {
        try {
            JsonObject obj = new JsonObject();
            for (var entry : comandos.entrySet()) {
                obj.add(entry.getKey(), GSON.toJsonTree(entry.getValue()));
            }
            Files.writeString(Path.of(ARQ_COMANDOS), GSON.toJson(obj));
        } catch (IOException e) {
            System.err.println("[AutoEvolucao] Erro ao salvar comandos: " + e.getMessage());
        }
    }

    private void registrarUso(String nome, boolean sucesso, long tempoMs) {
        RegistroUso r = new RegistroUso();
        r.comando = nome;
        r.sucesso = sucesso;
        r.tempoMs = tempoMs;
        r.data    = System.currentTimeMillis();
        historico.add(r);

        try {
            Files.writeString(Path.of(ARQ_LOG), GSON.toJson(historico));
        } catch (IOException ignored) {}
    }

    // ============================================================
    // UTILITÁRIOS
    // ============================================================

    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.substring(0, 16);
        } catch (Exception e) { return Long.toHexString(input.hashCode()); }
    }

    // ============================================================
    // CLASSES INTERNAS
    // ============================================================

    private static class ComandoPersonalizado {
        String nome;
        String descricao;
        String acao;
        String criador;
        long dataCriacao;
        String hash;
        int votos = 0;
        int downloads = 0;
    }

    private static class RegistroUso {
        String comando;
        boolean sucesso;
        long tempoMs;
        long data;
    }
}
