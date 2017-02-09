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
import yasc.Main;

import yasc.arquivo.CParser.CParser;
import yasc.arquivo.CParser_form.CParser_form;
import yasc.arquivo.CParser_table.CParser_table;
import yasc.arquivo.xml.IconicoXML;
import yasc.escalonador.Escalonador;
import yasc.gui.entrada.Gerar;
import yasc.gui.entrada.Parser;
import yasc.gui.iconico.grade.Instantaneo;
import yasc.motor.EventoFuturo;
import yasc.motor.Simulacao;
import yasc.motor.filas.Mensagem;
import yasc.motor.filas.Tarefa;
import yasc.motor.filas.servidores.CS_Comunicacao;
import yasc.motor.filas.servidores.CentroServico;
import yasc.motor.metricas.MetricasComunicacao;

/**
 *
 * @author Gabriel Covello (for Yasc)
 */
public class CS_Instantaneo extends CS_Comunicacao implements Vertice{

    private List<CentroServico> conexoesEntrada;
    private List<CentroServico> conexoesSaida;
    private List<Tarefa> filaPacotes;
    private List<Mensagem> filaMensagens;
    private ArrayList<String> Names = new ArrayList<String>();
    private ArrayList<String> Values = new ArrayList<String>();
    private String Trans_Func;
    private ArrayList<String> arrayIn = new ArrayList<String>();
    Object resultado;
    private boolean Origem;
    private boolean Destino;
    public static boolean ori;
    public static boolean dest;
    public static ArrayList<String> id2  = new ArrayList<String>();
    public static ArrayList<String> O  = new ArrayList<String>();
    public static ArrayList<String> D  = new ArrayList<String>();
    public static ArrayList<String> ancestral = new ArrayList<String>();
    public static ArrayList<String> goTo = new ArrayList<String>();
    public static ArrayList<String> InputColNames = new ArrayList<String>();
    private ArrayList<String> OutputColNames = new ArrayList<String>();
    private ArrayList<ArrayList<String>> InputValues = new ArrayList<ArrayList<String>>();
    private ArrayList<ArrayList<String>> OutputValues = new ArrayList<ArrayList<String>>();
    public static ArrayList<String> tarefaOrg = new ArrayList<String>(); 
    public static ArrayList<String> passagemOrg = new ArrayList<String>();
    public static ArrayList<String> elemAncestral = new ArrayList<String>();
    public static ArrayList<String> elemAtual = new ArrayList<String>();
    public static ArrayList<String> Pass = new ArrayList<String>();
    public static ArrayList<CentroServico> ances = new ArrayList<CentroServico>();
    public static ArrayList<CentroServico> agora = new ArrayList<CentroServico>();
    public static List<CentroServico> tarefaMotor = new ArrayList<CentroServico>();
    public static ArrayList<CentroServico> geral = new ArrayList<CentroServico>();
    public static int numOrigens = 0; 
    public static int numDestinos = 0; 
    public static int t = 0;
    

    public CS_Instantaneo(String id, double LarguraBanda, double Ocupacao, double Latencia, String Trans_Func, ArrayList<String> Names, ArrayList<String> Values, boolean Origem, boolean Destino) {
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
        //this.InputColNames = InputColNames;
        //this.OutputColNames = OutputColNames;
        //this.InputValues = InputValues;
        //this.OutputValues = OutputValues;
    }

    public List<CentroServico> getConexoesEntrada() {
        return conexoesEntrada;
    }

    @Override
    public void addConexoesEntrada(CentroServico conexoesEntrada) {
        ancestral.add(conexoesEntrada.getId().toString());
        ances.add(conexoesEntrada);
        geral.add(conexoesEntrada);
        this.conexoesEntrada.add(conexoesEntrada);
    }

    @Override
    public List<CentroServico> getConexoesSaida() {
        return conexoesSaida;
    }

    @Override
    public void addConexoesSaida(CentroServico conexoesSaida) {
        this.conexoesSaida.add(conexoesSaida);
        goTo.add(conexoesSaida.getId().toString());
        agora.add(conexoesSaida);
        geral.add(conexoesSaida);
    }

