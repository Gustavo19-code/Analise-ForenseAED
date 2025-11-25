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
    public List<Alerta> priorizarAlertas(String arquivo, int n) throws IOException {
        if (n <= 0) {
            return Collections.emptyList();
        }


        PriorityQueue<Alerta> alertasSeveridade = new PriorityQueue<>(

                (a1, a2) -> Integer.compare(a2.getSeverityLevel(), a1.getSeverityLevel())
        );

        try (BufferedReader leitor = new BufferedReader(new FileReader(arquivo))) {
            leitor.readLine();

            String linha;
            while ((linha = leitor.readLine()) != null) {
                String[] dados = linha.split(",");
                if (dados.length < 7) continue;

                try {
                    // Campos relevantes: TIMESTAMP(0), ACTION_TYPE(3), SEVERITY_LEVEL(5)
                    long TIMESTAMP = Long.parseLong(dados[0].trim());
                    String userId= dados[1].trim();
                    String sessionId=dados[2].trim();
                    String acao = dados[3].trim();
                    String  actionType=dados[4].trim();
                    int severidade = Integer.parseInt(dados[5].trim());
                    Long transferencia=Long.parseLong(dados[6].trim());


                    Alerta novoAlerta = new Alerta(TIMESTAMP,userId, sessionId,acao,actionType, severidade,transferencia);
                    alertasSeveridade.offer(novoAlerta);

                } catch (NumberFormatException ignored) {

                }
            }
        }

        List<Alerta> resultados = new ArrayList<>();
        int contador = 0;

        // Extrai os 'n' alertas mais severos (poll()) [cite: 184]
        while (!alertasSeveridade.isEmpty() && contador < n) {
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
                long TIMESTAMP = Long.parseLong(conjuntos[0].trim());
                String userId= conjuntos[1].trim();
                String sessionId=conjuntos[2].trim();
                String acao = conjuntos[3].trim();
                String  actionType=conjuntos[4].trim();
                int severidade = Integer.parseInt(conjuntos[5].trim());
                long transferencia=Long.parseLong(conjuntos[6].trim());

                if(transferencia>0){

                    Evento.add(new Alertas(TIMESTAMP,userId,sessionId,acao,actionType,severidade,transferencia));
                }

            }

        }

        Stack<Alertas> stack= new Stack<>();
        Map<Long,Long>resultados= new HashMap<>();

        for (int i=Evento.size()-1;i>=0;i--){
            Alertas a= Evento.get(i);


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


    // Desafio 5.
    @Override
    public Optional<List<String>> rastrearContaminacao(String caminhoArquivoCsv, String recursoInicial, String recursoAlvo) throws IOException {


        if (recursoInicial.equals(recursoAlvo)) {
            return Optional.of(Collections.singletonList(recursoInicial));
        }


        Map<String, List<LogEntry>> sessoes = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(caminhoArquivoCsv))) {
            br.readLine();
            String linha;
            while ((linha = br.readLine()) != null) {
                try {
                    LogEntry log = new LogEntry(linha);
                    sessoes.computeIfAbsent(log.getSessionId(), k -> new ArrayList<>()).add(log);
                } catch (Exception ignored) {

                }
            }
        }


        Map<String, Set<String>> grafo = new HashMap<>();

        for (List<LogEntry> acoesDaSessao : sessoes.values()) {

            acoesDaSessao.sort(Comparator.comparingLong(LogEntry::getTimestamp));


            for (int i = 0; i < acoesDaSessao.size() - 1; i++) {
                String origem = acoesDaSessao.get(i).getTargetResource();
                String destino = acoesDaSessao.get(i + 1).getTargetResource();

                if (!origem.equals(destino)) {
                    grafo.computeIfAbsent(origem, k -> new HashSet<>()).add(destino);
                }
            }
        }


        Queue<String> fila = new LinkedList<>();
        Map<String, String> predecessores = new HashMap<>();
        Set<String> visitados = new HashSet<>();

        if (!grafo.containsKey(recursoInicial) && !recursoInicial.equals(recursoAlvo)) {

            return Optional.empty();
        }

        fila.add(recursoInicial);
        visitados.add(recursoInicial);

        while (!fila.isEmpty()) {
            String atual = fila.poll();

            if (atual.equals(recursoAlvo)) {

                return Optional.of(reconstruirCaminho(recursoInicial, recursoAlvo, predecessores));
            }

            // Se o nó atual tem vizinhos
            if (grafo.containsKey(atual)) {
                for (String vizinho : grafo.get(atual)) {
                    if (!visitados.contains(vizinho)) {
                        visitados.add(vizinho);
                        predecessores.put(vizinho, atual);
                        fila.add(vizinho);
                    }
                }
            }
        }


        return Optional.empty();
    }


    private List<String> reconstruirCaminho(String recursoInicial, String recursoAlvo, Map<String, String> predecessores) {
        List<String> caminho = new ArrayList<>();
        String passo = recursoAlvo;


        while (passo != null) {
            caminho.add(passo);
            passo = predecessores.get(passo);
        }


        Collections.reverse(caminho);
        return caminho;
    }

    //Classe auxiliar para o desafio 4.
class Alertas{
    Long timestamp;
    long transferidos;


    public Alertas(long T, String userId, String sessionId, String acao, String actionType, long t, Long transferencia){
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

  //classe auxiliar desafio 5.
    private static class LogEntry {
        private final long timestamp;
        private final String sessionId;
        private final String targetResource;

        public LogEntry(String csvLine) {
            String[] parts = csvLine.split(",");
            this.timestamp = Long.parseLong(parts[0].trim());
            this.sessionId = parts[2].trim();
            this.targetResource = parts[4].trim(); // TARGET_RESOURCE é a 5ª coluna (índice 4)
        }

        public long getTimestamp() { return timestamp; }
        public String getSessionId() { return sessionId; }
        public String getTargetResource() { return targetResource; }
    }





}