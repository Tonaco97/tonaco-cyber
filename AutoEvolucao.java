// AutoEvolucao.java
// Módulo de auto-evolução do TONACO SCRIPT
// Criado por: Guilherme Lucas Tonaco Carvalho

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.security.MessageDigest;
import com.google.gson.*;

public class AutoEvolucao {
    
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final String REPOSITORIO_COMANDOS = "https://raw.githubusercontent.com/Tonaco97/tonaco-comandos/main/";
    private static final String PASTA_COMANDOS = ".tonaco_comandos/";
    private static final String ARQUIVO_LOG = ".tonaco_log.json";
    
    private Map<String, ComandoPersonalizado> comandosCustomizados;
    private List<RegistroUso> historicoUsos;
    
    public AutoEvolucao() {
        this.comandosCustomizados = new HashMap<>();
        this.historicoUsos = new ArrayList<>();
        criarPastas();
        carregarComandosLocais();
        carregarHistorico();
    }
    
    private void criarPastas() {
        try {
            Files.createDirectories(Path.of(PASTA_COMANDOS));
        } catch (IOException e) {
            System.err.println("Erro ao criar pastas: " + e.getMessage());
        }
    }
    
    private void carregarComandosLocais() {
        try {
            Path arquivoComandos = Path.of(PASTA_COMANDOS + "comandos.json");
            if (Files.exists(arquivoComandos)) {
                String conteudo = Files.readString(arquivoComandos);
                JsonObject obj = gson.fromJson(conteudo, JsonObject.class);
                for (var entry : obj.entrySet()) {
                    ComandoPersonalizado cmd = gson.fromJson(entry.getValue().toString(), ComandoPersonalizado.class);
                    comandosCustomizados.put(entry.getKey(), cmd);
                }
                System.out.println("[AUTO] " + comandosCustomizados.size() + " comandos personalizados carregados.");
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar comandos: " + e.getMessage());
        }
    }
    
    private void carregarHistorico() {
        try {
            Path arquivoHistorico = Path.of(PASTA_COMANDOS + ARQUIVO_LOG);
            if (Files.exists(arquivoHistorico)) {
                String conteudo = Files.readString(arquivoHistorico);
                JsonArray arr = gson.fromJson(conteudo, JsonArray.class);
                for (var elem : arr) {
                    historicoUsos.add(gson.fromJson(elem, RegistroUso.class));
                }
            }
        } catch (Exception e) {
            // Arquivo não existe ainda
        }
    }
    
    public void registrarUso(String comando, boolean sucesso, long tempoMs) {
        RegistroUso registro = new RegistroUso();
        registro.comando = comando;
        registro.sucesso = sucesso;
        registro.tempoMs = tempoMs;
        registro.data = System.currentTimeMillis();
        registro.ipUsuario = obterIpPublico();
        
        historicoUsos.add(registro);
        
        // Mantém apenas os últimos 1000 registros
        if (historicoUsos.size() > 1000) {
            historicoUsos = historicoUsos.subList(historicoUsos.size() - 1000, historicoUsos.size());
        }
        
        salvarHistorico();
    }
    
    private void salvarHistorico() {
        try {
            String json = gson.toJson(historicoUsos);
            Files.writeString(Path.of(PASTA_COMANDOS + ARQUIVO_LOG), json);
        } catch (IOException e) {
            System.err.println("Erro ao salvar histórico: " + e.getMessage());
        }
    }
    
    private String obterIpPublico() {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL("https://api.ipify.org").openConnection();
            conn.setConnectTimeout(3000);
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String ip = br.readLine();
            br.close();
            return ip;
        } catch (Exception e) {
            return "desconhecido";
        }
    }
    
