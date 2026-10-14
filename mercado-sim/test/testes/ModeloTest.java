package testes;

import app.Config;
import mercado.GeradorCompras;
import modelo.Compra;
import modelo.Cupom;
import modelo.ItemCompra;
import modelo.Produto;
import modelo.TipoCliente;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Testes unitários: compra, cupom fiscal e gerador de carga. */
public class ModeloTest {

    @Teste
    public void compraCalculaUnidadesEBurst() {
        Compra c = new Compra(0, "Ana", TipoCliente.NORMAL,
                List.of(new ItemCompra(0, 3), new ItemCompra(1, 2)), 4);
        Afirma.igual(5, c.unidades, "unidades do carrinho");
        Afirma.igual(20L, c.burst, "burst = unidades x ms por unidade");
    }

    @Teste
    public void cupomSomaApenasOQueFoiVendido() {
        Cupom cupom = new Cupom();
        cupom.adicionar("Arroz", 2, 2, 500);
        cupom.adicionar("Feijao", 3, 0, 800);   // sem estoque: não cobra
        Afirma.igual(1000L, cupom.total(), "total do cupom");
    }

    @Teste
    public void cupomMostraItemSemEstoque() {
        Cupom cupom = new Cupom();
        cupom.adicionar("Arroz", 3, 0, 2000);
        String texto = cupom.formatar(Fabrica.compra(7, TipoCliente.NORMAL, 0, 3));
        Afirma.verdadeiro(texto.contains("SEM ESTOQUE"), "cupom deve avisar falta de estoque");
        Afirma.verdadeiro(texto.contains("#7"), "cupom deve trazer o número da compra");
    }

    @Teste
    public void formataValorEmReais() {
        Afirma.igual("R$ 10,05", Cupom.reais(1005), "formato do valor");
        Afirma.igual("R$ 0,00", Cupom.reais(0), "zero");
    }

    @Teste
    public void produtoComecaComEstoqueInicialESemaforoLivre() {
        Produto p = new Produto(0, "Arroz", 2000, 5);
        Afirma.igual(5L, p.estoque, "estoque");
        Afirma.igual(5L, p.estoqueInicial, "estoque inicial");
        Afirma.igual(1, p.semaforo.availablePermits(), "semáforo binário começa livre");
    }

    @Teste
    public void mesmaSeedGeraMesmaCarga() {
        Config c = new Config();
        List<Compra> a = GeradorCompras.criarCompras(c, 6);
        List<Compra> b = GeradorCompras.criarCompras(c, 6);
        Afirma.igual(a.size(), b.size(), "tamanho");
        for (int i = 0; i < a.size(); i++) {
            Afirma.igual(a.get(i).tipo, b.get(i).tipo, "tipo da compra " + i);
            Afirma.igual(a.get(i).itens, b.get(i).itens, "itens da compra " + i);
        }
    }

    @Teste
    public void seedDiferenteGeraCargaDiferente() {
        Config c1 = new Config();
        Config c2 = new Config();
        c2.seed = 7;
        List<Compra> a = GeradorCompras.criarCompras(c1, 6);
        List<Compra> b = GeradorCompras.criarCompras(c2, 6);
        boolean diferente = false;
        for (int i = 0; i < a.size(); i++) if (!a.get(i).itens.equals(b.get(i).itens)) diferente = true;
        Afirma.verdadeiro(diferente, "seeds diferentes devem produzir cargas diferentes");
    }

    @Teste
    public void carrinhoNaoRepeteProdutoEFicaDentroDoLimite() {
        Config c = new Config();
        List<Produto> produtos = GeradorCompras.criarProdutos(c);
        Afirma.igual(c.produtos, produtos.size(), "quantidade de produtos");
        List<Compra> compras = GeradorCompras.criarCompras(c, produtos.size());
        Afirma.igual(c.clientes, compras.size(), "quantidade de clientes");
        for (Compra compra : compras) {
            Set<Integer> ids = new HashSet<>();
            for (ItemCompra i : compra.itens) {
                Afirma.verdadeiro(ids.add(i.produtoId()), "produto repetido no carrinho " + compra.id);
                Afirma.verdadeiro(i.produtoId() >= 0 && i.produtoId() < produtos.size(), "produto inexistente");
                Afirma.verdadeiro(i.quantidade() >= 1 && i.quantidade() <= 3, "quantidade fora de 1..3");
            }
            Afirma.verdadeiro(compra.unidades >= 1, "carrinho vazio");
        }
    }
}
