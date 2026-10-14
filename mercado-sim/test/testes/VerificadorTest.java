package testes;

import modelo.Compra;
import modelo.Produto;
import modelo.TipoCliente;
import monitor.Verificador;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/** Testes unitários do verificador que concilia cupons e estoque. */
public class VerificadorTest {

    private static Compra comCupom(int id, int vendido) {
        Compra c = Fabrica.compra(id, TipoCliente.NORMAL, 0, 3);
        c.cupom = new modelo.Cupom();
        c.cupom.adicionar("Arroz", 3, vendido, 2000);
        return c;
    }

    private static AtomicInteger[] execucoes(int... valores) {
        AtomicInteger[] a = new AtomicInteger[valores.length];
        for (int i = 0; i < a.length; i++) a[i] = new AtomicInteger(valores[i]);
        return a;
    }

    @Teste
    public void detectaVendaAcimaDoEstoque() {
        Produto arroz = new Produto(0, "Arroz", 2000, 5);
        arroz.estoque = 2;                    // dois caixas gravaram 5 - 3
        Verificador v = new Verificador(List.of(arroz), List.of(comCupom(0, 3), comCupom(1, 3)), execucoes(1, 1));
        Afirma.verdadeiro(v.haConflito(), "deve haver conflito");
        Afirma.igual(1L, v.unidadesVendidasAMais(), "vendeu 6 de um estoque de 5");
        Afirma.igual(3L, v.divergenciaTotal(), "esperado -1, real 2");
        Afirma.igual(1, v.produtosComConflito(), "produtos com conflito");
    }

    @Teste
    public void naoAcusaConflitoQuandoTudoBate() {
        Produto arroz = new Produto(0, "Arroz", 2000, 5);
        arroz.estoque = 2;
        Verificador v = new Verificador(List.of(arroz), List.of(comCupom(0, 3), comCupom(1, 0)), execucoes(1, 1));
        Afirma.verdadeiro(!v.haConflito(), "não deve haver conflito");
        Afirma.igual(0L, v.unidadesVendidasAMais(), "nada vendido a mais");
        Afirma.igual(0L, v.divergenciaTotal(), "sem divergência");
    }

    @Teste
    public void detectaEstoqueQueNaoBateMesmoSemVenderAMais() {
        Produto arroz = new Produto(0, "Arroz", 2000, 10);
        arroz.estoque = 7;                    // 10 - 3 vendidas = 7
        Verificador ok = new Verificador(List.of(arroz), List.of(comCupom(0, 3)), execucoes(1));
        Afirma.verdadeiro(!ok.haConflito(), "10 - 3 = 7 está correto");

        arroz.estoque = 8;                    // uma gravação foi perdida
        Verificador ruim = new Verificador(List.of(arroz), List.of(comCupom(0, 3)), execucoes(1));
        Afirma.verdadeiro(ruim.haConflito(), "estoque 8 não bate com 3 un vendidas de 10");
        Afirma.igual(0L, ruim.unidadesVendidasAMais(), "não vendeu além do estoque");
    }

    @Teste
    public void detectaCompraPerdidaOuDuplicada() {
        Produto arroz = new Produto(0, "Arroz", 2000, 5);
        Verificador v = new Verificador(List.of(arroz), List.of(), execucoes(1, 0, 2));
        Afirma.igual(2, v.comprasPerdidasOuDuplicadas, "uma perdida (0) e uma duplicada (2)");
    }
}