    public void sincronizarComandos() {
        System.out.println("[AUTO] Sincronizando comandos da comunidade...");
        
        String url = REPOSITORIO_COMANDOS + "comandos_populares.json";
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setConnectTimeout(5000);
            
            if (conn.getResponseCode() == 200) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String linha;
                while ((linha = br.readLine()) != null) {
                    sb.append(linha);
                }
                br.close();
                
                JsonObject comandosPopulares = gson.fromJson(sb.toString(), JsonObject.class);
                int novos = 0;
                
                for (var entry : comandosPopulares.entrySet()) {
                    if (!comandosCustomizados.containsKey(entry.getKey())) {
                        ComandoPersonalizado cmd = gson.fromJson(entry.getValue().toString(), ComandoPersonalizado.class);
                        comandosCustomizados.put(entry.getKey(), cmd);
                        novos++;
                    }
                }
                
                System.out.println("[AUTO] " + novos + " novos comandos disponíveis.");
                salvarComandos();
            } else {
                System.out.println("[AUTO] Servidor de comandos indisponível no momento.");
            }
        } catch (Exception e) {
            System.out.println("[AUTO] Erro na sincronização: " + e.getMessage());
        }
    }
    
    public void compartilharComando(String nome, String descricao, String acao) {
        ComandoPersonalizado cmd = new ComandoPersonalizado();
        cmd.nome = nome;
        cmd.descricao = descricao;
        cmd.acao = acao;
        cmd.criador = System.getProperty("user.name");
        cmd.dataCriacao = System.currentTimeMillis();
        cmd.hash = calcularHash(nome + acao);
        
        comandosCustomizados.put(nome, cmd);
        salvarComandos();
        
        System.out.println("[AUTO] Comando '" + nome + "' criado localmente.");
        System.out.println("[AUTO] Para compartilhar com a comunidade, use: compartilhar " + nome);
    }
    
    public void compartilharComComunidade(String nomeComando) {
        ComandoPersonalizado cmd = comandosCustomizados.get(nomeComando);
        if (cmd == null) {
            System.out.println("[AUTO] Comando '" + nomeComando + "' não encontrado.");
            return;
        }
        
        String urlEnvio = REPOSITORIO_COMANDOS + "api/enviar";
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(urlEnvio).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            
            String json = gson.toJson(cmd);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes());
                os.flush();
            }
            
            if (conn.getResponseCode() == 200) {
                System.out.println("[AUTO] Comando enviado para análise da comunidade.");
            } else {
                System.out.println("[AUTO] Erro ao enviar comando. Tente novamente mais tarde.");
            }
        } catch (Exception e) {
            System.out.println("[AUTO] Erro na conexão: " + e.getMessage());
        }
    }
    
    private void salvarComandos() {
        try {
            String json = gson.toJson(comandosCustomizados);
            Files.writeString(Path.of(PASTA_COMANDOS + "comandos.json"), json);
        } catch (IOException e) {
            System.err.println("Erro ao salvar comandos: " + e.getMessage());
        }
    }
    
    private String calcularHash(String entrada) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(entrada.getBytes());
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            return String.valueOf(entrada.hashCode());
        }
    }
    
    public String gerarRelatorioEvolucao() {
        Map<String, Object> relatorio = new LinkedHashMap<>();
        relatorio.put("versao", "1.1");
        relatorio.put("data_analise", new Date().toString());
        relatorio.put("total_comandos_custom", comandosCustomizados.size());
        relatorio.put("total_execucoes", historicoUsos.size());
        
        // Estatísticas de uso
        Map<String, Integer> comandosMaisUsados = new HashMap<>();
        Map<String, Integer> comandosMaisFalhos = new HashMap<>();
        
        for (RegistroUso uso : historicoUsos) {
            String cmd = uso.comando.split(" ")[0];
            comandosMaisUsados.put(cmd, comandosMaisUsados.getOrDefault(cmd, 0) + 1);
            if (!uso.sucesso) {
                comandosMaisFalhos.put(cmd, comandosMaisFalhos.getOrDefault(cmd, 0) + 1);
            }
        }
        
        relatorio.put("comandos_mais_usados", comandosMaisUsados);
        relatorio.put("comandos_mais_falhos", comandosMaisFalhos);
        
        // Sugestões automáticas
        List<String> sugestoes = new ArrayList<>();
        
        if (comandosCustomizados.size() < 5) {
            sugestoes.add("Crie mais comandos personalizados usando 'criar_comando'");
        }
        
        if (comandosMaisFalhos.containsKey("buscar") && comandosMaisFalhos.get("buscar") > 10) {
            sugestoes.add("O comando 'buscar' tem muitas falhas. Tente aumentar o timeout na config.tns");
        }
        
        if (historicoUsos.isEmpty()) {
            sugestoes.add("Execute mais comandos para gerar estatísticas úteis");
        }
        
        relatorio.put("sugestoes_auto", sugestoes);
        
        return gson.toJson(relatorio);
    }
    
    public void recomendarComandos() {
        System.out.println("[AUTO] Recomendações baseadas no seu uso:");
        System.out.println("");
        
        if (comandosCustomizados.isEmpty()) {
            System.out.println("  → Você ainda não criou comandos personalizados.");
            System.out.println("  → Use: criar_comando [nome] [descricao] [acao]");
            System.out.println("");
            System.out.println("  Exemplo:");
            System.out.println("  criar_comando ping Verifica se site está online analisar https://exemplo.com");
            return;
        }
        
        System.out.println("  Comandos disponíveis para você:");
        for (var entry : comandosCustomizados.entrySet()) {
            System.out.println("  → " + entry.getKey() + " - " + entry.getValue().descricao);
        }
    }
    
    public boolean executarComandoPersonalizado(String nome, String parametros) {
        ComandoPersonalizado cmd = comandosCustomizados.get(nome);
        if (cmd == null) {
            return false;
        }
        
        System.out.println("[AUTO] Executando comando personalizado: " + nome);
        System.out.println("  → " + cmd.descricao);
        System.out.println("");
        
        // Substitui placeholders
        String acao = cmd.acao.replace("{param}", parametros);
        
        // Executa a ação
        try {
            ProcessBuilder pb = new ProcessBuilder("sh", "-c", acao);
            pb.inheritIO();
            Process p = pb.start();
            int code = p.waitFor();
            return code == 0;
        } catch (Exception e) {
            System.err.println("Erro na execução: " + e.getMessage());
            return false;
        }
    }
    
    public void listarComandos() {
        System.out.println("");
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║        COMANDOS DISPONÍVEIS NO TONACO SCRIPT             ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
        System.out.println("");
        
        System.out.println("📦 COMANDOS NATIVOS:");
        System.out.println("  buscar, scanear, baixar, analisar, github, relatorio");
        System.out.println("  escrever, variavel, imprimir");
        System.out.println("");
        
        if (!comandosCustomizados.isEmpty()) {
            System.out.println("🔧 COMANDOS PERSONALIZADOS (" + comandosCustomizados.size() + "):");
            for (var entry : comandosCustomizados.entrySet()) {
                System.out.println("  → " + entry.getKey() + " - " + entry.getValue().descricao);
            }
            System.out.println("");
        }
        
        System.out.println("🔄 COMANDOS DE EVOLUÇÃO:");
        System.out.println("  sincronizar      → Baixa novos comandos da comunidade");
        System.out.println("  criar_comando    → Cria seu próprio comando");
        System.out.println("  compartilhar     → Envia comando para comunidade");
        System.out.println("  relatorio_evolucao → Gera estatísticas de uso");
        System.out.println("  recomendar       → Sugere comandos para você");
        System.out.println("");
    }
    
    // Classes internas
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
        String ipUsuario;
    }
}
