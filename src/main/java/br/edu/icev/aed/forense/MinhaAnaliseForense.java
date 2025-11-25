package br.edu.icev.aed.forense;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
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
    public List<String> reconstruirLinhaTempo(String arquivo, String s1) throws IOException {
        Queue<String> filaDeAcoes = new LinkedList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(arquivo))) {
            String linha=br.readLine();
            boolean isHeader = true;

            while (linha!=null){

                String[] sessao= linha.split(",");

                if (sessao.length<4) continue;

                String sessionID=sessao[2];
                String acao=sessao[3];


                if (sessionID.equals(s1)) {
                    filaDeAcoes.add(acao);
                }
            }
        }

        List<String> resultado = new ArrayList<>();
        while (!filaDeAcoes.isEmpty()) {
            resultado.add(filaDeAcoes.poll());
        }

        return resultado;
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

//classe auxiliar desafio 2.
class Logs{
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

class session{
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