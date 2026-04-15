# TONACO SCRIPT (TNS)

## Linguagem de Programacao para Automacao Web e Seguranca Cibernetica

**Versao:** 1.0  
**Criador:** Guilherme Lucas Tonaco Carvalho  
**Plataforma:** TONACO CYBER  
**Ano:** 2026  

---

## Sobre o Projeto

TONACO SCRIPT e uma linguagem de programacao original desenvolvida inteiramente em Java puro. Diferente de outras ferramentas, esta linguagem foi criada do zero sem frameworks ou bibliotecas externas complexas, com excecao do Gson para manipulacao de JSON.

A linguagem foi projetada para automatizar tarefas de reconhecimento web, busca de arquivos em servidores, analise de seguranca e integracao com o GitHub.

---

## Caracteristicas Principais

- Busca automatica de arquivos em sites alvo
- Scanner recursivo de links e subdiretorios
- Deteccao de conteudo sensivel (senhas, tokens, chaves)
- Analise de headers de seguranca HTTP
- Integracao com API do GitHub
- Geracao de relatorios em JSON e HTML
- Suporte a scripts com sintaxe propria
- Execucao em modo console ou via arquivos .tns

---

## Estrutura do Projeto

```
tonaco-cyber/
├── TonacoScriptInterpreter.java   # Interpretador e maquina virtual da linguagem
├── script.tns                      # Arquivo de exemplo com comandos TNS
├── config.tns                      # Configuracoes padrao da linguagem
└── README.md                       # Documentacao completa
```

---

## Requisitos para Execucao

- Java Development Kit 17 ou superior
- Biblioteca Gson 2.10.1 (unica dependencia externa)

---

## Instalacao e Configuracao

### Passo 1: Verificar Java

```
java -version
```

O retorno deve indicar Java 17 ou superior.

### Passo 2: Baixar a Biblioteca Gson

```
wget https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar
```

Caso nao tenha o wget disponivel, faca o download manual pelo navegador no endereco acima.

### Passo 3: Compilar o Interpretador

```
javac -cp "gson-2.10.1.jar:." TonacoScriptInterpreter.java
```

Para Windows, utilize ponto e virgula como separador:

```
javac -cp "gson-2.10.1.jar;." TonacoScriptInterpreter.java
```

### Passo 4: Executar

```
java -cp "gson-2.10.1.jar:." TonacoScriptInterpreter
```

---

## Modos de Execucao

### Modo 1: Executar um Script TNS

```
java -cp "gson-2.10.1.jar:." TonacoScriptInterpreter script.tns
```

### Modo 2: Buscar Arquivos em um Site

```
java -cp "gson-2.10.1.jar:." TonacoScriptInterpreter --auto-buscar https://exemplo.com.br
```

### Modo 3: Integrar com Repositorio GitHub

```
java -cp "gson-2.10.1.jar:." TonacoScriptInterpreter --github https://github.com/usuario/repositorio
```

---

## Sintaxe da Linguagem TNS

A linguagem TNS utiliza comandos em portugues com estrutura simples.

### Comando ESCREVER

Exibe texto no console.

```
escrever "Texto a ser exibido"
```

### Comando VARIAVEL

Declara uma variavel com um valor.

```
variavel nome = "Guilherme"
variavel idade = 28
variavel ativo = verdadeiro
```

### Comando IMPRIMIR

Exibe o valor de uma variavel.

```
imprimir nome
```

### Comando BUSCAR

Inicia o scanner de arquivos em um site alvo.

```
buscar https://exemplo.com.br
```

### Comando SCANEAR

Busca links recursivamente ate uma profundidade especifica.

```
scanear https://exemplo.com.br --profundidade 3
```

### Comando BAIXAR

Faz o download de um arquivo remoto.

```
baixar https://exemplo.com.br/arquivo.zip ./downloads/
```

### Comando ANALISAR

Verifica headers de seguranca e informacoes do servidor.

```
analisar https://exemplo.com.br
```

### Comando GITHUB

Lista arquivos de um repositorio publico no GitHub.

```
github https://github.com/usuario/repositorio
```

### Comando RELATORIO

Gera um relatorio da execucao.

```
relatorio json
relatorio html
```

---

## Exemplo Completo de Script

Crie um arquivo com extensao .tns e o seguinte conteudo:

```
escrever "Iniciando TONACO SCRIPT"

variavel alvo = "https://exemplo.com.br"
variavel profundidade = 2

escrever "Alvo definido: " + alvo

buscar alvo

scanear alvo --profundidade 2

analisar alvo

escrever "Finalizando execucao"

relatorio html
```

