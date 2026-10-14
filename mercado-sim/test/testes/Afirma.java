package testes;

import java.util.Objects;

/** Asserções mínimas, sem dependências externas. */
public final class Afirma {
    private Afirma() { }

    public static void verdadeiro(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
    }

    public static void igual(Object esperado, Object obtido, String msg) {
        if (!Objects.equals(esperado, obtido))
            throw new AssertionError(msg + " (esperado: " + esperado + ", obtido: " + obtido + ")");
    }
}
