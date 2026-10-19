# Simulador de Mercado: escalonamento e sincronização

Projeto didático de Sistemas Operacionais (Java 17+, sem dependências).
Vários clientes compram ao mesmo tempo em caixas diferentes (threads). Todos mexem no **mesmo estoque**.

```
Clientes ──[semáforo vazio]──► Fila de prontos ──[semáforo cheio]──► Caixas (threads) ──► Estoque
(1 thread)                     o ESCALONADOR escolhe                  atendem e baixam      compartilhado
                               quem vai primeiro                      o estoque             (semáforo/produto)
```

## Como executar

```bash
./build.sh
java -cp out app.Main --modo=demo        # 1) o conflito aparecendo no cupom fiscal
java -cp out app.Main --modo=comparar    # 2) experimentos para tirar conclusões
java -cp out app.Main --modo=escala      # 3) variando caixas (threads) e tamanho da carga
java -cp out app.Main --escalonador=PREFERENCIAL --sync=GLOBAL   # simulação avulsa
```

Parâmetros (`--chave=valor`): `escalonador` (FCFS, QUANTIDADE, PREFERENCIAL), `sync` (SEM, GLOBAL, POR_PRODUTO), `clientes`, `produtos`, `estoque`, `caixas`, `seed`, `repeticoes`, `janela`,
`buffer`, `intervalo`, `msporunidade`, `servico` (sleep ou cpu), `cor` (auto, sim ou nao).

## E em outro computador? (núcleos e threads)

O tempo de atendimento é simulado com `sleep` por padrão, então ele quase não depende da máquina: a thread dorme e não usa CPU.
Com `--servico=cpu` cada atendimento gasta tempo de CPU de verdade, e aí o número de núcleos passa a importar.

```bash
./nucleos.sh 8 3     # 8 caixas, 3 repetições, limita a CPU a 1, 2, 4 e 8 núcleos (Linux, usa taskset)
```

Regra prática: com `N` caixas e `M` núcleos, o ganho para quando `M >= N`. Com menos núcleos que caixas, as threads se revezam.
O conflito de estoque no modo `SEM` aparece em qualquer máquina, porque nasce da concorrência e não da velocidade.

## Saída colorida

Linhas **verdes** não tiveram conflito de estoque; linhas **amarelas** tiveram (e os cupons conflitantes aparecem em amarelo na demo).
As cores ligam sozinhas em um terminal e desligam quando a saída vai para um arquivo ou pipe (ou com `NO_COLOR`).
No console do IntelliJ, que não é um terminal de verdade, use `--cor=sim`. Para desligar, `--cor=nao`.

## Conceitos de SO, e onde estão no código

| Conceito | Onde | O que mostrar |
| --- | --- | --- |
| **Thread** | `Caixa` (várias) e a thread `clientes` | Execução concorrente de verdade |
| **Condição de corrida** | `Caixa.atender`: ler estoque, pausa, gravar | Dois cupons vendem o mesmo último item |
| **Seção crítica** | trecho `sync.executar(...)` | Só quem tem o semáforo mexe no estoque |
| **Semáforo binário** (valor 1) | `Produto.semaforo`, `SemaforoGlobal` | `acquire()` = P, `release()` = V |
| **Semáforo contador** | `FilaProntos` (`vazio` e `cheio`) | Produtor-consumidor com buffer limitado |
| **Escalonamento** | pacote `escalonador` | Ordem de atendimento muda os tempos de espera |
| **Starvation** | `PorPreferencia` e `PorQuantidade` | Alguém espera muito mais que os outros |

## Escalonadores (fila de prioridades)

| Nome | Regra | Efeito esperado |
| --- | --- | --- |
| `FCFS` | Ordem de chegada | Justo, mas um carrinho grande atrasa os pequenos |
| `QUANTIDADE` | Menos produtos primeiro ("caixa rápido", como o SJF) | Menor espera média, mas carrinhos grandes esperam muito |
| `PREFERENCIAL` | Cliente preferencial primeiro | Preferenciais quase não esperam, normais esperam mais |

