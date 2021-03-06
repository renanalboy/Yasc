/*
 * ---------------
 * CS_Servidor_Infinito.java
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
 */
package yasc.motor.filas.servidores.implementacao;

import java.util.ArrayList;
import java.util.List;

import yasc.motor.EventoFuturo;
import yasc.motor.Mensagens;
import yasc.motor.Simulacao;
import yasc.motor.filas.Mensagem;
import yasc.motor.filas.Tarefa;
import yasc.motor.filas.servidores.CS_Comunicacao;
import yasc.motor.filas.servidores.CentroServico;

/**
 *
 * @author Gabriel Covello (for Yasc)
 */
public class CS_Servidor_Infinito extends CS_Comunicacao implements Vertice {

    private List<CentroServico> conexoesEntrada;
    private List<CentroServico> conexoesSaida;
    private Integer pacotes = 0;
    private double Prob_Falha_Serv;
    private double Prob_Falha_Clie;
    private boolean Origem;
    private boolean Destino;

    
    public CS_Servidor_Infinito(String id, double TxServico, double TempoPreempcao, double TxAtraso, double Prob_Falha_Serv, double Prob_Falha_Clie, boolean Origem, boolean Destino) {
        super(id, TxServico, TempoPreempcao, TxAtraso);
        this.conexoesEntrada = new ArrayList<CentroServico>();
        this.conexoesSaida = new ArrayList<CentroServico>();
        this.setFalha(false);
        this.Prob_Falha_Clie = Prob_Falha_Clie;
        this.Prob_Falha_Serv = Prob_Falha_Serv;
        this.Origem = Origem;
        this.Destino = Destino;
    }

    

    @Override
    public void addConexoesEntrada(CentroServico conexao) {
        this.conexoesEntrada.add(conexao);
    }

    @Override
    public void addConexoesSaida(CentroServico conexao) {
        this.conexoesSaida.add(conexao);
    }

    @Override
    public void chegadaDeCliente(Simulacao simulacao, Tarefa cliente) {
        gerarFalhas_Tarefa(1, 1, Prob_Falha_Clie, cliente);//shape = 1 (falhas constantes e independentes de outros eventos, scale = 1 (dispersar falhas ao longo da simulação)
        gerarFalhas_Servidor(1, 1, Prob_Falha_Serv);
        
        if (isFalha()) {//realiza falha de servidor
            Mensagem msg = new Mensagem(this, Mensagens.FALHAR, cliente);
            EventoFuturo evt = new EventoFuturo(
                    simulacao.getTime(this),
                    EventoFuturo.MENSAGEM,
                    this, msg);
            simulacao.addEventoFuturo(evt);
        } else if (cliente.isFalha_atendimento()) {//realiza falha de atendimento
            if (cliente.isRecuperavel()) {//com recuperação
                Tarefa tar = criarCopia(simulacao, cliente);
                tar.setFalha_atendimento(false);
                EventoFuturo evtFut = new EventoFuturo(
                        simulacao.getTime(this) + cliente.getTimeout(),
                        EventoFuturo.CHEGADA,
                        tar.getOrigem(),
                        tar);
                this.setNumTarefasReenviadas(1);
                this.setNumTarefasPerdidas_Atend(1);
                simulacao.addEventoFuturo(evtFut);
            } else {//sem recuperação 
                this.setNumTarefasPerdidas_Atend(1);
            }
        } else {
            //cria evento para iniciar o atendimento imediatamente
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
        pacotes++;
        cliente.iniciarAtendimentoComunicacao(simulacao.getTime(this));
        //Gera evento para atender proximo cliente da lista
        EventoFuturo evtFut = new EventoFuturo(
                simulacao.getTime(this) + tempoTransmitir(cliente.getTamComunicacao()),
                EventoFuturo.SAÍDA,
                this, cliente);
        //Event adicionado a lista de evntos futuros
        simulacao.addEventoFuturo(evtFut);

    }

    @Override
    public void saidaDeCliente(Simulacao simulacao, Tarefa cliente) {
        pacotes--;
        //Incrementa o número de Mbits transmitido por este link
        this.getMetrica().incMbitsTransmitidos(cliente.getTamComunicacao());
        //Incrementa o tempo de transmissão
        double tempoTrans = this.tempoTransmitir(cliente.getTamComunicacao());
        this.getMetrica().incSegundosDeTransmissao(tempoTrans);
        //Incrementa o tempo de transmissão no pacote
        cliente.finalizarAtendimentoComunicacao(simulacao.getTime(this));
        //contabiliza tempo de atendimento realizado pelo servidor
        this.setTempoAtend(cliente.getTempoAtendLocal());
        if (this == cliente.getOrigem()) {
            List<CentroServico> cs = new ArrayList<CentroServico>();
            if(this != cliente.getDestino()){
                cs = this.getMenorCaminho(cliente.getOrigem(), cliente.getDestino());
                if (cs == null) {
                    throw new IllegalArgumentException("No route found, please select a valid route.");
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
        //Gera evento para chegada da tarefa no proximo servidor
        if (!cliente.getCaminho().isEmpty()) {
            EventoFuturo evtFut = new EventoFuturo(
                    simulacao.getTime(this),
                    EventoFuturo.CHEGADA,
                    cliente.getCaminho().remove(0), cliente);
            //Event adicionado a lista de evntos futuros
            simulacao.addEventoFuturo(evtFut);
        }

    }

    @Override
    public void requisicao(Simulacao simulacao, Mensagem cliente, int tipo) {
        if (cliente.getTipo() == Mensagens.FALHAR) {
            atenderFalha(this, cliente);
        }
    }

    public void atenderFalha(CS_Comunicacao simulacao, Mensagem cliente) {
        simulacao.setNumTarefasPerdidas(1);
    }

    @Override
    public Integer getCargaTarefas() {
        return pacotes;
    }

    @Override
    public Object getConexoesSaida() {
        return conexoesSaida;
    }

}
