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
 * CS_Link.java
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
package yasc.motor.filas.servidores.implementacao;

import java.util.ArrayList;
import java.util.List;

import yasc.motor.EventoFuturo;
import yasc.motor.Simulacao;
import yasc.motor.filas.Mensagem;
import yasc.motor.filas.Tarefa;
import yasc.motor.filas.servidores.CS_Comunicacao;
import yasc.motor.filas.servidores.CentroServico;

/**
 *
 * @author denison
 */
public class CS_Link extends CS_Comunicacao {

    private CentroServico conexoesEntrada;
    private CentroServico conexoesSaida;
    private List<Tarefa> filaPacotes;
    private List<Mensagem> filaMensagens;
    private boolean linkDisponivel;
    private boolean linkDisponivelMensagem;
    private double tempoTransmitirMensagem;
    public static String idPass;

    public CS_Link(String id, double LarguraBanda, double Ocupacao, double Latencia) {
        super(id, LarguraBanda, Ocupacao, Latencia);
        this.conexoesEntrada = null;
        this.conexoesSaida = null;
        this.linkDisponivel = true;
        this.filaPacotes = new ArrayList<Tarefa>();
        this.filaMensagens = new ArrayList<Mensagem>();
        this.tempoTransmitirMensagem = 0;
        this.linkDisponivelMensagem = true;
        this.idPass = id;
        //System.out.println("idpass: " + idPass);
        CS_Instantaneo.Pass.add(idPass);
    }

    public CentroServico getConexoesEntrada() {
        return conexoesEntrada;
    }

    public void setConexoesEntrada(CentroServico conexoesEntrada) {
        this.conexoesEntrada = conexoesEntrada;
    }

    @Override
    public CentroServico getConexoesSaida() {
        return conexoesSaida;
    }

    public void setConexoesSaida(CentroServico conexoesSaida) {
        this.conexoesSaida = conexoesSaida;
    }

    @Override
    public void chegadaDeCliente(Simulacao simulacao, Tarefa cliente) {
        
    }

    @Override
    public void atendimento(Simulacao simulacao, Tarefa cliente) {
        
    }

    @Override
    public void saidaDeCliente(Simulacao simulacao, Tarefa cliente) {
        
    }

    @Override
    public void requisicao(Simulacao simulacao, Mensagem cliente, int tipo) {
        
    }

    @Override
    public Integer getCargaTarefas() {
        return 0;
    }
}
