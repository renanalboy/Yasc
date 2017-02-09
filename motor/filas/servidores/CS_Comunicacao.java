/*
 * ---------------
 * CS_Comunicacao.java
 * ---------------
 * (C) Copyright 2015, by Grupo de pesquisas em Sistemas Paralelos e Distribuídos da Unesp (GSPD).
 *
 * Original Author:  Gabriel Covello (for Yasc);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 
 * 
 *
 */
package yasc.motor.filas.servidores;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import yasc.motor.Simulacao;
import yasc.motor.filas.Tarefa;
import yasc.motor.filas.servidores.implementacao.CS_Fila_Servidor;
import yasc.motor.filas.servidores.implementacao.CS_Instantaneo;
import yasc.motor.metricas.MetricasComunicacao;

/**
 * Classe abstrata que representa os servidores de comunicação do modelo de
 * fila, Esta classe possui atributos referente a este ripo de servidor, e
 * indica como calcular o tempo gasto para transmitir uma tarefa.
 *
 * @author Gabriel Covello
 */
public abstract class CS_Comunicacao extends CentroServico {

    /**
     * Identificador do centro de serviço, deve ser o mesmo do modelo icônico
     */
    private double ServiceRate;
    private double PreemptionTime;
    private double DelayRate;//Tempo para ir da fila ao servidor
    public MetricasComunicacao metrica;
    private double ServicoDisponivel;
    private int numTarefasPerdidas_Serv;
    private int numTarefasPerdidas_Atend;
    private int numTarefasReenviadas;
    private boolean Falha_Tarefa;
    private boolean Recuperavel;
    private double TempoFila;
    private double TempoAtend;
    
    

    public CS_Comunicacao(String id, double ServiceRate, double LostServiceRate, double DelayRate) {
        this.ServiceRate = ServiceRate;
        this.PreemptionTime = LostServiceRate;
        this.DelayRate = DelayRate;
        this.metrica = new MetricasComunicacao(id);
        this.ServicoDisponivel = this.ServiceRate - (this.ServiceRate * this.PreemptionTime);
        this.numTarefasPerdidas_Serv=0;
        this.numTarefasPerdidas_Atend=0;
        this.numTarefasReenviadas=0;
        this.TempoAtend = 0.0;
        this.TempoFila = 0.0;
    }
    
    public int valorF (int i){
        String a = MetricasComunicacao.valorSaida.get(i);
        if(a == null){
            a = "x";
        }
        int b = Integer.valueOf(a);
        return b;
    }

    public boolean isRecuperavel() {
        return Recuperavel;
    }

    public void setRecuperavel(boolean Recuperavel) {
        this.Recuperavel = Recuperavel;
    }
    
    public boolean isFalha_Tarefa() {
        return Falha_Tarefa;
    }

    public void setFalha_Tarefa(boolean Falha_Tarefa) {
        this.Falha_Tarefa = Falha_Tarefa;
    }

    
    public MetricasComunicacao getMetrica() {
        return metrica;
    }

    @Override
    public String getId() {
        return metrica.getId();
    }

    public double getServiceRate() {
        return ServiceRate;
    }

    public double getDelayRate() {
        return DelayRate;
    }

    public double getPreemptionTime() {
        return PreemptionTime;
    }
    
    public int getNumTarefasPerdidasServ() {
        return numTarefasPerdidas_Serv;
        //return 1;
    }
    
    public int getNumTarefasReenviadas() {
        return numTarefasReenviadas;
    }
    
    public int getNumTarefasPerdidas_Atend() {
        return numTarefasPerdidas_Atend;
    }
    
    public void setNumTarefasPerdidas_Atend(int num) {
        this.numTarefasPerdidas_Atend = this.numTarefasPerdidas_Atend + num;
    }

    public void setNumTarefasPerdidas(int num) {
        this.numTarefasPerdidas_Serv = this.numTarefasPerdidas_Serv + num;
    }

    public void setNumTarefasReenviadas(int num) {
        this.numTarefasReenviadas = this.numTarefasReenviadas + num;
    }

    public double getTempoFila() {
        return TempoFila;
    }

    public void setTempoFila(double TempoFila) {
        this.TempoFila =  this.TempoFila + TempoFila;
    }

    public double getTempoAtend() {
        return TempoAtend;
    }

    public void setTempoAtend(double TempoAtend) {
        this.TempoAtend = this.TempoAtend + TempoAtend;
    }
    
    
    /**
     * Retorna o tempo gasto
     *
     * @param TempoAtendimento
     */
    public double tempoTransmitir(double TempoAtendimento) {
        return (TempoAtendimento / ServicoDisponivel) + DelayRate;
    }

