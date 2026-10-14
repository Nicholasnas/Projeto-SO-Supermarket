package escalonador;

import modelo.Compra;

/** Decide qual compra da fila será atendida a seguir. Implementações são thread-safe. */
public interface Escalonador {
    void adicionar(Compra c);

    /** Próxima compra a ser atendida, ou null se a fila estiver vazia. */
    Compra proxima();

    String nome();
}
