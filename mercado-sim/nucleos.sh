#!/usr/bin/env bash
# Simula "computadores com menos núcleos" limitando a CPU do processo (Linux, precisa de taskset).
# Uso: ./nucleos.sh [caixas] [repeticoes]    (padrão: 8 caixas, 3 repetições)
set -e
cd "$(dirname "$0")"
CAIXAS=${1:-8}
REP=${2:-3}
MAX=$(nproc)
[ -d out ] || ./build.sh >/dev/null

media_ms() { # $1=núcleos $2=servico $3=sync -> tempo médio em ms
  taskset -c "0-$(($1 - 1))" java -cp out app.Main --modo=simular --cor=nao --caixas="$CAIXAS" \
    --repeticoes="$REP" --servico="$2" --sync="$3" |
    awk '/^#/ { for (i = 1; i <= NF; i++) if ($i == "ms,") { soma += $(i-1); n++ } } END { printf "%.0f", soma / n }'
}
conflitos() { # $1=núcleos -> execuções com conflito no modo SEM (serviço cpu)
  taskset -c "0-$(($1 - 1))" java -cp out app.Main --modo=simular --cor=nao --caixas="$CAIXAS" \
    --repeticoes="$REP" --servico=cpu --sync=SEM | awk '/^Resumo/ { print $2 " de " $4 }'
}

echo "Máquina com $MAX núcleos lógicos. $CAIXAS caixas (threads), $REP repetições, escalonador FCFS."
printf "%-9s %-26s %-26s %s\n" "Núcleos" "Tempo (serviço = sleep)" "Tempo (serviço = cpu)" "Conflitos no modo SEM (cpu)"
for n in 1 2 4 8 16; do
  [ "$n" -le "$MAX" ] || break
  printf "%-9s %-26s %-26s %s\n" "$n" "$(media_ms $n sleep POR_PRODUTO) ms" "$(media_ms $n cpu POR_PRODUTO) ms" "$(conflitos $n)"
done
echo "Tempos com POR_PRODUTO. Em 'sleep' o tempo é simulado e quase não depende dos núcleos; em 'cpu' ele depende."
