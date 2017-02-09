/* 
 * ---------------
 * CargaRandom.java
 * ---------------
 * (C) Copyright 2015, by Grupo de pesquisas em Sistemas Paralelos e Distribuídos da Unesp (GSPD).
 *
 * Original Author:  Gabriel Covello (for GSPD);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 
 * 09-Set-2014 : Version 2.0;
 *
 */
package yasc.motor.carga;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import yasc.motor.filas.RedeDeFilas;
import yasc.motor.filas.Tarefa;
import yasc.motor.filas.servidores.CS_Comunicacao;
import yasc.motor.filas.servidores.CS_Processamento;
import yasc.motor.filas.servidores.CentroServico;
import yasc.motor.random.Distribution;

/**
 * Descreve como gerar tarefas na forma randomica
 *
 * @author Gabriel
 */
public class CargaRandom extends GerarCarga {

    private int numeroTarefas;
    private double min;
    private double max;
    private double Avg2;
    private double AvgPoisson;
    private double AvgNormal;
    private double DesvPad;
    private double ArrivalTime;
    private int TipoDistr;
    private boolean distribuirIgual;

    public CargaRandom(int numeroTarefas, double min, double max, double Avg2,
            double AvgPoisson, double AvgNormal, double DesvPad, int TipoDistr, boolean distribuirIgual, double ArrivalTime) {
        this.numeroTarefas = numeroTarefas;
        this.min = min;
        this.max = max;
        this.Avg2 = Avg2;
        this.AvgPoisson = AvgPoisson;
        this.AvgNormal = AvgNormal;
        this.DesvPad = DesvPad;
        this.TipoDistr = TipoDistr;
        this.distribuirIgual = distribuirIgual;
        this.ArrivalTime = ArrivalTime;
    }

    @Override
    public List<Tarefa> toTarefaList(RedeDeFilas rdf) {
        List<Tarefa> tarefas = new ArrayList<Tarefa>();
        if(!distribuirIgual){  
            List<CentroServico> DestinosPossiveis = new ArrayList<CentroServico>();
            int identificador = 0;
            int quantidadePorDestino = 0;
            int quantidadePorOrigem = this.getNumeroTarefas() / rdf.getOrigens().size();
            int resto = this.getNumeroTarefas() % rdf.getOrigens().size();
            Distribution gerador = new Distribution((int) System.currentTimeMillis());
            for (CentroServico Or : rdf.getOrigens()) {
                int indexDest = 0;
                DestinosPossiveis = new ArrayList<CentroServico>();
                for (CentroServico Dest : rdf.getDestinos()) {
                    if (getMenorCaminho(Or, Dest) != null || Or == Dest) {
                        DestinosPossiveis.add(Dest);
                    }
                }
                for (int i = 0; i < quantidadePorOrigem; i++) {
                    double tam = 0;
                    if (TipoDistr == 0) {
                        tam = gerador.twoStageUniform(min, Avg2, max);
                    }
                    if (TipoDistr == 1) {
                        tam = gerador.nextPoisson(AvgPoisson);
                    }
                    if (TipoDistr == 2) {
                        tam = gerador.nextNormal(AvgNormal, DesvPad);
                    }
                    Tarefa tarefa = new Tarefa(
                            identificador,
                            "",
                            "",
                            Or,
                            DestinosPossiveis.get(indexDest),
                            tam,
                            gerador.nextExponential(ArrivalTime)/*tempo de criação*/);
                    tarefas.add(tarefa);
                    identificador++;
                    if (indexDest + 1 >= DestinosPossiveis.size()) {
                        indexDest = 0;
                    } else {
                        indexDest++;
                    }
                }
            }
            for (int i = 0; i < resto; i++) {
                double tam = 0;
                if (TipoDistr == 0) {
                    tam = gerador.twoStageUniform(min, Avg2, max);
                }
                if (TipoDistr == 1) {
                    tam = gerador.nextPoisson(AvgPoisson);
                }
                if (TipoDistr == 2) {
                    tam = gerador.nextNormal(AvgNormal, DesvPad);
                }
                DestinosPossiveis = new ArrayList<CentroServico>();
                for (CentroServico Dest : rdf.getDestinos()) {
                    if (getMenorCaminho(rdf.getOrigens().get(0), Dest) != null || rdf.getOrigens().get(0) == Dest) {
                        DestinosPossiveis.add(Dest);
                    }
                }
                Tarefa tarefa = new Tarefa(
                        identificador,
                        "",
                        "",
                        rdf.getOrigens().get(0),
                        DestinosPossiveis.get(0),
                        tam,
                        gerador.nextExponential(ArrivalTime)/*tempo de criação*/);
                tarefas.add(tarefa);
                identificador++;
            }
        }
        else{
            for(int i=0; i < this.getNumeroTarefas(); i++){
                Distribution gerador = new Distribution((int) System.currentTimeMillis());
                CentroServico Origem, Destino;
                Random rd = new Random(System.currentTimeMillis());
                int origemIndex, destinoIndex, id = 0;
                do{
                    origemIndex = Math.abs(rd.nextInt()%rdf.getOrigens().size());
                    destinoIndex = Math.abs(rd.nextInt()%rdf.getDestinos().size());
                    Origem = rdf.getOrigens().get(origemIndex);
                    Destino = rdf.getDestinos().get(destinoIndex);
                } while (getMenorCaminho(Origem, Destino) == null);
                double tam = 0;
                if (TipoDistr == 0) {
                    tam = gerador.twoStageUniform(min, Avg2, max);
                }
                if (TipoDistr == 1) {
                    tam = gerador.nextPoisson(AvgPoisson);
                }
                if (TipoDistr == 2) {
                    tam = gerador.nextNormal(AvgNormal, DesvPad);
                }
                Tarefa tarefa = new Tarefa(
                        id,
                        "",
                        "",
                        Origem,
                        Destino,
                        tam,
                        gerador.nextExponential(ArrivalTime)/*tempo de criação*/);
                tarefas.add(tarefa);
                id++;

            }
                
        }
        return tarefas;
    }

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
        if (atual == null) {
            return null;
        } else if (atual == destino) {
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
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return String.format("%d %d %d\n%f\n%f %f\n%d %d",
                this.min, this.Avg2, this.max,
                this.AvgPoisson, this.AvgNormal, this.DesvPad, this.TipoDistr, this.numeroTarefas);
    }

