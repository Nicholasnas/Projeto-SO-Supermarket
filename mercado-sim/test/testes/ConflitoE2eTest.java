package testes;

import app.Config;
import mercado.Mercado;
import mercado.Resultado;
import modelo.Compra;
import modelo.ItemCompra;
import modelo.Produto;
import modelo.TipoCliente;
import java.util.List;

/** E2E: o sistema inteiro (threads, fila, escalonador, sincronização, cupom, verificador). */
public class ConflitoE2eTest {

    /** Cenário da demo: estoque 5, dois clientes querem 3 un cada, em caixas diferentes. */
    private Resultado duelo(String sync) throws Exception {
        Config c = new Config();
        c.sync = sync;
        c.caixas = 2;
        c.janelaMs = 100;
        c.intervaloMs = 0;
        List<Produto> produtos = List.of(new Produto(0, "Arroz", 2000, 5));
        List<Compra> compras = List.of(
                new Compra(0, "Ana", TipoCliente.NORMAL, List.of(new ItemCompra(0, 3)), c.msPorUnidade),
                new Compra(1, "Bruno", TipoCliente.NORMAL, List.of(new ItemCompra(0, 3)), c.msPorUnidade));
        return Mercado.executar(c, produtos, compras);
    }

    @Teste
    public void semSincronizacaoOsDoisCuponsVendemOUltimoItem() throws Exception {
        Resultado r = duelo("SEM");
        Afirma.igual(2, r.concluidas, "as duas compras terminam");
        Afirma.igual(3L, r.compras.get(0).cupom.total() / 2000, "Ana levou 3 un");
        Afirma.igual(3L, r.compras.get(1).cupom.total() / 2000, "Bruno também levou 3 un");
        Afirma.verdadeiro(r.verificador.haConflito(), "conflito esperado");
        Afirma.igual(6L, r.verificador.linhas.get(0).vendido(), "os cupons venderam 6 un");
        Afirma.igual(2L, r.verificador.linhas.get(0).obtido(), "estoque real ficou em 2 (devia ser -1)");
        Afirma.igual(1L, r.verificador.unidadesVendidasAMais(), "1 un vendida a mais");
    }

    @Teste
    public void comSemaforoApenasUmClienteLevaOProduto() throws Exception {
        for (String sync : new String[]{"GLOBAL", "POR_PRODUTO"}) {
            Resultado r = duelo(sync);
            long totalA = r.compras.get(0).cupom.total();
            long totalB = r.compras.get(1).cupom.total();
            Afirma.igual(6000L, totalA + totalB, sync + ": só uma compra de 3 un x R$ 20,00 é cobrada");
            Afirma.verdadeiro(totalA == 0 || totalB == 0, sync + ": o outro cupom deve ser R$ 0,00");
            Afirma.verdadeiro(!r.verificador.haConflito(), sync + ": sem conflito");
            Afirma.igual(2L, r.verificador.linhas.get(0).obtido(), sync + ": estoque final");
            String cupomSemEstoque = (totalA == 0 ? r.compras.get(0) : r.compras.get(1)).cupom
                    .formatar(r.compras.get(0));
            Afirma.verdadeiro(cupomSemEstoque.contains("SEM ESTOQUE"), sync + ": cupom avisa falta de estoque");
        }
    }

    @Teste
    public void umUnicoCaixaNaoTemConflitoMesmoSemSincronizacao() throws Exception {
        Config c = new Config();
        c.sync = "SEM";
        c.caixas = 1;
        c.clientes = 40;
        c.intervaloMs = 0;
        Resultado r = Mercado.executarPadrao(c);
        Afirma.igual(c.clientes, r.concluidas, "todas concluídas");
        Afirma.verdadeiro(!r.verificador.haConflito(), "sem concorrência não há corrida");
    }

    @Teste
    public void simulacaoCompletaSemSincronizacaoGeraConflito() throws Exception {
        Config c = new Config();
        c.sync = "SEM";
        c.janelaMs = 5;
        Resultado r = Mercado.executarPadrao(c);
        Afirma.igual(c.clientes, r.concluidas, "todas concluídas");
        Afirma.verdadeiro(r.verificador.haConflito(), "deve haver conflito de estoque");
        Afirma.verdadeiro(r.verificador.unidadesVendidasAMais() > 0, "cupons vendem mais do que havia");
    }

    @Teste
    public void simulacaoCompletaComSemaforosNuncaTemConflito() throws Exception {
        for (String sync : new String[]{"GLOBAL", "POR_PRODUTO"}) {
            for (String esc : new String[]{"FCFS", "QUANTIDADE", "PREFERENCIAL"}) {
                Config c = new Config();
                c.sync = sync;
                c.escalonador = esc;
                c.janelaMs = 5;
                Resultado r = Mercado.executarPadrao(c);
                String cfg = sync + "/" + esc;
                Afirma.igual(c.clientes, r.concluidas, cfg + ": todas concluídas");
                Afirma.verdadeiro(!r.verificador.haConflito(), cfg + ": estoque deve bater com os cupons");
                Afirma.igual(0, r.verificador.comprasPerdidasOuDuplicadas, cfg + ": nenhuma compra perdida ou duplicada");
                Afirma.igual(0L, r.verificador.unidadesVendidasAMais(), cfg + ": nada vendido além do estoque");
                for (var l : r.verificador.linhas)
                    Afirma.verdadeiro(l.obtido() >= 0, cfg + ": estoque negativo em " + l.nome());
            }
        }
    }

    @Teste
    public void filaMinimaDeUmaVagaNaoPerdeNemDuplicaCompras() throws Exception {
        Config c = new Config();
        c.sync = "POR_PRODUTO";
        c.buffer = 1;
        c.clientes = 30;
        Resultado r = Mercado.executarPadrao(c);
        Afirma.igual(30, r.concluidas, "todas concluídas");
        Afirma.igual(0, r.verificador.comprasPerdidasOuDuplicadas, "cada compra atendida exatamente uma vez");
    }

    @Teste
    public void produtoSemEstoqueDeixaOCupomZeradoSemQuebrarOSistema() throws Exception {
        Config c = new Config();
        c.sync = "GLOBAL";
        c.estoque = 0;
        c.clientes = 10;
        Resultado r = Mercado.executarPadrao(c);
        Afirma.igual(10, r.concluidas, "todas concluídas");
        for (Compra compra : r.compras) Afirma.igual(0L, compra.cupom.total(), "nada a cobrar sem estoque");
        Afirma.verdadeiro(!r.verificador.haConflito(), "sem conflito");
    }

    @Teste
    public void servicoComTrabalhoDeCpuTambemFechaOEstoque() throws Exception {
        Config c = new Config();
        c.sync = "POR_PRODUTO";
        c.servico = "cpu";
        c.clientes = 20;
        Resultado r = Mercado.executarPadrao(c);
        Afirma.igual(20, r.concluidas, "todas concluídas");
        Afirma.verdadeiro(!r.verificador.haConflito(), "semáforos continuam evitando o conflito com serviço em CPU");
    }
}