    /**
     * Retorna o menor caminho entre dois recursos de processamento
     *
     * @param origem recurso origem
     * @param destino recurso destino
     * @return caminho completo a partir do primeiro link até o recurso destino
     */
    public static List<CentroServico> getMenorCaminho(CentroServico origem, CentroServico destino) {
        //cria vetor com distancia acumulada
        List<CentroServico> nosExpandidos = new ArrayList<CentroServico>();
        List<Object[]> caminho = new ArrayList<Object[]>();
        CentroServico atual = origem;
        //armazena valor acumulado até atingir o nó atual
        Double acumulado = 0.0;
        do {
            ArrayList<CentroServico> lista = (ArrayList<CentroServico>) atual.getConexoesSaida();//recebe a lista de saidas de um objeto
            for (CentroServico cs : lista) {//percorre a lista procurando o caminho
                Object caminhoItem[] = new Object[4];
                caminhoItem[0] = atual;
                
                if (cs instanceof CS_Comunicacao && cs != destino) {
                    CS_Comunicacao comu = (CS_Comunicacao) cs;
                    caminhoItem[1] = comu.tempoTransmitir(10000) + acumulado;
                    caminhoItem[2] = cs;
                } else {
                    caminhoItem[1] = 0.0 + acumulado;
                    caminhoItem[2] = cs;
                }
                caminhoItem[3] = acumulado;
                caminho.add(caminhoItem);
            }
            //Marca que o nó atual foi expandido
            nosExpandidos.add(atual);
            //Inicia variavel de menor caminho com maior valor possivel
            Object[] menorCaminho = new Object[4];
            menorCaminho[0] = null;
            menorCaminho[1] = Double.MAX_VALUE;
            menorCaminho[2] = null;
            menorCaminho[3] = Double.MAX_VALUE;
            //busca menor caminho não expandido
            for (Object[] obj : caminho) {
                Double menor = (Double) menorCaminho[1];
                Double objAtual = (Double) obj[1];
                if (menor > objAtual && !nosExpandidos.contains(obj[2])) {
                    menorCaminho = obj;
                }
            }
            //atribui valor a atual com resultado da busca do menor caminho
            atual = (CentroServico) menorCaminho[2];
            acumulado = (Double) menorCaminho[1];
            
        } while (atual != null && atual != destino);
        if(atual==null)
            return null;
        else if (atual == destino) {
            List<CentroServico> menorCaminho = new ArrayList<CentroServico>();
            List<CentroServico> inverso = new ArrayList<CentroServico>();
            Object[] obj;
            while (atual != origem) {
                int i = 0;
                do {
                    obj = caminho.get(i);
                    i++;
                } while (obj[2] != atual);
                inverso.add(atual);
                atual = (CentroServico) obj[0];
            }
            for (int j = inverso.size() - 1; j >= 0; j--) {
                menorCaminho.add(inverso.get(j));
            }

            return menorCaminho;
        }
        else
            return null;
    }
    
    public static List<CentroServico> getTodoCaminho(CentroServico origem, CentroServico destino){
        //System.out.println("In getTodoCaminho");
        List<CentroServico> listaObj = new ArrayList<CentroServico>();
        List<CentroServico> lista = new ArrayList<CentroServico>();
        CentroServico atual;
        lista = CS_Instantaneo.tarefaMotor;
        //System.out.println("Lista" + lista);
        
        for(int i = 1; i < lista.size(); i++){
                atual = lista.get(i);
                //System.out.println("Atual: " + atual);
                listaObj.add(atual);
        }
        //System.out.println("Retornando listaOBJ - " + listaObj);
        return listaObj;
    } 
    
    
    
    public Tarefa criarCopia(Simulacao simulacao, Tarefa get) {
        Tarefa tarefa = new Tarefa(get);
        simulacao.addTarefa(tarefa);
        return tarefa;
    }
    
    public void gerarFalhas_Servidor(double scale, double shape, double probab) {
        Random rd = new Random();
        double x = scale * Math.pow(-Math.log(1 - rd.nextDouble()), 1 / shape);
        if(x > 1)
            x = 1;
        if(x > 0 && x <= probab)
            this.setFalha(true);
    }
    
    public void gerarFalhas_Tarefa(double scale, int shape, double Porcent, Tarefa Cliente){//gera falhas no atendimento dos clientes (perda ou entrega com erro)
        Random rd = new Random();
        double result = (scale * Math.pow(-Math.log(1 - rd.nextDouble()), 1 / shape));
        if(result > 1)
            result = 1;
        if(result > 0 && result <= Porcent){//verifica se numero gerado representa falha
            Cliente.setFalha_atendimento(true);
            int Tipo = rd.nextInt()%2;           
            if(Tipo==1){//seta falha como recuperavel
                   Cliente.setRecuperavel(true);
            }
            else{//seta falha como não recuperável
                Cliente.setRecuperavel(false);
            }
        }
    }
}
