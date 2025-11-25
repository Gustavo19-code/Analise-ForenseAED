package br.edu.icev.aed.forense;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

public class MinhaAnaliseForense implements AnaliseForenseAvancada {

    @Override
    public Set<String> encontrarSessoesInvalidas(String arquivo) throws IOException {
        Set<String> sessoesInvalidas = new HashSet<>();
        // Mapa: Chave = UserID, Valor = Pilha de SessionIDs
        Map<String, Deque<String>> userStacks = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(arquivo))) {
            String linha;
            br.readLine();

            while ((linha = br.readLine()) != null) {
                String[] partes = linha.split(",");
                if (partes.length < 4) continue;

                String userId = partes[1];
                String sessionId = partes[2];
                String action = partes[3];

                Deque<String> stack = userStacks.computeIfAbsent(userId, k -> new ArrayDeque<>());

                if (action.equals("LOGIN")) {
                    // Se tentar LOGIN e a pilha não estiver vazia, é Login aninhado (inválido)
                    if (!stack.isEmpty()) {
                        sessoesInvalidas.add(sessionId);
                    }
                    // Sempre empilha o login para manter o estado
                    stack.push(sessionId);

                } else if (action.equals("LOGOUT")) {
                    // LOGOUT inválido se pilha vazia ou topo diferente da sessão atual
                    if (stack.isEmpty() || !stack.peek().equals(sessionId)) {
                        sessoesInvalidas.add(sessionId);
                    } else {
                        stack.pop(); // Logout válido
                    }
                }
            }
        }

        // Qualquer sessão que restou na pilha é inválida (não foi fechada)
        for (Deque<String> stack : userStacks.values()) {
            while (!stack.isEmpty()) {
                sessoesInvalidas.add(stack.pop());
            }
        }
        return sessoesInvalidas;
    }


    // --- Desafio 2: Reconstruir Linha do Tempo ---
    @Override
    public List<String> reconstruirLinhaTempo(String caminhoArquivoCsv, String sessionIdAlvo) throws IOException {
        // Usa Fila (Queue) conforme pedido no desafio
        Queue<String> filaAcoes = new LinkedList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(caminhoArquivoCsv))) {
            String linha;

            while ((linha = br.readLine()) != null) {
                String[] partes = linha.split(",");
                if (partes.length < 4) continue;

                String sessionIdAtual = partes[2];
                String action = partes[3];

                // Filtra apenas a sessão alvo
                if (sessionIdAtual.equals(sessionIdAlvo)) {
                    filaAcoes.add(action);
                }
            }
        }
        // Retorna a lista na ordem cronológica (FIFO)
        return new ArrayList<>(filaAcoes);
    }






    //Desafio 3.
    @Override
    public List<Alerta> priorizarAlertas(String arquivo, int i) throws IOException {
        if(i<=0){
            return Collections.emptyList();
        }

        PriorityQueue<Alerta> alertasSeveridade=new PriorityQueue<>(
                (a1,a2)->Integer.compare(a2.getSeverityLevel(), a1.getSeverityLevel())
        );

        try(BufferedReader leitor= new BufferedReader(new FileReader(arquivo))){
            leitor.readLine();
            String linha;
            linha=leitor.readLine();

            while(linha!=null){
                String[] dados = linha.split(",");

                if(dados.length<7)continue;
                long TIMESTAMP = Long.parseLong(dados[0].trim());
                String userId = dados[1].trim();
                String sessionId = dados[2].trim();
                String acao = dados[3].trim();
                String alvo = dados[4].trim();
                int severidade = Integer.parseInt(dados[5].trim());
                long transferidos = Long.parseLong(dados[6].trim());

                Alerta novoAlerta = new Alerta(TIMESTAMP,userId, sessionId ,acao,alvo,severidade,transferidos);
                alertasSeveridade.offer(novoAlerta);

            }

        }

        List<Alerta>resultados=new ArrayList<>();
        int contador= 0;

        while(!alertasSeveridade.isEmpty()&& contador<i){
            resultados.add(alertasSeveridade.poll());
            contador++;
        }

        return resultados;


    }

    //Desafio 4.
    @Override
    public Map<Long, Long> encontrarPicosTransferencia(String arquivo) throws IOException {
        List<Alertas> Evento= new ArrayList<>();

        try(BufferedReader leitor=new BufferedReader(new FileReader(arquivo))){
            String linha= leitor.readLine();

            while (leitor!=null){
                String[]conjuntos= linha.split(";");
                long timestamp=Long.parseLong(conjuntos[0]);
                long transferidos= Long.parseLong(conjuntos[6]);
                if(transferidos>0){
                    Evento.add(new Alertas(timestamp,transferidos));
                }

            }

        }

        Stack<Alertas> stack= new Stack<>();
        Map<Long,Long>resultados= new HashMap<>();

        for (int i=Evento.size()-1;i>=0;i--){
            Alertas a= Evento.get(i);

            //desempilha os alertas enquanto o topo for menor ou igual a quantidade de bytes trasferidos.
            while(!stack.isEmpty()&&stack.peek().transferidos<=a.transferidos){
                stack.pop();
            }

            if(!stack.isEmpty()){
                resultados.put(a.timestamp, stack.peek().timestamp);
            }

            stack.push(a);
        }


        return resultados;
    }


    // --- DESAFIO 5: RASTREAR CONTAMINAÇÃO (Grafo + BFS) ---
    @Override
    public Optional<List<String>> rastrearContaminacao(String caminhoArquivoCsv, String recursoInicial, String recursoAlvo) throws IOException {
        
        // 1. Caso base: Se início for igual ao alvo, retorna lista unitária (se existir no log)
        // O documento pede para verificar se o recurso existe, mas para simplificar a BFS,
        // vamos assumir que se ele foi passado e é igual, o caminho é ele mesmo.
        if (recursoInicial.equals(recursoAlvo)) {
            return Optional.of(Collections.singletonList(recursoInicial));
        }

        // 2. Construção do Grafo
        // Mapa: SessionID -> Lista de Ações (para reconstruir a ordem temporal de cada sessão)
        Map<String, List<LogEntry>> sessoes = new HashMap<>();
        
        // Leitura do arquivo para agrupar ações por sessão
        try (BufferedReader br = new BufferedReader(new FileReader(caminhoArquivoCsv))) {
            String linha;
            boolean header = true;
            while ((linha = br.readLine()) != null) {
                if (header) { header = false; continue; }
                LogEntry log = new LogEntry(linha);
                
                sessoes.putIfAbsent(log.getSessionId(), new ArrayList<>());
                sessoes.get(log.getSessionId()).add(log);
            }
        }

        // Lista de Adjacência: Recurso -> Conjunto de Recursos acessados imediatamente após
        // Usamos Set para evitar arestas duplicadas
        Map<String, Set<String>> grafo = new HashMap<>();

        for (List<LogEntry> acoesDaSessao : sessoes.values()) {
            // Garante ordenação por timestamp (embora o log geralmente já venha ordenado)
            acoesDaSessao.sort(Comparator.comparingLong(LogEntry::getTimestamp));

            for (int i = 0; i < acoesDaSessao.size() - 1; i++) {
                String origem = acoesDaSessao.get(i).getTargetResource();
                String destino = acoesDaSessao.get(i+1).getTargetResource();

                // Ignora se o recurso for o mesmo (auto-ciclo irrelevante para caminho mínimo)
                if (!origem.equals(destino)) {
                    grafo.putIfAbsent(origem, new HashSet<>());
                    grafo.get(origem).add(destino);
                }
            }
        }

        // 3. Execução do BFS (Busca em Largura)
        Queue<String> fila = new LinkedList<>();
        Map<String, String> predecessores = new HashMap<>(); // Para reconstruir o caminho (Filho -> Pai)
        Set<String> visitados = new HashSet<>();

        fila.add(recursoInicial);
        visitados.add(recursoInicial);
        boolean encontrou = false;

        while (!fila.isEmpty()) {
            String atual = fila.poll();

            if (atual.equals(recursoAlvo)) {
                encontrou = true;
                break;
            }

            // Se o nó atual tem vizinhos
            if (grafo.containsKey(atual)) {
                for (String vizinho : grafo.get(atual)) {
                    if (!visitados.contains(vizinho)) {
                        visitados.add(vizinho);
                        predecessores.put(vizinho, atual); // Mapeia de onde viemos
                        fila.add(vizinho);
                    }
                }
            }
        }

        // 4. Reconstrução do Caminho
        if (encontrou) {
            List<String> caminho = new ArrayList<>();
            String passo = recursoAlvo;
            
            // Backtracking do alvo até o início usando o mapa de predecessores
            while (passo != null) {
                caminho.add(passo);
                passo = predecessores.get(passo);
            }
            
            // O caminho foi montado de trás para frente, então invertemos
            Collections.reverse(caminho);
            return Optional.of(caminho);
        }

        return Optional.empty();
    }


    //Classe auxiliar para o desafio 4.
class Alertas{
    Long timestamp;
    long transferidos;


    public Alertas(long T, long t){
        this.timestamp=T;
        this.transferidos=t;
    }

}

//classe auxiliar desafio 2.
static class Logs{
        String sessionId;
        String acao;

        public Logs(String Id,String acao){
            this.sessionId=Id;
            this.acao=acao;
        }

    public String getSessionId() {
        return sessionId;
    }

    public String getAcao() {
        return acao;
    }
}

 static class session{
        long TIMESTAMP;
        String acao;
       int severidade;

        public session(long tm,String ac,int sv) {
            this.TIMESTAMP=tm;
            this.acao=ac;
            this.severidade=sv;

        }



    public int getSeveridade() {
        return severidade;
    }
}








}