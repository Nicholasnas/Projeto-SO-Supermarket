package testes;

import escalonador.Fcfs;
import escalonador.PorQuantidade;
import mercado.FilaProntos;
import modelo.Compra;
import modelo.TipoCliente;

/** Testes unitários da fila de prontos (semáforos vazio/cheio). */
public class FilaProntosTest {

    @Teste
    public void filaVaziaBloqueiaOCaixaAteOTimeout() throws Exception {
        FilaProntos fila = new FilaProntos(new Fcfs(), 3);
        long ini = System.nanoTime();
        Compra c = fila.consumir(80);
        long ms = (System.nanoTime() - ini) / 1_000_000;
        Afirma.verdadeiro(c == null, "fila vazia deve devolver null");
        Afirma.verdadeiro(ms >= 70, "o caixa deveria ter esperado o timeout (esperou " + ms + " ms)");
    }

    @Teste
    public void produzirMarcaChegadaEConsumirDevolveACompra() throws Exception {
        FilaProntos fila = new FilaProntos(new Fcfs(), 3);
        Compra c = Fabrica.compra(0, TipoCliente.NORMAL, 0, 1);
        fila.produzir(c);
        Afirma.verdadeiro(c.chegada > 0, "chegada deve ser registrada");
        Afirma.verdadeiro(fila.consumir(100) == c, "deve devolver a mesma compra");
    }

    @Teste
    public void aFilaObedeceOEscalonador() throws Exception {
        FilaProntos fila = new FilaProntos(new PorQuantidade(), 5);
        fila.produzir(Fabrica.compra(0, TipoCliente.NORMAL, 0, 5));
        fila.produzir(Fabrica.compra(1, TipoCliente.NORMAL, 0, 1));
        Afirma.igual(1, fila.consumir(100).id, "primeiro a sair: menor carrinho");
        Afirma.igual(0, fila.consumir(100).id, "segundo a sair");
    }

    @Teste
    public void filaCheiaBloqueiaOClienteAteLiberarVaga() throws Exception {
        FilaProntos fila = new FilaProntos(new Fcfs(), 2);
        fila.produzir(Fabrica.compra(0, TipoCliente.NORMAL, 0, 1));
        fila.produzir(Fabrica.compra(1, TipoCliente.NORMAL, 0, 1));

        Thread cliente = new Thread(() -> {
            try { fila.produzir(Fabrica.compra(2, TipoCliente.NORMAL, 0, 1)); }
            catch (InterruptedException ignorado) { }
        });
        cliente.start();
        Thread.sleep(150);
        Afirma.verdadeiro(cliente.isAlive(), "com a fila cheia, o 3º cliente deve ficar bloqueado");

        fila.consumir(100);                 // libera uma vaga (V no semáforo vazio)
        cliente.join(1000);
        Afirma.verdadeiro(!cliente.isAlive(), "ao liberar a vaga, o cliente deve conseguir entrar");
    }
}
