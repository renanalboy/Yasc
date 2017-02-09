/* 
 * ---------------
 * GerarCarga.java
 * ---------------
 * (C) Copyright 2015, by Grupo de pesquisas em Sistemas Paralelos e Distribuídos da Unesp (GSPD).
 *
 * Original Author:  Gabriel Covello (for GSPD);
 * Contributor(s):   -;
 *
 *
 */
package yasc.motor.carga;

import java.util.List;

import yasc.motor.filas.RedeDeFilas;
import yasc.motor.filas.Tarefa;

/**
 * Descreve forma de criar tarefas durante a simulação
 * @author Gabriel
 */
public abstract class GerarCarga {
    public static final int NULL = -1;
    public static final int RANDOM = 0;
    public static final int UNIFORM = 1;
    
    public abstract List<Tarefa> toTarefaList(RedeDeFilas rdf);

    @Override
    public abstract String toString();

    public abstract int getTipo();
}
