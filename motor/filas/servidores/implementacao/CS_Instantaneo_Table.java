 /*
 * ---------------
 * CS_Instantaneo.java
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
package yasc.motor.filas.servidores.implementacao;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptException;

import yasc.arquivo.CParser.CParser;
import yasc.arquivo.CParser_table.CParser_table;
import yasc.escalonador.Escalonador;
import yasc.gui.entrada.Gerar;
import yasc.motor.EventoFuturo;
import yasc.motor.Simulacao;
import yasc.motor.filas.Mensagem;
import yasc.motor.filas.Tarefa;
import yasc.motor.filas.servidores.CS_Comunicacao;
import yasc.motor.filas.servidores.CentroServico;

/**
 *
 * @author Renan Alboy (for Yasc)
 */
public class CS_Instantaneo_Table extends CS_Comunicacao implements Vertice{

    private List<CentroServico> conexoesEntrada;
    private List<CentroServico> conexoesSaida;
    private List<Tarefa> filaPacotes;
    private List<Mensagem> filaMensagens;
    private ArrayList<String> Names = new ArrayList<String>();
    private ArrayList<String> Values = new ArrayList<String>();
    private ArrayList<String> inputColNames = new ArrayList<String>();//exclusivo para tabela
    private ArrayList<String> outputColNames = new ArrayList<String>();//excluisvo para tabela
    private ArrayList<ArrayList<String>> inputValues = new ArrayList<ArrayList<String>>();//exclusivo para tabela
    private ArrayList<ArrayList<String>> outputValues = new ArrayList<ArrayList<String>>();//exclusivo para tabela
    private String Trans_Func;
    //private String inputString;//convertida para array dentro de Parser_table
    //private String outputString;//convertida para array dentro de Parser_table
    Object resultado;
    private boolean Origem;
    private boolean Destino;
    
    /*Parametros importantes para a chamada da classe: InputValues, OutputValues, InputColNames
    OutputColNames, Names, Values*/

    public CS_Instantaneo_Table(String id, double LarguraBanda, double Ocupacao, double Latencia, String Trans_Func, ArrayList<String> Names, ArrayList<String> Values, boolean Origem, boolean Destino, ArrayList<String> inputColNames, ArrayList<String> outputColNames, ArrayList<ArrayList<String>> inputValues, ArrayList<ArrayList<String>> outputValues) {
        super(id, LarguraBanda, Ocupacao, Latencia);
        this.conexoesEntrada = new ArrayList<CentroServico>();
        this.conexoesSaida = new ArrayList<CentroServico>();
        this.filaPacotes = new ArrayList<Tarefa>();
        this.filaMensagens = new ArrayList<Mensagem>();
        this.Trans_Func = Trans_Func;
        this.Names = Names;
        this.Values = Values;
        this.Origem = Origem;
        this.Destino = Destino;
        this.inputColNames = inputColNames;
        this.outputColNames = outputColNames;
        this.inputValues = inputValues;
        this.outputValues = outputValues;
    }

    public List<CentroServico> getConexoesEntrada() {
        return conexoesEntrada;
    }

    @Override
    public void addConexoesEntrada(CentroServico conexoesEntrada) {
        this.conexoesEntrada.add(conexoesEntrada);
    }

    @Override
    public List<CentroServico> getConexoesSaida() {
        return conexoesSaida;
    }

    @Override
    public void addConexoesSaida(CentroServico conexoesSaida) {
        this.conexoesSaida.add(conexoesSaida);
    }

    @Override
    public void chegadaDeCliente(Simulacao simulacao, Tarefa cliente) {
        //cria evento para iniciar o atendimento imediatamente
        if (cliente.isFalha_atendimento() && this.equals(cliente.getServ_falha())) {//realiza falha de atendimento
            if (cliente.isRecuperavel()) {//com recuperação
                Tarefa tar = criarCopia(simulacao, cliente);
                tar.setFalha_atendimento(false);
                tar.setRecuperavel(false);
                EventoFuturo evtFut = new EventoFuturo(
                        simulacao.getTime(this) + cliente.getTimeout(),
                        EventoFuturo.CHEGADA,
                        tar.getOrigem(),
                        tar);
                simulacao.addEventoFuturo(evtFut);
                this.setNumTarefasPerdidas_Atend(1);
                this.setNumTarefasReenviadas(1);
            } else {//sem recuperação 
                this.setNumTarefasPerdidas_Atend(1);
            }
        } else {
            EventoFuturo novoEvt = new EventoFuturo(
                    simulacao.getTime(this),
                    EventoFuturo.ATENDIMENTO,
                    this,
                    cliente);
            simulacao.addEventoFuturo(novoEvt);
        }
    }

    @Override
    public void atendimento(Simulacao simulacao, Tarefa cliente) {
        cliente.iniciarAtendimentoComunicacao(simulacao.getTime(this));
        //Gera evento para atender cliente
        EventoFuturo evtFut = new EventoFuturo(
                simulacao.getTime(this),
                EventoFuturo.SAÍDA,
                this, cliente);
        //Evento adicionado a lista de eventos futuros
        simulacao.addEventoFuturo(evtFut);
        InputStream is = new ByteArrayInputStream(Trans_Func.getBytes());
        CParser_table I = new CParser_table(is, Names, Values);
        /*try{
            resultado = I.InterpretaExp(Trans_Func, I);
        }catch(ScriptException ex){
            Logger.getLogger(CS_Durativo.class.getName()).log(Level.SEVERE, null, ex);
        }*/
    }

    @Override
    public void saidaDeCliente(Simulacao simulacao, Tarefa cliente) {
        cliente.finalizarAtendimentoComunicacao(simulacao.getTime(this));
        //Gera evento para chegada da tarefa no proximo servidor
        EventoFuturo evtFut;
        if (this == cliente.getOrigem()) {
            List<CentroServico> cs = new ArrayList<CentroServico>();
            if(this != cliente.getDestino()){
                cs = this.getMenorCaminho(cliente.getOrigem(), cliente.getDestino());
                if (cs == null) {
                    throw new IllegalArgumentException("The model has no icons.");
                } else {
                    cliente.setCaminho(cs);
                    cliente.setLocalProcessamento(cliente.getDestino());
                }
            }
            else{
                cliente.setCaminho(cs);
                cliente.setLocalProcessamento(cliente.getDestino());
            }
        }
        if (!cliente.getCaminho().isEmpty()) {
            evtFut = new EventoFuturo(
                    simulacao.getTime(this),
                    EventoFuturo.CHEGADA,
                    cliente.getCaminho().remove(0),
                    cliente);
            simulacao.addEventoFuturo(evtFut);

        }
        //Evento adicionado a lista de eventos futuros
    }

    @Override
    public void requisicao(Simulacao simulacao, Mensagem cliente, int tipo) {
    }

    @Override
    public Integer getCargaTarefas() {
        return 0;
    }
}
