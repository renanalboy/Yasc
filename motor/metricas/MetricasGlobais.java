/* ==========================================================
 * iSPD : iconic Simulator of Parallel and Distributed System
 * ==========================================================
 *
 * (C) Copyright 2010-2014, by Grupo de pesquisas em Sistemas Paralelos e Distribuídos da Unesp (GSPD).
 *
 * Project Info:  http://gspd.dcce.ibilce.unesp.br/
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * [Oracle and Java are registered trademarks of Oracle and/or its affiliates. 
 * Other names may be trademarks of their respective owners.]
 *
 * ---------------
 * MetricasGlobais.java
 * ---------------
 * (C) Copyright 2014, by Grupo de pesquisas em Sistemas Paralelos e Distribuídos da Unesp (GSPD).
 *
 * Original Author:  Denison Menezes (for GSPD);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 
 * 09-Set-2014 : Version 2.0;
 *
 */
package yasc.motor.metricas;

import java.io.Serializable;
import java.util.List;

import yasc.motor.filas.RedeDeFilas;
import yasc.motor.filas.Tarefa;
import yasc.motor.filas.servidores.CS_Comunicacao;
import yasc.motor.filas.servidores.CS_Processamento;
import yasc.motor.filas.servidores.CentroServico;

/**
 *
 * @author denison
 */
public class MetricasGlobais implements Serializable {

    private double tempoSimulacao;
    private double eficiencia;
    private int TarefasPerdidas;
    private int TarefasReenviadas;
    private int TotalPerdido;
    private int TotalTarefas;
    private int total;
    private double Tempo_Fila;
    private double Tempo_Atendimento;
    private double Tempo_MFila;
    private double Tempo_MAtendimento;
         

    public MetricasGlobais(RedeDeFilas redeDeFilas, double tempoSimulacao, List<Tarefa> tarefas) {
        this.tempoSimulacao = tempoSimulacao;
        this.TotalPerdido = addTarefasPerdidas(redeDeFilas.getCs());
        this.TotalTarefas = addTotalTarefas(tarefas);
        addTempos(tarefas);
        this.eficiencia = getEficiencia(tarefas);
        this.total = 0;
    }

    public MetricasGlobais() {
        this.tempoSimulacao = 0;
        this.eficiencia = 0;
        this.TarefasPerdidas = 0;
        this.TarefasReenviadas = 0;
        this.total = 0;
    }

    public int getTotalPerdido() {
        return TotalPerdido;
    }

    public int getTotalTarefas() {
        return TotalTarefas;
    }
    
    public int getTarefasPerdidas() {
        return TarefasPerdidas;
    }

    public int getTarefasReenviadas() {
        return TarefasReenviadas;
    }

    
    public double getEficiencia() {
        return eficiencia;
    }

    public double getTempoSimulacao() {
        return tempoSimulacao;
    }

    public double getTempo_MFila() {
        return Tempo_MFila;
    }

    public void setTempo_MFila(double Tempo_MFila) {
        this.Tempo_MFila = Tempo_MFila;
    }

    public double getTempo_MAtendimento() {
        return Tempo_MAtendimento;
    }

    public void setTempo_MAtendimento(double Tempo_MAtendimento) {
        this.Tempo_MAtendimento = Tempo_MAtendimento;
    }

    public double getTempo_Fila() {
        return Tempo_Fila;
    }

    public void setTempo_Fila(double Tempo_Fila) {
        this.Tempo_Fila = Tempo_Fila;
    }

    public double getTempo_Atendimento() {
        return Tempo_Atendimento;
    }

    public void setTempo_Atendimento(double Tempo_Atendimento) {
        this.Tempo_Atendimento = Tempo_Atendimento;
    }
    
