package testes;

import terminal.Cor;

/** Testes unitários das cores do terminal. */
public class CorTest {

    @Teste
    public void semCorOTextoFicaIntacto() {
        Cor.configurar("nao");
        Afirma.igual("texto", Cor.verde("texto"), "verde sem cor");
        Afirma.igual("texto", Cor.status(true, "texto"), "status sem cor");
    }

    @Teste
    public void conflitoFicaAmareloETudoCertoFicaVerde() {
        Cor.configurar("sim");
        try {
            Afirma.verdadeiro(Cor.status(true, "x").startsWith("\u001B[33m"), "conflito em amarelo (33)");
            Afirma.verdadeiro(Cor.status(false, "x").startsWith("\u001B[32m"), "ok em verde (32)");
            Afirma.verdadeiro(Cor.status(true, "x").endsWith("\u001B[0m"), "deve resetar a cor no final");
        } finally {
            Cor.configurar("nao");
        }
    }
}
