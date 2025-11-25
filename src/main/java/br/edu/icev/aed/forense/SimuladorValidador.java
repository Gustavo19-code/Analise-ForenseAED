package br.edu.icev.aed.forense;
import java.io.File;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


public class SimuladorValidador {

    private static final String ARQUIVO_TESTE_CSV = "forensic_logs.csv";

    private static final String NOME_CLASSE_SOLUCAO = "br.edu.icev.aed.forense.MinhaAnaliseForense";

    public static void main(String[] args) {

        System.out.println("--- SIMULADOR DE VALIDADOR - Teste de Execução ---");

        if (!new File(ARQUIVO_TESTE_CSV).exists()) {
            System.err.println("ERRO : Arquivo de logs de teste ('" + ARQUIVO_TESTE_CSV + "') não encontrado.");
            System.err.println("Por favor, crie o arquivo CSV na raiz do projeto para testar.");
            return;
        }

        try {

            System.out.println("1. Carregando classe: " + NOME_CLASSE_SOLUCAO);
            Class<?> clazz = Class.forName(NOME_CLASSE_SOLUCAO);

            Constructor<?> constructor = clazz.getDeclaredConstructor();
            AnaliseForenseAvancada analyzer = (AnaliseForenseAvancada) constructor.newInstance();
            System.out.println("   ✓ Classe instanciada com sucesso!");

            testDesafio1(analyzer);
            testDesafio2(analyzer);
            testDesafio3(analyzer);
            testDesafio4(analyzer);
            testDesafio5(analyzer);

            System.out.println("--- SIMULAÇÃO CONCLUÍDA. Verifique as saídas para Corretude. ---");

        } catch (ClassNotFoundException e) {
            System.err.println(" ERRO CRÍTICO: Classe não encontrada. Verifique o nome da classe e o classpath/JAR.");
        } catch (NoSuchMethodException e) {
            System.err.println(" ERRO CRÍTICO: Construtor público sem parâmetros não encontrado. OBRIGATÓRIO!");
        } catch (Exception e) {
            System.err.println(" ERRO DE EXECUÇÃO: Falha ao rodar a solução. Verifique a lógica.");
            e.printStackTrace();
        }
    }

    private static void testDesafio1(AnaliseForenseAvancada impl) throws Exception {
        System.out.println("\n--- Testando Desafio 1 (Sessões Inválidas) ---");
        Set<String> resultado = impl.encontrarSessoesInvalidas(ARQUIVO_TESTE_CSV);
        System.out.println("Resultado: " + resultado);
    }

    private static void testDesafio2(AnaliseForenseAvancada impl) throws Exception {
        String targetSession = "session-alpha-723";
        System.out.println("\n--- Testando Desafio 2 (Linha do Tempo de " + targetSession + ") ---");
        List<String> resultado = impl.reconstruirLinhaTempo(ARQUIVO_TESTE_CSV, targetSession);
        System.out.println("Resultado: " + resultado);
    }

    private static void testDesafio3(AnaliseForenseAvancada impl) throws Exception {
        int n = 3;
        System.out.println("\n--- Testando Desafio 3 (Priorizar Top " + n + " Alertas) ---");
        List<Alerta> resultado = impl.priorizarAlertas(ARQUIVO_TESTE_CSV, n);
        System.out.println("Resultado (Severidade Decrescente):");
        resultado.forEach(a -> System.out.println("  " + a));
    }

    private static void testDesafio4(AnaliseForenseAvancada impl) throws Exception {
        System.out.println("\n--- Testando Desafio 4 (Picos de Transferência) ---");
        Map<Long, Long> resultado = impl.encontrarPicosTransferencia(ARQUIVO_TESTE_CSV);
        System.out.println("Resultado: " + resultado);
    }

    private static void testDesafio5(AnaliseForenseAvancada impl) throws Exception {
        String inicial = "/usr/bin/sshd";
        String alvo = "/var/secrets/key.dat";
        System.out.println("\n--- Testando Desafio 5 (Rastrear Contaminação: " + inicial + " -> " + alvo + ") ---");
        Optional<List<String>> resultado = impl.rastrearContaminacao(ARQUIVO_TESTE_CSV, inicial, alvo);
        System.out.println("Resultado: " + resultado);
    }
}