// TonacoScriptInterpreter.java
// Linguagem de programação TONACO SCRIPT (TNS) - Versão 1.0
// Criada por: Guilherme Lucas Tonaco Carvalho

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.*;
import com.google.gson.*;

public class TonacoScriptInterpreter {
    
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static Map<String, Object> variables = new HashMap<>();
    private static Map<String, String> config = new HashMap<>();
    private static List<String> output = new ArrayList<>();
    private static ExecutorService executor = Executors.newFixedThreadPool(10);
    
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║           TONACO SCRIPT (TNS) - Linguagem de Automação       ║");
        System.out.println("║                    Versão 1.0 - Criada em 2026               ║");
        System.out.println("║           Desenvolvedor: Guilherme Lucas Tonaco Carvalho     ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println();
        
        if (args.length == 0) {
            System.out.println("Uso: java TonacoScriptInterpreter arquivo.tns");
            System.out.println("     java TonacoScriptInterpreter --auto-buscar [url]");
            System.out.println("     java TonacoScriptInterpreter --github [repo]");
            return;
        }
        
        if (args[0].equals("--auto-buscar") && args.length > 1) {
            autoBuscarArquivos(args[1]);
        } else if (args[0].equals("--github") && args.length > 1) {
            integrarGitHub(args[1]);
        } else {
            interpretarArquivo(args[0]);
        }
        
        executor.shutdown();
    }
    
