#!/bin/bash
set -uex

if [[ $1 == "main" ]]; then
  # build hadoop jar package
  # before check maven.compiler.source=1.7
  echo "run maven clean and package."
  mvn clean package
  mv target/zj-mvn-demo.jar /tmp/hadoop_test
fi

if [[ $1 == "check" ]]; then
  echo "run checkstyle."
  mvn checkstyle:checkstyle
fi

if [[ $1 == "cover" ]]; then
  echo "run code coverage."
  mvn clean cobertura:cobertura
fi

if [[ $1 == "checkall" ]]; then
  echo "run checkstyle and coverage."
  mvn clean checkstyle:checkstyle cobertura:cobertura
fi

if [[ $1 == "site" ]]; then
  echo "run project info site."
  mvn clean site:site
fi

echo "Java build DONE."

set +uex