    private double getOciosidadeComputacao(RedeDeFilas redeDeFilas) {
        double tempoLivreMedio = 0.0;
        for (CS_Processamento maquina : redeDeFilas.getMaquinas()) {
            double aux = maquina.getMetrica().getSegundosDeProcessamento();
            aux = (this.getTempoSimulacao() - aux);
            tempoLivreMedio += aux;//tempo livre
            aux = maquina.getOcupacao() * aux;
            tempoLivreMedio -= aux;
        }
        tempoLivreMedio = tempoLivreMedio / redeDeFilas.getMaquinas().size();
        return (tempoLivreMedio * 100) / getTempoSimulacao();
    }

    
    private double getEficiencia(List<Tarefa> tarefas) {
        double somaEfic = 0;
        for (Tarefa tar : tarefas) {
            somaEfic += tar.getMetricas().getEficiencia();
        }
        return somaEfic / tarefas.size();
        /*
         double tempoUtil = 0.0;
         double tempoMedio = 0.0;
         for (CS_Processamento maquina : redeDeFilas.getMaquinas()) {
         double aux = maquina.getMetrica().getSegundosDeProcessamento();
         aux = (this.getTempoSimulacao() - aux);//tempo livre
         aux = maquina.getLostServiceRate() * aux;//tempo processando sem ser tarefa
         tempoUtil = aux + maquina.getMetrica().getSegundosDeProcessamento();
         tempoMedio += tempoUtil / this.getTempoSimulacao();
         }
         tempoMedio = tempoMedio / redeDeFilas.getMaquinas().size();
         return tempoMedio; 
         */
    }

    public void setTotalPerdido(int TotalPerdido) {
        this.TotalPerdido = TotalPerdido;
    }

    public void setTotalTarefas(int TotalTarefas) {
        this.TotalTarefas = TotalTarefas;
    }

    
    public void setTarefasPerdidas(int TarefasPerdidas) {
        this.TarefasPerdidas = TarefasPerdidas;
    }

    public void setTarefasReenviadas(int TarefasReenviadas) {
        this.TarefasReenviadas = TarefasReenviadas;
    }

    public void setTempoSimulacao(double tempoSimulacao) {
        this.tempoSimulacao = tempoSimulacao;
    }
    
    public void setEficiencia(double eficiencia) {
        this.eficiencia = eficiencia;
    }

    public void add(MetricasGlobais global) {
        tempoSimulacao += global.getTempoSimulacao();
        TarefasPerdidas += global.getTarefasPerdidas();
        TarefasReenviadas += global.getTarefasReenviadas();
        total++;
    }
    
    public int addTarefasPerdidas(List<CentroServico> cs){
        for(int i=0; i< cs.size(); i++){
            CS_Comunicacao aux = (CS_Comunicacao) cs.get(i);
            this.TarefasPerdidas = this.TarefasPerdidas + aux.getNumTarefasPerdidasServ() + aux.getNumTarefasPerdidas_Atend();
            this.TarefasReenviadas = this.TarefasReenviadas + aux.getNumTarefasReenviadas();
        }
        return this.TarefasPerdidas - this.TarefasReenviadas;
    }
    
    public void addTempos(List<Tarefa> tars){
        for(int i=0 ; i < tars.size(); i++){
            this.Tempo_Fila += tars.get(i).getMetricas().getTempoEsperaComu();
            this.Tempo_Atendimento += tars.get(i).getMetricas().getTempoComunicacao();
        }
        this.Tempo_MFila += this.Tempo_Fila/tars.size();
        this.Tempo_MAtendimento += this.Tempo_Atendimento/tars.size();
    }
    
    public int addTotalTarefas(List<Tarefa> Tarefas){
        return Tarefas.size();
    }

    @Override
    public String toString() {
        double eficiencia = (TotalPerdido*100)/ TotalTarefas;
        int totalTemp = 1;
        if (total > 0) {
            totalTemp = total;
        }
        String texto = "\t\tSimulation Results\n\n";
        texto += String.format("\tTotal Simulated Time = %g \n", tempoSimulacao / totalTemp);
        texto += String.format("\tTotal of Lost Tasks = %d \n", TotalPerdido);
        texto += String.format("\tTotal of Resubmitted Tasks = %d \n", TarefasReenviadas);
        texto += String.format("\tEfficiency = %.2f %%\n", eficiencia);
        if (eficiencia > 70.0) {
            texto += "\tEfficiency GOOD\n ";
        } else if (eficiencia > 40.0) {
            texto += "\tEfficiency MEDIA\n ";
        } else {
            texto += "\tEfficiency BAD\n ";
        }
        return texto;
    }
}