    // ============================================================
    // AUTO BUSCAR ARQUIVOS EM SITES
    // ============================================================
    private static void autoBuscarArquivos(String url) {
        System.out.println("🔍 TONACO SCANNER - Buscando arquivos em: " + url);
        System.out.println("━".repeat(60));
        
        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("url_alvo", url);
        resultado.put("data_scan", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        List<Map<String, Object>> arquivosEncontrados = new ArrayList<>();
        
        // Extensões para buscar
        String[] extensoes = {
            ".php", ".html", ".htm", ".jsp", ".asp", ".aspx", ".js", ".css",
            ".xml", ".json", ".txt", ".log", ".conf", ".config", ".ini",
            ".sql", ".bak", ".zip", ".tar", ".gz", ".rar", ".pdf", ".doc", ".xls",
            ".java", ".py", ".rb", ".go", ".c", ".cpp", ".h", ".class", ".jar",
            ".war", ".ear", ".properties", ".yml", ".yaml", ".md", ".markdown"
        };
        
        // Diretórios comuns para verificar
        String[] diretorios = {
            "", "/admin", "/backup", "/temp", "/tmp", "/logs", "/config", "/conf",
            "/WEB-INF", "/META-INF", "/resources", "/static", "/assets", "/public",
            "/api", "/v1", "/v2", "/old", "/archive", "/download", "/files",
            "/uploads", "/images", "/css", "/js", "/lib", "/include", "/inc"
        };
        
        // Palavras-chave para arquivos sensíveis
        String[] palavrasChave = {
            "password", "secret", "key", "token", "api", "auth", "login", "admin",
            "config", "database", "db", "backup", "dump", "credential", "private"
        };
        
        int totalEncontrados = 0;
        
        for (String dir : diretorios) {
            for (String ext : extensoes) {
                String urlTeste = url + dir + "/index" + ext;
                try {
                    HttpURLConnection conn = (HttpURLConnection) new URL(urlTeste).openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(2000);
                    conn.setReadTimeout(3000);
                    int code = conn.getResponseCode();
                    
                    if (code == 200) {
                        Map<String, Object> arquivo = new LinkedHashMap<>();
                        arquivo.put("url", urlTeste);
                        arquivo.put("status", code);
                        arquivo.put("tamanho", conn.getContentLength());
                        arquivo.put("tipo", conn.getContentType());
                        
                        // Verifica se contém palavras sensíveis
                        boolean isSensitive = false;
                        String conteudo = lerConteudoParcial(conn);
                        for (String kw : palavrasChave) {
                            if (conteudo.toLowerCase().contains(kw)) {
                                isSensitive = true;
                                arquivo.put("sensivel", true);
                                arquivo.put("palavra_encontrada", kw);
                                break;
                            }
                        }
                        
                        arquivosEncontrados.add(arquivo);
                        totalEncontrados++;
                        
                        String icone = isSensitive ? "⚠️" : "📄";
                        System.out.println(icone + " " + urlTeste + " (HTTP " + code + ")");
                    }
                } catch (Exception e) {
                    // Arquivo não encontrado, ignora
                }
            }
        }
        
        // Busca recursiva por links na página principal
        System.out.println("\n🔗 Buscando links na página principal...");
        Set<String> linksEncontrados = buscarLinksRecursivos(url, 2);
        
        for (String link : linksEncontrados) {
            Map<String, Object> arquivo = new LinkedHashMap<>();
            arquivo.put("url", link);
            arquivo.put("origem", "link_encontrado");
            arquivosEncontrados.add(arquivo);
            System.out.println("🔗 " + link);
        }
        
        resultado.put("total_arquivos", totalEncontrados);
        resultado.put("total_links", linksEncontrados.size());
        resultado.put("arquivos", arquivosEncontrados);
        
        // Gera relatório
        String relatorio = gson.toJson(resultado);
        String nomeArquivo = "scan_" + System.currentTimeMillis() + ".json";
        
        try {
            Files.writeString(Path.of(nomeArquivo), relatorio);
            System.out.println("\n✅ Relatório salvo em: " + nomeArquivo);
        } catch (IOException e) {
            System.out.println("❌ Erro ao salvar relatório: " + e.getMessage());
        }
        
        // Gera também um relatório em HTML
        gerarRelatorioHTML(resultado);
    }
    
    private static String lerConteudoParcial(HttpURLConnection conn) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder sb = new StringBuilder();
            char[] buffer = new char[8192];
            int lidos = br.read(buffer);
            if (lidos > 0) sb.append(buffer, 0, lidos);
            return sb.toString().toLowerCase();
        } catch (Exception e) {
            return "";
        }
    }
    
    private static Set<String> buscarLinksRecursivos(String url, int profundidade) {
        Set<String> links = new HashSet<>();
        if (profundidade <= 0) return links;
        
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(3000);
            
            String conteudo = lerConteudoCompleto(conn);
            if (conteudo == null) return links;
            
            // Busca por href e src
            Pattern pattern = Pattern.compile("(?:href|src)=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(conteudo);
            
            while (matcher.find()) {
                String link = matcher.group(1);
                if (link.startsWith("http")) {
                    links.add(link);
                } else if (link.startsWith("/")) {
                    links.add(extrairDominio(url) + link);
                }
            }
        } catch (Exception e) {
            // Ignora erros
        }
        
        return links;
    }
    
    private static String lerConteudoCompleto(HttpURLConnection conn) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder sb = new StringBuilder();
            String linha;
            while ((linha = br.readLine()) != null) {
                sb.append(linha);
            }
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }
    
    private static String extrairDominio(String url) {
        try {
            URL u = new URL(url);
            return u.getProtocol() + "://" + u.getHost();
        } catch (Exception e) {
            return url;
        }
    }
    
    private static void gerarRelatorioHTML(Map<String, Object> resultado) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang='pt-br'>\n");
        html.append("<head>\n");
        html.append("<meta charset='UTF-8'>\n");
        html.append("<title>TONACO SCANNER - Relatório</title>\n");
        html.append("<style>\n");
        html.append("body { font-family: monospace; background: #0a0a0a; color: #00ff88; padding: 20px; }\n");
        html.append(".container { max-width: 1200px; margin: 0 auto; }\n");
        html.append("h1 { color: #00ff88; border-bottom: 1px solid #00ff88; }\n");
        html.append(".file { background: #1a1a2e; margin: 10px 0; padding: 10px; border-radius: 5px; }\n");
        html.append(".sensitive { border-left: 4px solid #ff4444; }\n");
        html.append(".url { color: #ffaa00; }\n");
        html.append("</style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("<div class='container'>\n");
        html.append("<h1>🔍 TONACO SCANNER - Relatório de Busca</h1>\n");
        html.append("<p><strong>URL Alvo:</strong> ").append(resultado.get("url_alvo")).append("</p>\n");
        html.append("<p><strong>Data:</strong> ").append(resultado.get("data_scan")).append("</p>\n");
        html.append("<p><strong>Total de Arquivos:</strong> ").append(resultado.get("total_arquivos")).append("</p>\n");
        html.append("<hr>\n");
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> arquivos = (List<Map<String, Object>>) resultado.get("arquivos");
        if (arquivos != null) {
            for (Map<String, Object> arq : arquivos) {
                String classe = arq.containsKey("sensivel") ? "file sensitive" : "file";
                html.append("<div class='").append(classe).append("'>\n");
                html.append("  <span class='url'>📄 ").append(arq.get("url")).append("</span>\n");
                if (arq.containsKey("status")) {
                    html.append("  <span> - HTTP ").append(arq.get("status")).append("</span>\n");
                }
                if (arq.containsKey("sensivel")) {
                    html.append("  <span style='color:#ff4444;'> ⚠️ CONTEÚDO SENSÍVEL DETECTADO!</span>\n");
                }
                html.append("</div>\n");
            }
        }
        
        html.append("</div>\n");
        html.append("</body>\n");
        html.append("</html>\n");
        
        try {
            Files.writeString(Path.of("relatorio_" + System.currentTimeMillis() + ".html"), html.toString());
            System.out.println("✅ Relatório HTML gerado!");
        } catch (IOException e) {
            System.out.println("❌ Erro ao gerar HTML: " + e.getMessage());
        }
    }
    
    // ============================================================
    // INTEGRAÇÃO COM GITHUB
    // ============================================================
    private static void integrarGitHub(String repoUrl) {
        System.out.println("📦 TONACO GITHUB INTEGRATOR");
        System.out.println("━".repeat(60));
        System.out.println("Repositório: " + repoUrl);
        
        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("repositorio", repoUrl);
        resultado.put("data_analise", LocalDateTime.now().toString());
        
        List<Map<String, String>> arquivos = new ArrayList<>();
        
        // Extrai nome do repo da URL
        String repoName = repoUrl.replace("https://github.com/", "").replace(".git", "");
        
        // API do GitHub para listar conteúdo
        String apiUrl = "https://api.github.com/repos/" + repoName + "/contents/";
        
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
            conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
            conn.setRequestProperty("User-Agent", "TONACO-SCRIPT");
            
            if (conn.getResponseCode() == 200) {
                String json = lerConteudoCompleto(conn);
                JsonArray items = gson.fromJson(json, JsonArray.class);
                
                for (JsonElement item : items) {
                    JsonObject obj = item.getAsJsonObject();
                    Map<String, String> arquivo = new LinkedHashMap<>();
                    arquivo.put("nome", obj.get("name").getAsString());
                    arquivo.put("tipo", obj.get("type").getAsString());
                    arquivo.put("url", obj.get("html_url").getAsString());
                    arquivos.add(arquivo);
                }
                
                resultado.put("total_arquivos", arquivos.size());
                resultado.put("arquivos", arquivos);
                
                System.out.println("\n📁 Arquivos encontrados no repositório:");
                for (Map<String, String> arq : arquivos) {
                    System.out.println("   📄 " + arq.get("nome") + " (" + arq.get("tipo") + ")");
                }
                
                // Gera arquivo de configuração para upload
                gerarScriptUpload(repoName, arquivos);
                
            } else {
                System.out.println("❌ Erro ao acessar GitHub API: HTTP " + conn.getResponseCode());
            }
        } catch (Exception e) {
            System.out.println("❌ Erro: " + e.getMessage());
        }
        
        // Salva relatório
        try {
            Files.writeString(Path.of("github_analise.json"), gson.toJson(resultado));
            System.out.println("\n✅ Relatório salvo em: github_analise.json");
        } catch (IOException e) {
            System.out.println("❌ Erro ao salvar: " + e.getMessage());
        }
    }
    
    private static void gerarScriptUpload(String repoName, List<Map<String, String>> arquivos) {
        StringBuilder script = new StringBuilder();
        script.append("#!/bin/bash\n");
        script.append("# Script gerado pelo TONACO SCRIPT para upload ao GitHub\n");
        script.append("# Repositório: ").append(repoName).append("\n");
        script.append("\n");
        script.append("echo \"🚀 Iniciando upload para GitHub...\"\n");
        script.append("\n");
        script.append("# Inicializar repositório\n");
        script.append("git init\n");
        script.append("\n");
        script.append("# Adicionar todos os arquivos\n");
        script.append("git add .\n");
        script.append("\n");
        script.append("# Commit\n");
        script.append("git commit -m \"feat: TONACO CYBER - Plataforma completa em Java\"\n");
        script.append("\n");
        script.append("# Adicionar remote\n");
        script.append("git remote add origin https://github.com/").append(repoName).append(".git\n");
        script.append("\n");
        script.append("# Enviar\n");
        script.append("git push -u origin main\n");
        script.append("\n");
        script.append("echo \"✅ Upload concluído!\"\n");
        
        try {
            Files.writeString(Path.of("upload_github.sh"), script.toString());
            System.out.println("✅ Script de upload gerado: upload_github.sh");
            System.out.println("   Execute: chmod +x upload_github.sh && ./upload_github.sh");
        } catch (IOException e) {
            System.out.println("❌ Erro ao gerar script: " + e.getMessage());
        }
    }
    
    // ============================================================
    // INTERPRETADOR DA LINGUAGEM TNS
    // ============================================================
    private static void interpretarArquivo(String arquivo) {
        System.out.println("📜 Interpretando arquivo: " + arquivo);
        System.out.println("━".repeat(60));
        
        try {
            List<String> linhas = Files.readAllLines(Path.of(arquivo));
            int linhaNum = 0;
            
            for (String linha : linhas) {
                linhaNum++;
                linha = linha.trim();
                
                if (linha.isEmpty() || linha.startsWith("//") || linha.startsWith("#")) {
                    continue;
                }
                
                executarComando(linha, linhaNum);
            }
            
        } catch (IOException e) {
            System.out.println("❌ Erro ao ler arquivo: " + e.getMessage());
        }
        
        System.out.println("\n✅ Execução concluída!");
    }
    
    private static void executarComando(String comando, int linhaNum) {
        try {
            // Comando: buscar [url]
            if (comando.startsWith("buscar ")) {
                String url = comando.substring(7).trim();
                autoBuscarArquivos(url);
            }
            
            // Comando: github [repo]
            else if (comando.startsWith("github ")) {
                String repo = comando.substring(7).trim();
                integrarGitHub(repo);
            }
            
            // Comando: escrever [texto]
            else if (comando.startsWith("escrever ")) {
                String texto = comando.substring(9).trim();
                System.out.println(texto);
                output.add(texto);
            }
            
            // Comando: variavel [nome] = [valor]
            else if (comando.contains("=")) {
                String[] partes = comando.split("=", 2);
                String nomeVar = partes[0].trim();
                String valor = partes[1].trim();
                
                // Remove aspas se tiver
                if (valor.startsWith("\"") && valor.endsWith("\"")) {
                    valor = valor.substring(1, valor.length() - 1);
                }
                variables.put(nomeVar, valor);
            }
            
            // Comando: imprimir [variavel]
            else if (comando.startsWith("imprimir ")) {
                String var = comando.substring(9).trim();
                if (variables.containsKey(var)) {
                    System.out.println(variables.get(var));
                } else {
                    System.out.println(var);
                }
            }
            
            // Comando: scanear [url] --profundidade [n]
            else if (comando.startsWith("scanear ")) {
                String resto = comando.substring(8).trim();
                String[] partes = resto.split("--profundidade");
                String url = partes[0].trim();
                int profundidade = partes.length > 1 ? Integer.parseInt(partes[1].trim()) : 2;
                
                System.out.println("🔍 Escaneando " + url + " com profundidade " + profundidade);
                Set<String> links = buscarLinksRecursivos(url, profundidade);
                for (String link : links) {
                    System.out.println("   🔗 " + link);
                }
            }
            
            // Comando: baixar [url] [destino]
            else if (comando.startsWith("baixar ")) {
                String resto = comando.substring(7).trim();
                String[] partes = resto.split(" ");
                String url = partes[0];
                String destino = partes.length > 1 ? partes[1] : "download_" + System.currentTimeMillis();
                
                try (InputStream in = new URL(url).openStream()) {
                    Files.copy(in, Path.of(destino), StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("✅ Baixado: " + url + " -> " + destino);
                } catch (Exception e) {
                    System.out.println("❌ Erro ao baixar: " + e.getMessage());
                }
            }
            
            // Comando: analisar [url]
            else if (comando.startsWith("analisar ")) {
                String url = comando.substring(9).trim();
                analisarSite(url);
            }
            
            // Comando: relatorio [formato]
            else if (comando.startsWith("relatorio ")) {
                String formato = comando.substring(10).trim();
                gerarRelatorio(formato);
            }
            
            else {
                System.out.println("⚠️ Linha " + linhaNum + ": Comando desconhecido: " + comando);
            }
            
        } catch (Exception e) {
            System.out.println("❌ Erro na linha " + linhaNum + ": " + e.getMessage());
        }
    }
    
    private static void analisarSite(String url) {
        System.out.println("🔍 Analisando site: " + url);
        
        Map<String, Object> analise = new LinkedHashMap<>();
        analise.put("url", url);
        analise.put("data", LocalDateTime.now().toString());
        
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            int code = conn.getResponseCode();
            analise.put("status_code", code);
            analise.put("content_type", conn.getContentType());
            analise.put("content_length", conn.getContentLength());
            
            // Headers de segurança
            Map<String, String> headers = new LinkedHashMap<>();
            String[] securityHeaders = {"Strict-Transport-Security", "X-Content-Type-Options", 
                                       "X-Frame-Options", "Content-Security-Policy"};
            for (String h : securityHeaders) {
                String val = conn.getHeaderField(h);
                headers.put(h, val != null ? "✅" : "❌");
            }
            analise.put("security_headers", headers);
            
            // Server info
            String server = conn.getHeaderField("Server");
            if (server != null) analise.put("server", server);
            
            System.out.println(gson.toJson(analise));
            
        } catch (Exception e) {
            System.out.println("❌ Erro na análise: " + e.getMessage());
        }
    }
    
    private static void gerarRelatorio(String formato) {
        Map<String, Object> relatorio = new LinkedHashMap<>();
        relatorio.put("data_execucao", LocalDateTime.now().toString());
        relatorio.put("variaveis", variables);
        relatorio.put("output", output);
        relatorio.put("total_comandos", output.size());
        
        if (formato.equals("json")) {
            try {
                Files.writeString(Path.of("relatorio_final.json"), gson.toJson(relatorio));
                System.out.println("✅ Relatório JSON gerado: relatorio_final.json");
            } catch (IOException e) {
                System.out.println("❌ Erro: " + e.getMessage());
            }
        } else if (formato.equals("html")) {
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html><html><head><title>Relatório TNS</title>");
            html.append("<style>body{background:#0a0a0a;color:#00ff88;font-family:monospace;padding:20px;}</style>");
            html.append("</head><body><h1>📊 RELATÓRIO TONACO SCRIPT</h1>");
            html.append("<p>Data: ").append(LocalDateTime.now()).append("</p>");
            html.append("<h2>Variáveis</h2><pre>").append(gson.toJson(variables)).append("</pre>");
            html.append("<h2>Output</h2><pre>").append(String.join("\n", output)).append("</pre>");
            html.append("</body></html>");
            
            try {
                Files.writeString(Path.of("relatorio_final.html"), html.toString());
                System.out.println("✅ Relatório HTML gerado: relatorio_final.html");
            } catch (IOException e) {
                System.out.println("❌ Erro: " + e.getMessage());
            }
        }
    }
}