/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package yasc;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import yasc.gui.JPrincipal;
import yasc.gui.LogExceptions;
import yasc.gui.entrada.Linguagem;
import yasc.gui.entrada.Listas_Armazenamento;
import yasc.gui.entrada.Parser;

/**
 *
 * @author gabriel
 */
public class Main {

    Parser Table_Carregar;
    public static String tipo;
    public static boolean ordena =true;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        //LogExceptions logExceptions = new LogExceptions(null);
        //Thread.setDefaultUncaughtExceptionHandler(logExceptions);
        if(Paths.get("simulatorlibrary").toFile().exists()){
            Main executa = new Main();
            String separator = System.getProperty("file.separator");  
            executa.leitura_arquivo("simulatorlibrary" + separator + "simulador.conf");
            Locale.setDefault(new Locale("en", "US"));
            //LogExceptions logExceptions = new LogExceptions(null);
            JPrincipal gui = new JPrincipal();
            gui.setLocationRelativeTo(null);
            //logExceptions.setParentComponent(gui);
            gui.setVisible(true);
        } 
        else{
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    new Linguagem().setVisible(true);
                }
            });
        }
    }
    
    public void leitura_arquivo(String arq) {
        Scanner ler = new Scanner(System.in);
        try {
            FileReader arquivo = new FileReader(arq);
            BufferedReader lerArq = new BufferedReader(arquivo);
            String linha = lerArq.readLine(); // lê a primeira linha 
            // a variável "linha" recebe o valor "null" quando o processo de repetição atingir o final do arquivo texto 
            while (linha != null) {
                String[] b = linha.split(" ");
                //for (int i = 0; i < b.length; i++) {
                  //  System.out.println(b[i]);
                //}
                switch (b[0]) {
                    case "1":
                        Table_Carregar = new Parser("", "", null, null, null, null, null, false, false, false, false,false, false, "", "", "", "", "", new ArrayList<String>(),new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>());
                        Table_Carregar.setId(b[1]);
                        Table_Carregar.setImg(b[2]);
                        Table_Carregar.setFila(true);
                        Table_Carregar.setFilaTipo(b[3]);
                        switch(b[3]){
                            case "One_queue_one_server":
                                for(int j = 0; j < 2; j++){
                                    linha = lerArq.readLine();
                                    String[] c = linha.split(" ");
                                    if (c[0].equals("a")) {             
                                        Table_Carregar.getLabels().add(c[1]);
                                        Table_Carregar.getMetricas().add(c[2]);
                                        if(j == 1){
                                            if(c[3] == "NonDeterministic")
                                                Table_Carregar.setNDeterministico(true);
                                        }
                                    }
                                }
                                break; 
                            case "Multiple_queues_one_server":
                                for(int j = 0; j < 3; j++){
                                    linha = lerArq.readLine();
                                    String[] c = linha.split(" ");
                                    if (c[0].equals("a")) {             
                                        Table_Carregar.getLabels().add(c[1]);
                                        Table_Carregar.getMetricas().add(c[2]);
                                        if(j == 1){
                                            if(c[3] == "NonDeterministic")
                                                Table_Carregar.setNDeterministico(true);
                                        }
                                    }
                                }
                                break;    
                            case "Multiple_queues_multiple_servers":
                                for(int j = 0; j < 4; j++){
                                    linha = lerArq.readLine();
                                    String[] c = linha.split(" ");
                                    if (c[0].equals("a")) {             
                                        Table_Carregar.getLabels().add(c[1]);
                                        Table_Carregar.getMetricas().add(c[2]);
                                        if(j == 1){
                                            if(c[3] == "NonDeterministic")
                                                Table_Carregar.setNDeterministico(true);
                                        }
                                    }
                                }
                                break;
                            case "One_queue_multiple_servers":
                                for(int j = 0; j < 3; j++){
                                    linha = lerArq.readLine();
                                    String[] c = linha.split(" ");
                                    if (c[0].equals("a")) {             
                                        Table_Carregar.getLabels().add(c[1]);
                                        Table_Carregar.getMetricas().add(c[2]);
                                        if(j == 1){
                                            if(c[3] == "NonDeterministic")
                                                Table_Carregar.setNDeterministico(true);
                                        }
                                    }
                                }
                                break;
                            case "Infinity_Server":
                                for(int j = 0; j < 2; j++){
                                    linha = lerArq.readLine();
                                    String[] c = linha.split(" ");
                                    if (c[0].equals("a")) {             
                                        Table_Carregar.getLabels().add(c[1]);
                                        Table_Carregar.getMetricas().add(c[2]);
                                        if(j == 1){
                                            if(c[3] == "NonDeterministic")
                                                Table_Carregar.setNDeterministico(true);
                                        }
                                    }
                                }
                                break;
                        }
                        Listas_Armazenamento.Lista_Carregamento.add(Table_Carregar);
                        break;
                    case "2":
                        Table_Carregar = new Parser("", "", null, null, null, null, null, false, false, false, false, false, false, "", "", "", "", "", new ArrayList<String>(),new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>());
                        Table_Carregar.setId(b[1]);
                        Table_Carregar.setImg(b[2]);
                        Table_Carregar.setDurativo(true);
                        Table_Carregar.setFuncTrans(true);
                        Table_Carregar.setFuncaoTrans(b[3]);
                        for (int k = 0; k < Integer.parseInt(b[4]); k++) {
                            linha = lerArq.readLine();
                            String[] c = linha.split(" ");
                            if (c[0].equals("b")) {
                                if(c.length > 3){
                                    Table_Carregar.setTipos(c[1]+ " " + c[2]);
                                    Table_Carregar.setVars(c[3]);
                                }
                                else{
                                    Table_Carregar.setTipos(c[1]);
                                    Table_Carregar.setVars(c[2]);
                                }
                            }
                        }
                        Listas_Armazenamento.Lista_Carregamento.add(Table_Carregar);
                        break;
                    case "3":
                        switch(b[1]){
                            /*Obs: O ELSE trabalha com a abertura de arquivos cujos objetos sejam caracterizados
                            a partir da tabela. O programa não admite a abertura de bibliotecas que sejam formados
                            por objetos caracterizados por fórmulas e tabelas. Ou seja, a biblioteca deve ser composta
                            por objetos caracterizados somentepor fórmula ou somente por tabela.*/
                            case "y"://Caso em que logicOperation é setado como sim
                                Table_Carregar = new Parser("", "", new ArrayList<ArrayList<String>>(), new ArrayList<ArrayList<String>>(), new ArrayList<String>(), new ArrayList<String>(), new ArrayList<ArrayList<String>>(), false, false, false, false, false, false, "", "", "", "", "", new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>());
                                Table_Carregar.setLogicOperation(b[1]);
                                Table_Carregar.setId(b[2]);
                                Table_Carregar.setImg(b[3]);
                                Table_Carregar.setInstantaneo(true);
                                Table_Carregar.setFuncaoTrans(b[4]); tipo = "logic";
                                for (int k = 0; k < Integer.parseInt(b[5]); k++) {
                                    linha = lerArq.readLine();
                                    String[] c = linha.split(" ");
                                    if (c[0].equals("b")) {
                                        if(c.length > 3){
                                            Table_Carregar.setTipos(c[1]+ " " + c[2]);
                                            Table_Carregar.setVars(c[3]);
                                        }
                                        else{
                                            Table_Carregar.setTipos(c[1]);
                                            Table_Carregar.setVars(c[2]);
                                        }                           
                                    }
                                }
                                Listas_Armazenamento.Lista_Carregamento.add(Table_Carregar);
                                break;
                            case  "n"://Caso do uso de fórmula em que logicOperation é setado como não
                                Table_Carregar = new Parser("", "", new ArrayList<ArrayList<String>>(), new ArrayList<ArrayList<String>>(), new ArrayList<String>(), new ArrayList<String>(), new ArrayList<ArrayList<String>>(), false, false, false, false, false, false, "", "", "", "", "", new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>());
                                Table_Carregar.setLogicOperation(b[1]);
                                Table_Carregar.setId(b[2]);
                                Table_Carregar.setImg(b[3]);
                                Table_Carregar.setInstantaneo(true);
                                Table_Carregar.setFuncaoTrans(b[4]); 
                                for (int k = 0; k < Integer.parseInt(b[5]); k++) {
                                    linha = lerArq.readLine();
                                    String[] c = linha.split(" ");
                                    if (c[0].equals("b")) {
                                        if(c.length > 3){
                                            Table_Carregar.setTipos(c[1]+ " " + c[2]);
                                            Table_Carregar.setVars(c[3]);
                                        }
                                        else{
                                            Table_Carregar.setTipos(c[1]);
                                            Table_Carregar.setVars(c[2]);
                                        }                           
                                    }
                                }
                                Listas_Armazenamento.Lista_Carregamento.add(Table_Carregar); tipo="notLogic";
                                break;
                            default://Caso da tabela
                                Table_Carregar = new Parser("", "", new ArrayList<ArrayList<String>>(), new ArrayList<ArrayList<String>>(), new ArrayList<String>(), new ArrayList<String>(), new ArrayList<ArrayList<String>>(), false, false, false, false, false, false, "", "", "", "", "", new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>());
                                Table_Carregar.setId(b[1]);
                                Table_Carregar.setImg(b[2]);
                                Table_Carregar.setInstantaneo(true);
                                Table_Carregar.setInputTrans(b[3]);
                                Table_Carregar.setOutputTrans(b[4]);
                                for (int k = 0; k < Integer.parseInt(b[5]); k++) {
                                    linha = lerArq.readLine();
                                    String[] c = linha.split(" ");
                                    if (c[0].equals("b")) {
                                        if(c.length > 3){
                                            Table_Carregar.setTipos(c[1]+ " " + c[2]);
                                            Table_Carregar.setVars(c[3]);
                                        }
                                        else{
                                            Table_Carregar.setTipos(c[1]);
                                            Table_Carregar.setVars(c[2]);
                                        }                           
                                    }
                                }
                                Listas_Armazenamento.Lista_Carregamento.add(Table_Carregar); tipo="table";
                                break;    
                        }
                    case "4":
                        Listas_Armazenamento.setFalha(true);
                        break;
                    case "5":
                        Listas_Armazenamento.setFalha_Capacidade(true);
                        break;
                    case "6":
                        Listas_Armazenamento.setEvent(b[1]);
                        break;
                    case "7":
                        switch(b[1]){
                            case "Loss": 
                                Listas_Armazenamento.setPerda(true);
                                break;
                            case "Service":
                                Listas_Armazenamento.setAtendimento(true);
                                Listas_Armazenamento.setAtendimentoMet(b[2]);
                                break;
                            case "Queue":
                                Listas_Armazenamento.setFila(true);
                                Listas_Armazenamento.setFilaMet(b[2]);
                                break;
                            default:
                        }
                    default:
                }
                linha = lerArq.readLine();
            } // lê da segunda até a última linha 
            arquivo.close();
        } catch (IOException e) {
            System.err.printf("Erro na abertura do arquivo: %s.\n", e.getMessage());
        }
    }
}
