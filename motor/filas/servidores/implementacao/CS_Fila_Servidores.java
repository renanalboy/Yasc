/*
 * ---------------
 * CS_Fila_Servidores.java
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
public class CS_Fila_Servidores extends CS_Comunicacao implements Vertice{

    private List<CentroServico> conexoesEntrada;
    private List<CentroServico> conexoesSaida;
    private List<Tarefa> filaPacotes;
    private List<Mensagem> filaMensagens;
    private boolean recursoDisponivel;
    private boolean linkDisponivelMensagem;
    private double tempoTransmitirMensagem;
    private int Num_serv = 0;
    private String Escalonador;
    private int Capacidade_Fila;
    private double Prob_Falha_Serv;
    private double Prob_Falha_Clie;
    private boolean Capacidade;
    private boolean Origem;
    private boolean Destino;

    public CS_Fila_Servidores(String id, double TxServico, double TempoPreempcao, double TxAtraso, String Escalonador, int Num_serv, int Capacidade_Fila, double Prob_Falha_Serv, double Prob_Falha_Clie, boolean Capacidade, boolean Origem, boolean Destino) {
        super(id, TxServico, TempoPreempcao, TxAtraso);
        this.conexoesEntrada = new ArrayList<CentroServico>();
        this.conexoesSaida = new ArrayList<CentroServico>();
        this.recursoDisponivel = true;
        this.filaPacotes = new ArrayList<Tarefa>();
        this.filaMensagens = new ArrayList<Mensagem>();
        this.tempoTransmitirMensagem = 0;
        this.linkDisponivelMensagem = true;
        this.Num_serv = Num_serv;
        this.Escalonador = Escalonador;
        this.setFalha(false);
        this.Capacidade_Fila = Capacidade_Fila;
        this.Prob_Falha_Clie = Prob_Falha_Clie;
        this.Prob_Falha_Serv = Prob_Falha_Serv;
        this.Capacidade = Capacidade;
        this.Origem = Origem;
        this.Destino = Destino;
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
        cliente.iniciarEsperaComunicacao(simulacao.getTime(this));
        gerarFalhas_Tarefa(1, 1, Prob_Falha_Clie, cliente);//shape = 1 (falhas constantes e independentes de outros eventos, scale = 1 (dispersar falhas ao longo da simulação)
        gerarFalhas_Servidor(1, 1, Prob_Falha_Serv);
        
        if (isFalha()) {//realiza falha de servidor
            Mensagem msg = new Mensagem(this, Mensagens.FALHAR, cliente);
            EventoFuturo evt = new EventoFuturo(
                    simulacao.getTime(this),
                    EventoFuturo.MENSAGEM,
                    this, msg);
            simulacao.addEventoFuturo(evt);
        } else if (cliente.isFalha_atendimento()) {//realiza falha de prestação de serviço
            if (cliente.isRecuperavel()) {//com recuperação
                Tarefa tar = criarCopia(simulacao, cliente);
                tar.setFalha_atendimento(false);
                EventoFuturo evtFut = new EventoFuturo(
                        simulacao.getTime(this) + cliente.getTimeout(),
                        EventoFuturo.CHEGADA,
                        tar.getOrigem(),
                        tar);
                this.setNumTarefasPerdidas_Atend(1);
                this.setNumTarefasReenviadas(1);
                simulacao.addEventoFuturo(evtFut);
                
            } else{//sem recuperação 
                this.setNumTarefasPerdidas_Atend(1);
            }
        } else if (this.Num_serv != 0) {
            //indica que recurso está ocupado
            this.Num_serv--;
            //cria evento para iniciar o atendimento imediatamente
            //System.out.println("Tempo:"+ simulacao.getTime(this) + " Id:" + cliente.getIdentificador());
            EventoFuturo novoEvt = new EventoFuturo(
                    simulacao.getTime(this),
                    EventoFuturo.ATENDIMENTO,
                    this,
                    cliente);
            simulacao.addEventoFuturo(novoEvt);
        } else {//inserção de clientes na fila de maneira organizada por algoritmos de escalonamento
            String fila = Escalonador;
            switch (fila) {
                case "FIFO":
                    Fifo(cliente);
                    break;
                case "LIFO":
                    Pilha(cliente);
                    break;
                case "SJF":
                    Sjf(cliente);
                    break;
                default:
                    throw new IllegalArgumentException("Illegal scheduling algorithm"); 
            }
            //System.out.println("Tempo:" + simulacao.getTime(this) + " Id:" + cliente.getIdentificador());


        }
    }

    public void Fifo(Tarefa cliente) {
        if (Capacidade) {
            if (filaPacotes.size() < Capacidade_Fila) {
                filaPacotes.add(cliente);
            } else {
                this.setNumTarefasPerdidas_Atend(1);
            }
        } else {
            filaPacotes.add(cliente);
        }

    }

    public void Pilha(Tarefa cliente) {
        if (Capacidade) {
            if (filaPacotes.size() < Capacidade_Fila) {
                filaPacotes.add(0, cliente);
            } else {
                this.setNumTarefasPerdidas_Atend(1);
            }
        } else {
            filaPacotes.add(0, cliente);
        }
    }

    public void Sjf(Tarefa cliente) {
        double tam;
        if (Capacidade) {
            if (filaPacotes.size() < Capacidade_Fila) {
                if (!filaPacotes.isEmpty()) {
                    tam = filaPacotes.get(0).getTamComunicacao();
                    int i;
                    for (i = 0; tam < cliente.getTamComunicacao() && i < filaPacotes.size(); i++) {
                        tam = filaPacotes.get(i).getTamComunicacao();
                    }
                    filaPacotes.add(i, cliente);
                } else {
                    filaPacotes.add(cliente);
                }
            } else {
                this.setNumTarefasPerdidas_Atend(1);
            }
        } else {
            if (!filaPacotes.isEmpty()) {
                tam = filaPacotes.get(0).getTamComunicacao();
                int i;
                for (i = 0; tam < cliente.getTamComunicacao() && i < filaPacotes.size(); i++) {
                    tam = filaPacotes.get(i).getTamComunicacao();
                }
                filaPacotes.add(i, cliente);
            } else {
                filaPacotes.add(cliente);
            }
        }
    }

    @Override
    public void atendimento(Simulacao simulacao, Tarefa cliente) {

        cliente.finalizarEsperaComunicacao(simulacao.getTime(this));
        //contabiliza tempo de espera em fila
        this.setTempoFila(cliente.getTempoEsperaLocal());
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
        //Incrementa o número de Mbits transmitido por este link
        this.getMetrica().incMbitsTransmitidos(cliente.getTamComunicacao());
        //Incrementa o tempo de transmissão
        double tempoTrans = this.tempoTransmitir(cliente.getTamComunicacao());
        this.getMetrica().incSegundosDeTransmissao(tempoTrans);
        //Incrementa o tempo de transmissão no pacote
        cliente.finalizarAtendimentoComunicacao(simulacao.getTime(this));
        //contabiliza tempo de atendimento realizado pelo servidor
        this.setTempoAtend(cliente.getTempoAtendLocal());
        if (this == cliente.getOrigem()) {//acha o caminho a ser percorrido pelas tarefas
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
        EventoFuturo evtFut;
        if (!cliente.getCaminho().isEmpty()) {
            evtFut = new EventoFuturo(
                    simulacao.getTime(this),
                    EventoFuturo.CHEGADA,
                    cliente.getCaminho().remove(0), cliente);
            //Event adicionado a lista de evntos futuros
            simulacao.addEventoFuturo(evtFut);
        }
        if (filaPacotes.isEmpty()) {
            //Indica que está livre
            this.Num_serv++;
        } else {
            //Gera evento para atender proximo cliente da lista
            Tarefa proxCliente = filaPacotes.remove(0);
            evtFut = new EventoFuturo(
                    simulacao.getTime(this),
                    EventoFuturo.ATENDIMENTO,
                    this, proxCliente);
            //Event adicionado a lista de evntos futuros
            simulacao.addEventoFuturo(evtFut);
        }
    }

    public void atenderFalha(CS_Comunicacao simulacao, Mensagem cliente) {
        simulacao.setNumTarefasPerdidas(1);
    }

    @Override
    public void requisicao(Simulacao simulacao, Mensagem cliente, int tipo) {
        if (cliente.getTipo() == Mensagens.FALHAR) {
            atenderFalha(this, cliente);
        }
    }

    @Override
    public Integer getCargaTarefas() {
        if (this.Num_serv == Num_serv && linkDisponivelMensagem) {
            return 0;
        } else {
            return (filaMensagens.size() + filaPacotes.size()) + 1;
        }
    }
}