Para executar:

```
java -cp "gson-2.10.1.jar:." TonacoScriptInterpreter meu_script.tns
```

---

## Funcionamento do Scanner de Arquivos

Quando o comando BUSCAR e executado, o interpretador realiza as seguintes acoes:

1. Conecta-se ao dominio informado
2. Testa combinacoes de diretorios e extensoes
3. Identifica arquivos existentes (HTTP 200)
4. Verifica se o conteudo contem palavras sensiveis
5. Busca links na pagina principal recursivamente
6. Gera relatorio com todos os achados

### Extensoes Verificadas

.php, .jsp, .asp, .aspx, .html, .htm, .js, .css, .xml, .json, .txt, .log, .sql, .bak, .zip, .tar, .gz, .java, .py, .rb, .go, .c, .cpp, .class, .jar, .war, .ear, .properties, .yml, .yaml, .md

### Diretorios Verificados

/, /admin, /backup, /temp, /logs, /config, /WEB-INF, /api, /v1, /old, /download, /files, /uploads, /images, /css, /js, /lib, /include, /inc

### Palavras Sensiveis Detectadas

password, secret, key, token, api_key, database, db, backup, dump, credential, private, auth, login

---

## Exemplo de Saida

```
TONACO SCANNER - Buscando arquivos em: https://exemplo.com.br
----------------------------------------------------------------

https://exemplo.com.br/index.php (HTTP 200)
https://exemplo.com.br/admin/config.php (HTTP 200) - CONTEUDO SENSIVEL DETECTADO
https://exemplo.com.br/backup/database.sql (HTTP 200)
https://exemplo.com.br/api/v1/users (HTTP 200)

Total de arquivos encontrados: 4
Total de links encontrados: 12

Relatorio salvo em: scan_1702684800000.json
Relatorio HTML gerado: relatorio_1702684800000.html
```

---

## Estrutura do Relatorio JSON

```json
{
  "url_alvo": "https://exemplo.com.br",
  "data_scan": "2026-01-15T10:30:00",
  "total_arquivos": 4,
  "total_links": 12,
  "arquivos": [
    {
      "url": "https://exemplo.com.br/index.php",
      "status": 200,
      "tamanho": 15234
    },
    {
      "url": "https://exemplo.com.br/admin/config.php",
      "status": 200,
      "sensivel": true,
      "palavra_encontrada": "password"
    }
  ]
}
```

---

## Arquivo de Configuracao (config.tns)

O arquivo config.tns permite personalizar o comportamento da linguagem:

```
config timeout = 5000
config max_threads = 20
config user_agent = "TONACO-SCRIPT/1.0"

extensoes = [".php", ".jsp", ".html", ".js", ".css"]

diretorios = ["", "/admin", "/backup", "/api"]

palavras_sensiveis = ["password", "secret", "token", "key"]
```

---

## Limitacoes Conhecidas

1. O scanner depende da resposta HTTP do servidor alvo
2. Sites com bloqueio por User-Agent podem nao responder
3. O tempo de execucao aumenta com a profundidade do scan
4. O estado das variaveis e resetado a cada execucao
5. Nao ha suporte nativo a banco de dados

---

## Consideracoes Legais e Eticas

Esta ferramenta foi desenvolvida para fins educacionais e de seguranca autorizada.

O uso do TONACO SCRIPT em sistemas sem autorizacao expressa do proprietario pode violar leis locais e internacionais.

O criador nao se responsabiliza pelo uso indevido da ferramenta.

Sempre obtenha autorizacao por escrito antes de realizar qualquer teste de seguranca em sistemas de terceiros.

---

## Historico de Versoes

**Versao 1.0 (2026)**
- Lancamento inicial da linguagem
- Scanner basico de arquivos
- Suporte a scripts .tns
- Integracao com GitHub API
- Geracao de relatorios JSON e HTML

---

## Contato e Suporte

**Criador:** Guilherme Lucas Tonaco Carvalho  
**Plataforma:** TONACO CYBER  

---

## Licenca

MIT License

Copyright (c) 2026 Guilherme Lucas Tonaco Carvalho

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files, to deal in the Software without restriction.

---

## Agradecimentos

A comunidade de seguranca da informacao que compartilha conhecimento diariamente.  
Aos desenvolvedores de software livre que mantem o ecossistema Java vivo.

---

## Compartilhamento

Se este projeto foi util para seus estudos, considere compartilhar com outros profissionais da area.

---

**TONACO SCRIPT**  
Uma linguagem criada do zero para resolver problemas reais de automacao e seguranca.