    @Override
    public void chegadaDeCliente(Simulacao simulacao, Tarefa cliente) {
        if(Main.ordena == true && Main.tipo.equals("logic")){
            Ordena(); 
            Main.ordena = false;
        }
        if(Main.ordena == true && Main.tipo.equals("table")){
            Ordena(); 
            Main.ordena = false;
        }
        if (cliente.isFalha_atendimento() && this.equals(cliente.getServ_falha())) {
            if (cliente.isRecuperavel()) {
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
            } else {
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
        //Criação do objeto
       if("notLogic".equals(Main.tipo)){
            CParser I = new CParser(is, Names, Values);
            try{
                resultado = I.InterpretaExp(Trans_Func, I);
            }catch(ScriptException ex){
                Logger.getLogger(CS_Durativo.class.getName()).log(Level.SEVERE, null, ex);
            }
       } else {
            if("logic".equals(Main.tipo)){
               CParser_form I = new CParser_form(is, Names, Values);
               try{
                    ori = Origem;
                    dest = Destino;
                    resultado = I.InterpretaExp(Trans_Func, I);
               }catch(ScriptException ex){
                    Logger.getLogger(CS_Durativo.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if("table".equals(Main.tipo)){
                CParser_table I = new CParser_table(is, Names, Values);
                try{
                    ori = Origem;
                    dest = Destino;
                    resultado = I.InterpretaExp(I);
               }catch(ScriptException ex){
                    Logger.getLogger(CS_Durativo.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
       }
    }

    @Override
    public void saidaDeCliente(Simulacao simulacao, Tarefa cliente) {
        cliente.finalizarAtendimentoComunicacao(simulacao.getTime(this));
        //Gera evento para chegada da tarefa no proximo servidor
        EventoFuturo evtFut;
        if (this == cliente.getOrigem()) {
            List<CentroServico> cs = new ArrayList<CentroServico>();
            if(this != cliente.getDestino()){
                if(Main.tipo.equals("notLogic") || numOrigens == 1){
                    cs = this.getMenorCaminho(cliente.getOrigem(), cliente.getDestino());
               }else{
                    cs = getTodoCaminho(cliente.getOrigem(), cliente.getDestino());
                }
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
    }

    @Override
    public void requisicao(Simulacao simulacao, Mensagem cliente, int tipo) {
    }

    @Override
    public Integer getCargaTarefas() {
        return 0;
    }
   
    /*
    Função que analisa os objetos passados no modelo cocstruido e ordena segundo 
    a seguimte caracteristica:
    
    - Objetos marcados somente como origem
    - Objetos sem marcação de origemou destino
    - Objetos marcados somente como destino
    
    Tal forma de marcação define o inicio e fim da simulação. Assim que finalizada 
    a análise de todos os objetos mapeados pelo motor, a ordem dos oobjetos é passada
    para a execução no motor.
    */
    
    public static void Ordena(){
       
        int j = 0;
        //System.out.println("id2 em ordena: " + id2);
        tarefaOrg.clear();
        //System.out.println("tarefaOrg em ordena: " + tarefaOrg);
        tarefaMotor.clear();
        //System.out.println("tarefaMotor em ordena: " + tarefaMotor);
        //MetricasComunicacao.valorSaida.clear();
        /*System.out.println("goto: " + goTo + "Pass: " + Pass);
        System.out.println("elemAtual: " + elemAtual );
        System.out.println("elemAncestral: " + elemAncestral );
        System.out.println("ancestral: " + ancestral );*/
        if(id2.size() == 1){
          tarefaOrg.add(id2.get(0)); 
        }else{
            /*Armazena primeiro os objetos que são somente Origem*/
            //System.out.println(O);
            //System.out.println(D);
            for(int i = 0; i < O.size(); i++){
                if(O.get(i).equals("o")){
                    tarefaOrg.add(id2.get(i));
                    for(int k = 0; k < ancestral.size(); k ++){
                        if(ancestral.get(k).equals(tarefaOrg.get(j))){
                            elemAncestral.add(id2.get(i));
                            elemAtual.add(goTo.get(k));
                            passagemOrg.add(Pass.get(k));
                            numOrigens++;
                        }
                    }
                    j++;
                }
            }
            /*Armazena os objetos de passagem*/
            for(int i = 0; i < O.size(); i++){
                if(O.get(i).equals("x") && D.get(i).equals("x")){
                    tarefaOrg.add(id2.get(i));
                    for(int k = 0; k < ancestral.size(); k ++){
                        if(ancestral.get(k).equals(tarefaOrg.get(j))){
                            elemAncestral.add(id2.get(i));
                            elemAtual.add(goTo.get(k));
                            passagemOrg.add(Pass.get(k));
                        }
                    }
                    j++;
                }
            }
            /*Armazena os objetos que são destino*/
            for(int i = 0; i < D.size(); i++){
                if(D.get(i).equals("d")){
                    tarefaOrg.add(id2.get(i));
                    for(int k = 0; k < ancestral.size(); k ++){
                        if(ancestral.get(k).equals(tarefaOrg.get(j))){
                            elemAncestral.add(id2.get(i));
                            elemAtual.add(goTo.get(k));
                            passagemOrg.add(Pass.get(k));
                            numDestinos++;
                        }
                    }
                    j++;
                }
            }   
            
            CParser_form.IdObjFinal = tarefaOrg;
            
            boolean ja;
            String copia;
            //Ordenaçao para tarefaMotor
            for(int i = 0; i < tarefaOrg.size(); i++){
                ja = false;
               for(int k = 0; k < geral.size(); k++){
                   copia = geral.get(k).getId().toString();
                   if(ja ==false){
                        if(copia.equals(tarefaOrg.get(i))){
                           tarefaMotor.add(geral.get(k));
                           ja =true;
                        }
                    }
                }
            }
        }
    }

}
