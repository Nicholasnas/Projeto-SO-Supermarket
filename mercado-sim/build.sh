#!/usr/bin/env bash
set -e
cd "$(dirname "$0")"
rm -rf out && mkdir out
javac --release 17 -d out $(find src -name '*.java')
echo "Compilado em out/. Execute: java -cp out app.Main --modo=demo"
