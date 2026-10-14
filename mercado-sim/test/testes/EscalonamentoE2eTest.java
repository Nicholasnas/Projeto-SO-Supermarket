package testes;

import app.Config;
import mercado.Mercado;
import mercado.Resultado;
import modelo.Compra;
import modelo.TipoCliente;
import java.util.ArrayList;
import java.util.List;

/**
 * E2E do escalonamento: com UM caixa e todos os clientes chegando de uma vez, a ordem de atendimento
 * depende só do escalonador. O estoque é grande para a sincronização não interferir.
 */
public class EscalonamentoE2eTest {

    private Resultado rodar(String escalonador) throws Exception {
        Config c = new Config();
        c.escalonador = escalonador;
        c.sync = "GLOBAL";
        c.caixas = 1;
        c.clientes = 40;
        c.buffer = 40;
        c.intervaloMs = 0;
        c.estoque = 100_000;
        return Mercado.executarPadrao(c);
    }

    private static List<Compra> ordemDeAtendimento(Resultado r) {
        List<Compra> ordem = new ArrayList<>(r.compras);
        ordem.sort((a, b) -> Long.compare(a.inicio, b.inicio));
        return ordem;
    }

    @Teste
    public void fcfsAtendeEmOrdemDeChegada() throws Exception {
        List<Compra> ordem = ordemDeAtendimento(rodar("FCFS"));
        for (int i = 0; i < ordem.size(); i++) Afirma.igual(i, ordem.get(i).id, "posição " + i);
    }

    @Teste
    public void preferencialEsperaMuitoMenosQueNormal() throws Exception {
        Resultado r = rodar("PREFERENCIAL");
        Afirma.verdadeiro(r.esperaPreferencial * 2 < r.esperaNormal,
                "espera preferencial (" + r.esperaPreferencial + ") deveria ser bem menor que normal (" + r.esperaNormal + ")");
    }

    @Teste
    public void quantidadeReduzAEsperaMediaEPrejudicaOsCarrinhosGrandes() throws Exception {
        Resultado fcfs = rodar("FCFS");
        Resultado qtd = rodar("QUANTIDADE");
        Afirma.verdadeiro(qtd.esperaMedia < fcfs.esperaMedia,
                "QUANTIDADE (" + qtd.esperaMedia + ") deveria ter espera média menor que FCFS (" + fcfs.esperaMedia + ")");

        double esperaGrandes = 0; int n = 0;
        for (Compra c : qtd.compras) if (c.unidades >= 8) { esperaGrandes += (c.inicio - c.chegada) / 1e6; n++; }
        double esperaPequenos = 0; int m = 0;
        for (Compra c : qtd.compras) if (c.unidades <= 3) { esperaPequenos += (c.inicio - c.chegada) / 1e6; m++; }
        Afirma.verdadeiro(n > 0 && m > 0, "a carga deve ter carrinhos grandes e pequenos");
        Afirma.verdadeiro(esperaPequenos / m < esperaGrandes / n, "carrinhos pequenos esperam menos que os grandes");
    }

    @Teste
    public void escalonadorNaoMudaACorretudeDoEstoque() throws Exception {
        for (String esc : new String[]{"FCFS", "QUANTIDADE", "PREFERENCIAL"}) {
            Resultado r = rodar(esc);
            Afirma.verdadeiro(!r.verificador.haConflito(), esc + ": sem conflito de estoque");
            Afirma.igual(0, r.verificador.comprasPerdidasOuDuplicadas, esc + ": nada perdido");
            Afirma.igual(40, r.concluidas, esc + ": todas concluídas");
        }
    }

    @Teste
    public void tipoDeClienteChegaAoCupomEAsMetricas() throws Exception {
        Resultado r = rodar("PREFERENCIAL");
        long prefs = r.compras.stream().filter(c -> c.tipo == TipoCliente.PREFERENCIAL).count();
        Afirma.verdadeiro(prefs > 0 && prefs < r.compras.size(), "a carga mistura os dois tipos de cliente");
        Afirma.verdadeiro(r.esperaMaxima >= r.esperaMedia, "espera máxima >= espera média");
    }
}
