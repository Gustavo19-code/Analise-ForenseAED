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


public class MinhaAnaliseForense implements AnaliseForenseAvancada {


    public MinhaAnaliseForense() {
    }

    //Desafio 1
    @Override
    public Set<String> encontrarSessoesInvalidas(String arquivo) throws IOException {
        Set<String> sessoesInvalidas = new HashSet<>();

        Map<String, Deque<String>> userStacks = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(arquivo))) {
            br.readLine();

            String linha;
            while ((linha = br.readLine()) != null) {
                String[] partes = linha.split(",");
                //  campos: TIMESTAMP, USER_ID, SESSION_ID, ACTION_TYPE
                if (partes.length < 4) continue;

                String userId = partes[1].trim();
                String sessionId = partes[2].trim();
                String actionType = partes[3].trim();

                Deque<String> stack = userStacks.computeIfAbsent(userId, k -> new ArrayDeque<>());

                if (actionType.equals("LOGIN")) {

                    if (!stack.isEmpty()) {
                        sessoesInvalidas.add(stack.peek());
                    }

                    stack.push(sessionId);

                } else if (actionType.equals("LOGOUT")) {
                    if (stack.isEmpty() || !stack.peek().equals(sessionId)) {
                        sessoesInvalidas.add(sessionId);
                    } else {
                        stack.pop();
                    }
                }
            }
        }


        for (Deque<String> stack : userStacks.values()) {
            sessoesInvalidas.addAll(stack);
        }
        return sessoesInvalidas;
    }


    //  Desafio 2
    @Override
    public List<String> reconstruirLinhaTempo(String caminhoArquivoCsv, String sessionIdAlvo) throws IOException {
        Queue<String> filaAcoes = new LinkedList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(caminhoArquivoCsv))) {
            br.readLine();
            String linha;

            while ((linha = br.readLine()) != null) {
                String[] partes = linha.split(",");
                if (partes.length < 4) continue;


                String sessionIdAtual = partes[2].trim();
                String action = partes[3].trim();

                if (sessionIdAtual.equals(sessionIdAlvo)) {
                    filaAcoes.add(action);
                }
            }
        }
        return new ArrayList<>(filaAcoes);
    }


    // Desafio 3
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
                    // Campos relevantes para Alerta (3 argumentos): TIMESTAMP(0), ACTION_TYPE(3), SEVERITY_LEVEL(5)
                    long TIMESTAMP = Long.parseLong(dados[0].trim());
                    String userId=dados[1].trim();
                    String acao = dados[2].trim();
                     String sessionId=dados[3].trim();
                    String targetResource=dados[4].trim();
                    int severidade = Integer.parseInt(dados[5].trim());
                    long bytesTransferred= Long.parseLong(dados[6].trim());


                    Alerta novoAlerta = new Alerta(TIMESTAMP,userId, acao,sessionId,targetResource, severidade,bytesTransferred);
                    alertasSeveridade.offer(novoAlerta);

                } catch (NumberFormatException ignored) {

                }
            }
        }

        List<Alerta> resultados = new ArrayList<>();
        int contador = 0;


        while (!alertasSeveridade.isEmpty() && contador < n) {
            resultados.add(alertasSeveridade.poll());
            contador++;
        }

        return resultados;
    }

    // Classe auxiliar para o Desafio 4
    private static class EventoTransferencia {
        final long timestamp;
        final long transferidos;

        public EventoTransferencia(long T, long t) {
            this.timestamp = T;
            this.transferidos = t;
        }
    }

    //  Desafio 4
    @Override
    public Map<Long, Long> encontrarPicosTransferencia(String arquivo) throws IOException {
        List<EventoTransferencia> eventosTransferencia = new ArrayList<>();

        try (BufferedReader leitor = new BufferedReader(new FileReader(arquivo))) {
            leitor.readLine();
            String linha;
            while ((linha = leitor.readLine()) != null) {
                String[] dados = linha.split(",");
                if (dados.length < 7) continue;

                try {
                    long timestamp = Long.parseLong(dados[0].trim());
                    long transferidos = Long.parseLong(dados[6].trim());

                    if (transferidos > 0) {
                        eventosTransferencia.add(new EventoTransferencia(timestamp, transferidos));
                    }
                } catch (NumberFormatException ignored) {

                }
            }
        }

        Deque<EventoTransferencia> stack = new ArrayDeque<>();
        Map<Long, Long> resultados = new HashMap<>();

        // Itera em ordem cronológica inversa (do fim para o começo)
        for (int i = eventosTransferencia.size() - 1; i >= 0; i--) {
            EventoTransferencia eventoAtual = eventosTransferencia.get(i);

            // Desempilha enquanto o topo for menor ou igual (mantém a pilha decrescente)
            while (!stack.isEmpty() && stack.peek().transferidos <= eventoAtual.transferidos) {
                stack.pop();
            }

            // Se a pilha não estiver vazia, o topo é o "próximo elemento MAIOR"
            if (!stack.isEmpty()) {
                resultados.put(eventoAtual.timestamp, stack.peek().timestamp);
            }

            // Empilha o evento atual
            stack.push(eventoAtual);
        }

        return resultados;
    }

    // Classe auxiliar para o Desafio 5
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


    // DESAFIO 5 .
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


        if (!grafo.containsKey(recursoInicial)) {
            return Optional.empty();
        }

        fila.add(recursoInicial);
        visitados.add(recursoInicial);

        while (!fila.isEmpty()) {
            String atual = fila.poll();

            if (atual.equals(recursoAlvo)) {

                return Optional.of(reconstruirCaminho(recursoAlvo, predecessores));
            }

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


    private List<String> reconstruirCaminho(String recursoAlvo, Map<String, String> predecessores) {
        List<String> caminho = new ArrayList<>();
        String passo = recursoAlvo;


        while (passo != null) {
            caminho.add(passo);
            passo = predecessores.get(passo);
        }

        Collections.reverse(caminho);
        return caminho;
    }
}
