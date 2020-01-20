#!/bin/bash
set -uex

if [[ $1 == "jar" ]]; then
  # build jar package for hadoop, flink
  # if hadoop, check maven.compiler.source=1.7
  echo "run clean and package."
  mvn clean package -Dmaven.test.skip=true
fi

echo "Rtidb project build DONE."

set +uex
