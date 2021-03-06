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
 * MetricasComunicacao.java
 * ---------------
 * (C) Copyright 2014, by Grupo de pesquisas em Sistemas Paralelos e Distribuídos da Unesp (GSPD).
 *
 * Original Author:  Denison Menezes (for GSPD);
 * Contributor(s):   Renan Alboy;
 *
 * Changes
 * -------
 * 
 * 09-Set-2014 : Version 2.0;
 *
 */
package yasc.motor.metricas;

import java.io.Serializable;
import java.util.ArrayList;
import yasc.arquivo.CParser_form.CParser_form;

/**
 * Cada centro de serviço usado para conexão deve ter um objeto desta classe
 * Responsavel por armazenar o total de comunicação realizada em Mbits e segundos
 * @author denison
 */
public class MetricasComunicacao implements Serializable {
    /**
     * Armazena o total de comunicação realizada em Mbits
     */
    private double MbitsTransmitidos;
    public static ArrayList<String> valorSaida = new ArrayList<String>();
    /**
     * Armazena o total de comunicação realizada em segundos
     */
    private double SegundosDeTransmissao;
    private String id;
    private int result;
    //public static String link;
    
    
    public MetricasComunicacao(String id) {
        this.id = id;
        this.MbitsTransmitidos = 0;
        this.SegundosDeTransmissao = 0;
        this.valorSaida = valorSaida;
        this.result = 0;
    }

    public void incMbitsTransmitidos(double MbitsTransmitidos) {
        this.MbitsTransmitidos += MbitsTransmitidos;
    }

    public void incSegundosDeTransmissao(double SegundosDeTransmissao) {
        this.SegundosDeTransmissao += SegundosDeTransmissao;
    }

    public double getMbitsTransmitidos() {
        return MbitsTransmitidos;
    }
    
    public double getSegundosDeTransmissao() {
        return SegundosDeTransmissao;
    }

   public String getId() {
       CParser_form.id = id;
        return id;
    }
   
   /*public static String getLink() {
       id = link;
       CParser_form.link = id;
        return id;
    }*/

    void setMbitsTransmitidos(double d) {
        this.MbitsTransmitidos = d;
    }

    void setSegundosDeTransmissao(double d) {
        this.SegundosDeTransmissao = d;
    }
    
    /*Médodos para serem chamados em caso de uso lógico*/
    
     public String buscaValoresResultantes(int result){
        return valorSaida.get(result);
    }
    
    //Armazena os valores de saída no vetor
    public void addValoresResultantes(int result){
        String j = result + "";
        valorSaida.add(j);
    }
     
    //Retorna valor da saida
    public int getValoresResultantes(){
        return result;
    }
    
    //aplica valor da saída a variavel result. Para que esta seja buscada e adicionada ao arraylist
    public void setValoresResultantes(int d){
        this.result = d;
    }
    
    
}
