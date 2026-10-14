#!/usr/bin/env bash
set -e
cd "$(dirname "$0")"
rm -rf out-test && mkdir out-test
javac --release 17 -d out-test $(find src test -name '*.java')
java -cp out-test testes.Runner
