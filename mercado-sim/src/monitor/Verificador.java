package monitor;

import modelo.Compra;
import modelo.Produto;
import terminal.Cor;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Concilia os cupons com o estoque. Para cada produto:
 * <pre>estoque_esperado = estoque_inicial - unidades_vendidas_nos_cupons</pre>
 * Se o estoque real for diferente, ou se os cupons venderam mais do que existia, houve conflito.
 */
public class Verificador {
    public record LinhaProduto(String nome, long inicial, long vendido, long esperado, long obtido) {
        public boolean conflito() { return esperado != obtido || vendido > inicial; }
    }

    public final List<LinhaProduto> linhas = new ArrayList<>();
    public final int comprasPerdidasOuDuplicadas;

    public Verificador(List<Produto> produtos, List<Compra> compras, AtomicInteger[] execucoes) {
        long[] vendido = new long[produtos.size()];
        for (Compra c : compras) {
            if (c.cupom == null) continue;
            for (var l : c.cupom.linhas()) {
                for (Produto p : produtos) if (p.nome.equals(l.produto())) vendido[p.id] += l.vendido();
            }
        }
        for (Produto p : produtos) {
            long esperado = p.estoqueInicial - vendido[p.id];
            linhas.add(new LinhaProduto(p.nome, p.estoqueInicial, vendido[p.id], esperado, p.estoque));
        }
        int anomalias = 0;
        for (AtomicInteger e : execucoes) if (e.get() != 1) anomalias++;
        this.comprasPerdidasOuDuplicadas = anomalias;
    }

    public int produtosComConflito() { return (int) linhas.stream().filter(LinhaProduto::conflito).count(); }

    public boolean haConflito() { return produtosComConflito() > 0; }

    /** Unidades que os cupons venderam além do que existia em estoque (venda de produto que não havia). */
    public long unidadesVendidasAMais() {
        return linhas.stream().mapToLong(l -> Math.max(0, l.vendido() - l.inicial())).sum();
    }

    /** Diferença total entre o estoque esperado e o real (unidades "fantasma"). */
    public long divergenciaTotal() {
        return linhas.stream().mapToLong(l -> Math.abs(l.obtido() - l.esperado())).sum();
    }

    public void imprimir() {
        System.out.println(Cor.negrito("  Conferência (cupons x estoque):"));
        System.out.println(Cor.negrito(String.format("    %-10s %8s %9s %9s %7s  %s", "Produto", "Inicial", "Vendido", "Esperado", "Real", "Situação")));
        for (LinhaProduto l : linhas) {
            String s = l.conflito() ? (l.vendido() > l.inicial() ? "CONFLITO: vendeu mais do que havia" : "CONFLITO: estoque não bate") : "ok";
            System.out.println(Cor.status(l.conflito(), String.format("    %-10s %8d %9d %9d %7d  %s",
                    l.nome(), l.inicial(), l.vendido(), l.esperado(), l.obtido(), s)));
        }
    }
}
