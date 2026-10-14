package modelo;

import java.util.ArrayList;
import java.util.List;

/** Cupom fiscal: é onde o conflito de estoque fica visível para o cliente. */
public class Cupom {
    public record Linha(String produto, int pedido, int vendido, long precoUnitario) {
        long subtotal() { return vendido * precoUnitario; }
    }

    private final List<Linha> linhas = new ArrayList<>();

    public void adicionar(String produto, int pedido, int vendido, long precoUnitario) {
        linhas.add(new Linha(produto, pedido, vendido, precoUnitario));
    }

    public List<Linha> linhas() { return linhas; }

    public long total() { return linhas.stream().mapToLong(Linha::subtotal).sum(); }

    public static String reais(long centavos) {
        return String.format("R$ %d,%02d", centavos / 100, centavos % 100);
    }

    public String formatar(Compra c) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("  CUPOM FISCAL #%d - %s (%s)%n", c.id, c.cliente, c.tipo));
        for (Linha l : linhas) {
            if (l.vendido() > 0)
                sb.append(String.format("    %-12s %2d un x %-9s = %s%n", l.produto(), l.vendido(),
                        reais(l.precoUnitario()), reais(l.subtotal())));
            else
                sb.append(String.format("    %-12s SEM ESTOQUE (pediu %d)%n", l.produto(), l.pedido()));
        }
        sb.append(String.format("    TOTAL: %s%n", reais(total())));
        return sb.toString();
    }
}
