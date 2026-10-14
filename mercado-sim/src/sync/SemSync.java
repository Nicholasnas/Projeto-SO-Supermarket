package sync;

import modelo.Produto;
import java.util.List;

/** Modo "assíncrono sem controle": ninguém espera ninguém, e o estoque pode ser corrompido. */
public class SemSync implements EstrategiaSync {
    public void executar(List<Produto> produtos, Runnable corpo) { corpo.run(); }
    public String nome() { return "SEM"; }
}
