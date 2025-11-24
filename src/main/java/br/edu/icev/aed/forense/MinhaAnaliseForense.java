package br.edu.icev.aed.forense;

import java.io.*;
import java.util.*;

public class MinhaAnaliseForense implements AnaliseForenseAvancada {


    //Desafio 1.
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



    //Desafio 2.
    @Override
    public List<String> reconstruirLinhaTempo(String s, String s1) throws IOException {
        return List.of();
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
                String[] dados=linha.split(",");
                //De acordo com a ordem de entrada na classe aviso: 1.TIMESTAMP,2.ACTION_TYPE,3.SEVERITY_LEVEL.
                long TIMESTAMP=Long.parseLong(dados[0].trim());
                String Id= dados[1].trim();
                String sessao=dados[2].trim();
                String acao=dados[3].trim();
                String alvo= dados[4].trim();
                int severidade= Integer.parseInt(dados[5].trim());
                Long transferencia= Long.parseLong(dados[6].trim());

                Alerta novoAlerta= new Alerta(TIMESTAMP,Id,sessao,acao,alvo,severidade,transferencia);

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


    //Desafio 5.
    @Override
    public Optional<List<String>> rastrearContaminacao(String s, String s1, String s2) throws IOException {
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











}