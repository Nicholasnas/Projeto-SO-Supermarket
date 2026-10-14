package testes;

import escalonador.*;
import modelo.Compra;
import modelo.TipoCliente;
import java.util.ArrayList;
import java.util.List;

/** Testes unitários: cada escalonador devolve as compras na ordem esperada. */
public class EscalonadorTest {

    private static List<Integer> esvaziar(Escalonador e) {
        List<Integer> ids = new ArrayList<>();
        Compra c;
        while ((c = e.proxima()) != null) ids.add(c.id);
        return ids;
    }

    @Teste
    public void fcfsAtendeNaOrdemDeChegada() {
        Escalonador e = new Fcfs();
        e.adicionar(Fabrica.compra(0, TipoCliente.NORMAL, 0, 9));
        e.adicionar(Fabrica.compra(1, TipoCliente.PREFERENCIAL, 0, 1));
        e.adicionar(Fabrica.compra(2, TipoCliente.NORMAL, 0, 5));
        Afirma.igual(List.of(0, 1, 2), esvaziar(e), "ordem FCFS");
    }

    @Teste
    public void quantidadeAtendeMenorCarrinhoPrimeiro() {
        Escalonador e = new PorQuantidade();
        e.adicionar(Fabrica.compra(0, TipoCliente.NORMAL, 0, 5));
        e.adicionar(Fabrica.compra(1, TipoCliente.NORMAL, 0, 1));
        e.adicionar(Fabrica.compra(2, TipoCliente.NORMAL, 0, 3));
        Afirma.igual(List.of(1, 2, 0), esvaziar(e), "ordem por quantidade");
    }

    @Teste
    public void quantidadeDesempataPelaChegada() {
        Escalonador e = new PorQuantidade();
        e.adicionar(Fabrica.compra(5, TipoCliente.NORMAL, 0, 2));
        e.adicionar(Fabrica.compra(2, TipoCliente.NORMAL, 0, 2));
        Afirma.igual(List.of(2, 5), esvaziar(e), "empate resolvido pelo menor id");
    }

    @Teste
    public void preferencialPassaNaFrenteDoNormal() {
        Escalonador e = new PorPreferencia();
        e.adicionar(Fabrica.compra(0, TipoCliente.NORMAL, 0, 1));
        e.adicionar(Fabrica.compra(1, TipoCliente.PREFERENCIAL, 0, 9));
        e.adicionar(Fabrica.compra(2, TipoCliente.NORMAL, 0, 1));
        e.adicionar(Fabrica.compra(3, TipoCliente.PREFERENCIAL, 0, 9));
        Afirma.igual(List.of(1, 3, 0, 2), esvaziar(e), "preferenciais primeiro, cada grupo por chegada");
    }

    @Teste
    public void filaVaziaDevolveNull() {
        Afirma.verdadeiro(new Fcfs().proxima() == null, "FCFS vazio");
        Afirma.verdadeiro(new PorQuantidade().proxima() == null, "QUANTIDADE vazio");
        Afirma.verdadeiro(new PorPreferencia().proxima() == null, "PREFERENCIAL vazio");
    }
}
