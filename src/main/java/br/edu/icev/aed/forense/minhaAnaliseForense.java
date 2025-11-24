package br.edu.icev.aed.forense;

import java.io.*;
import java.util.*;

public class MinhaAnaliseForense implements AnaliseForenseAvancada {

    @Override
    public Set<String> encontrarSessoesInvalidas(String arquivo) throws IOException {
        Set<String> sessoesInvalidas = new HashSet<>();
        // Mapa: Chave = UserID, Valor = Pilha de SessionIDs
        Map<String, Deque<String>> userStacks = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(arquivo))) {
            String linha;
            br.readLine(); // Pular cabeçalho

            while ((linha = br.readLine()) != null) {
                String[] partes = linha.split(",");
                // Validação básica
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
    }

    @Override
    public List<String> reconstruirLinhaTempo(String arquivo, String sessionId) throws IOException {
        // Implementar usando Queue<String>
    }

    @Override
    public List<Alerta> priorizarAlertas(String arquivo, int n) throws IOException {
        if(n<=0){
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
                String[] dados=linha.split(",");
                //De acordo com a ordem de entrada na classe aviso: 1.TIMESTAMP,2.ACTION_TYPE,3.SEVERITY_LEVEL.
                long TIMESTAMP=Long.parseLong(dados[0].trim());
                String acao=dados[3].trim();
                int severidade= Integer.parseInt(dados[5].trim());

                Alerta novoAlerta= new Alerta(TIMESTAMP, acao, severidade);

                alertasSeveridade.offer(novoAlerta);

            }

        }

        List<Alerta>resultados=new ArrayList<>();
        int contador= 0;

        while(!alertasSeveridade.isEmpty()&& contador<n){
            resultados.add(alertasSeveridade.poll());
            contador++;
        }

        return resultados;


    }

    @Override
    public Map<Long, Long> encontrarPicosTransferencia(String arquivo) throws IOException {


    }

    @Override
    public Optional<List<String>> rastrearContaminacao(String arquivo, String origem, String destino) throws IOException {
        // Implementar usando BFS em grafo
    }
}