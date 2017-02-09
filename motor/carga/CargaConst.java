/* 
 * ---------------
 * CargaConst.java
 * ---------------
 * (C) Copyright 2014, by Grupo de pesquisas em Sistemas Paralelos e Distribuídos da Unesp (GSPD).
 *
 * Original Author:  Gabriel Covello (for GSPD);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 
 * 
 *
 */
package yasc.motor.carga;

import static yasc.motor.carga.CargaRandom.getMenorCaminho;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import yasc.motor.filas.RedeDeFilas;
import yasc.motor.filas.Tarefa;
import yasc.motor.filas.servidores.CS_Comunicacao;
import yasc.motor.filas.servidores.CentroServico;
import yasc.motor.random.Distribution;

import java.util.Random;

/**
 * Descreve como gerar tarefas e falhas
 * @author Gabriel Covello
 */
public class CargaConst extends GerarCarga {

    private int inicioIdentificadorTarefa;
    private int NumTars;
    private double Tam;
    private double ArrivalTime;
    private boolean distribuirIgual;
    

    public CargaConst(int NumTars, double Tam, boolean distribuirIgual, double ArrivalTime) {
        this.inicioIdentificadorTarefa = 0;
        this.NumTars = NumTars;
        this.Tam = Tam;
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
            int quantidadePorOrigem = this.getNumTars() / rdf.getOrigens().size();
            int resto = this.getNumTars() % rdf.getOrigens().size();
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
                    Tarefa tarefa = new Tarefa(
                            identificador,
                            "",
                            "",
                            Or,
                            DestinosPossiveis.get(indexDest),
                            getTam(),
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
                        getTam(),
                        gerador.nextExponential(ArrivalTime)/*tempo de criação*/);
                tarefas.add(tarefa);
                identificador++;
            }
        }
        else{
            for(int i=0; i < this.getNumTars(); i++){
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
                Tarefa tarefa = new Tarefa(
                        id,
                        "",
                        "",
                        Origem,
                        Destino,
                        getTam(),
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
        return String.format("%d %f",
                this.NumTars,
                this.Tam);
    }

    /*public static GerarCarga newGerarCarga(String entrada) {
    CargaConst newObj = null;
    String[] valores = entrada.split(" ");
    int numeroTarefas = Integer.parseInt(valores[1]);
    double min = Double.parseDouble(valores[2]);
    double max = Double.parseDouble(valores[3]);
    double avg = Double.parseDouble(valores[4]);
    double prob = Double.parseDouble(valores[5]);
    newObj = new CargaConst(aplicacao, proprietario, escalonador,
    numeroTarefas, maxComputacao, minComputacao, maxComunicacao, minComunicacao);
    return newObj;
    }*/
    public boolean isDistribuirIgual() {
        return distribuirIgual;
    }

    public void setDistribuirIgual(boolean distribuirIgual) {
        this.distribuirIgual = distribuirIgual;
    }

    public double getArrivalTime() {
        return ArrivalTime;
    }

    public void setArrivalTime(double ArrivalTime) {
        this.ArrivalTime = ArrivalTime;
    }

    @Override
    public int getTipo() {
        return GerarCarga.UNIFORM;
    }

    //Gets e Sets
    public int getNumTars() {
        return NumTars;
    }

    public void setNumTars(int NumTars) {
        this.NumTars = NumTars;
    }

    public double getTam() {
        return Tam;
    }

    public void setTam(double Tam) {
        this.Tam = Tam;
    }
    
    public void setInicioIdentificadorTarefa(int inicioIdentificadorTarefa) {
        this.inicioIdentificadorTarefa = inicioIdentificadorTarefa;
    }
    
    
    
}