O tempo de atendimento de cada compra (`burst`) é proporcional à quantidade de produtos no carrinho.

## Sincronização: "assíncrono" contra "sincronizado"

| Modo | O que faz | Resultado |
| --- | --- | --- |
| `SEM` | Caixas mexem no estoque sem se coordenar | Cupons vendem produto que não existe |
| `GLOBAL` | Um semáforo para a loja inteira | Correto, mas uma compra por vez |
| `POR_PRODUTO` | Um semáforo por produto | Correto, e compras de produtos diferentes andam em paralelo |

**Por que o conflito acontece:** o caixa lê o estoque (5), demora um pouco e grava `5 - 3 = 2`.
Se outro caixa leu 5 antes dessa gravação, ele também grava 2. Duas vendas de 3 unidades saíram de um estoque de 5.

## Como a corretude é verificada

Para cada produto: `estoque_esperado = estoque_inicial - unidades_vendidas_nos_cupons`.
Se o estoque real for diferente, ou os cupons venderam mais do que havia, a execução tem **conflito**.
O verificador também confere se cada compra foi atendida exatamente uma vez.

## Roteiro de demonstração e conclusões

1. `--modo=demo`: veja os dois cupons cobrando o último item. Depois, o mesmo cenário com semáforo: um cupom
   informa "SEM ESTOQUE".
2. `--modo=comparar`:
   - **Experimento 1** (varia só a sincronização): `SEM` tem conflito em todas as execuções e é mais rápido, porque
     ninguém espera. `GLOBAL` e `POR_PRODUTO` têm zero conflito. Conclusão: **corretude custa desempenho**.
     A diferença entre `GLOBAL` e `POR_PRODUTO` é pequena nesta carga, e cresce com mais caixas e mais produtos.
   - **Experimento 2** (varia só o escalonador, com sincronização correta): `PREFERENCIAL` derruba a espera dos
     preferenciais e aumenta a dos normais. `QUANTIDADE` reduz a espera média, mas aumenta a espera máxima
     (carrinhos grandes ficam para trás). Conclusão: **o escalonador não muda a corretude, muda os tempos e quem
     espera mais**.

Sugestões para explorar: `--caixas=1` (sem concorrência, o conflito some), `--caixas=8`, `--estoque=1000`
(estoque sobrando: menos conflitos visíveis), `--buffer=3` (fila pequena: o semáforo `vazio` passa a bloquear clientes).

## Testes

```bash
./test.sh     # compila src/ e test/ e roda tudo (cerca de 15 s), sem dependências externas
```

| Classe | Tipo | O que garante |
| --- | --- | --- |
| `EscalonadorTest` | unitário | Ordem de saída de FCFS, QUANTIDADE e PREFERENCIAL, desempates, fila vazia |
| `ModeloTest` | unitário | Compra, cupom fiscal, gerador de carga determinístico por seed |
| `FilaProntosTest` | unitário | Semáforos: caixa espera com fila vazia, cliente espera com fila cheia |
| `SincronizacaoTest` | unitário | Sem controle perde atualizações; semáforos dão exclusão mútua, liberam sempre e (por produto) deixam produtos diferentes em paralelo |
| `VerificadorTest` | unitário | Detecta venda acima do estoque, estoque que não bate e compra perdida ou duplicada |
| `ConflitoE2eTest` | E2E | O conflito no cupom sem sincronização, ausência dele com semáforos em 6 combinações, 1 caixa sem conflito, fila de 1 vaga, estoque zero |
| `EscalonamentoE2eTest` | E2E | Com 1 caixa: FCFS por chegada, preferenciais esperam bem menos, QUANTIDADE reduz a espera média e prejudica os carrinhos grandes, corretude igual nos 3 escalonadores |

Os testes sobre tempo usam folgas largas (ex.: espera preferencial menos da metade da normal) para não falharem por oscilação da máquina.