    /*public static GerarCarga newGerarCarga(String entrada) {
    CargaRandom newObj;
    String aux = entrada.replace("\n", " ");
    String[] valores = aux.split(" ");
    int minComputacao = Integer.parseInt(valores[0]);
    int AverageComputacao = Integer.parseInt(valores[1]);
    int maxComputacao = Integer.parseInt(valores[2]);
    double ProbabilityComputacao = Double.parseDouble(valores[3]);
    int minComunicacao = Integer.parseInt(valores[4]);
    int AverageComunicacao = Integer.parseInt(valores[5]);
    int maxComunicacao = Integer.parseInt(valores[6]);
    double ProbabilityComunicacao = Double.parseDouble(valores[7]);
    //não usado --> valores[8]
    int timeOfArrival = Integer.parseInt(valores[9]);
    int numeroTarefas = Integer.parseInt(valores[10]);
    newObj = new CargaRandom(numeroTarefas,
    minComputacao, maxComputacao, AverageComputacao, ProbabilityComputacao,
    minComunicacao, maxComunicacao, AverageComunicacao, ProbabilityComunicacao,
    timeOfArrival);
    return newObj;
    }*/
    public double getArrivalTime() {
        return ArrivalTime;
    }

    public void setArrivalTime(double ArrivalTime) {
        this.ArrivalTime = ArrivalTime;
    }
    
    public boolean isDistribuirIgual() {
        return distribuirIgual;
    }

    public void setDistribuirIgual(boolean distribuirIgual) {
        this.distribuirIgual = distribuirIgual;
    }
    
    
    @Override
    public int getTipo() {
        return GerarCarga.RANDOM;
    }

    //Gets e sets
    public double getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public double getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public double getAvg2() {
        return Avg2;
    }

    public void setAvg2(int Avg2) {
        this.Avg2 = Avg2;
    }

    public double getAvgPoisson() {
        return AvgPoisson;
    }

    public void setAvgPoisson(double AvgPoisson) {
        this.AvgPoisson = AvgPoisson;
    }

    public double getAvgNormal() {
        return AvgNormal;
    }

    public void setAvgNormal(double AvgNormal) {
        this.AvgNormal = AvgNormal;
    }

    public double getDesvPad() {
        return DesvPad;
    }

    public void setDesvPad(double DesvPad) {
        this.DesvPad = DesvPad;
    }

    public int getTipoDistr() {
        return TipoDistr;
    }

    public void setTipoDistr(int TipoDistr) {
        this.TipoDistr = TipoDistr;
    }

    public Integer getNumeroTarefas() {
        return numeroTarefas;
    }

    public void setNumeroTarefas(int numeroTarefas) {
        this.numeroTarefas = numeroTarefas;
    }
}
