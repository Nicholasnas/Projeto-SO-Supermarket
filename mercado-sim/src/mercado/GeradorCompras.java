package mercado;

import app.Config;
import modelo.Compra;
import modelo.ItemCompra;
import modelo.Produto;
import modelo.TipoCliente;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Gera produtos e compras. A mesma seed produz sempre exatamente a mesma carga. */
public class GeradorCompras {
    private static final String[] NOMES = {"Arroz", "Feijao", "Leite", "Cafe", "Acucar", "Macarrao", "Oleo", "Sal"};

    public static List<Produto> criarProdutos(Config c) {
        Random r = new Random(c.seed);
        List<Produto> lista = new ArrayList<>();
        int n = Math.min(c.produtos, NOMES.length);
        for (int i = 0; i < n; i++) lista.add(new Produto(i, NOMES[i], 300 + r.nextInt(2700), c.estoque));
        return lista;
    }

    public static List<Compra> criarCompras(Config c, int totalProdutos) {
        Random r = new Random(c.seed + 1);
        List<Compra> lista = new ArrayList<>();
        for (int i = 0; i < c.clientes; i++) {
            TipoCliente tipo = r.nextInt(100) < 30 ? TipoCliente.PREFERENCIAL : TipoCliente.NORMAL;
            int nItens = 1 + r.nextInt(Math.min(5, totalProdutos));
            List<Integer> ids = new ArrayList<>();
            for (int p = 0; p < totalProdutos; p++) ids.add(p);
            Collections.shuffle(ids, r); // a ordem dos produtos no carrinho varia de cliente para cliente
            List<ItemCompra> itens = new ArrayList<>();
            for (int k = 0; k < nItens; k++) itens.add(new ItemCompra(ids.get(k), 1 + r.nextInt(3)));
            lista.add(new Compra(i, "Cliente " + i, tipo, itens, c.msPorUnidade));
        }
        return lista;
    }
}
