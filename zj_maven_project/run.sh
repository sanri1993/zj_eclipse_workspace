#!/bin/bash
set -uex

if [ $1 == "main" ]; then
  # build hadoop jar package
  # before check maven.compiler.source=1.7
  echo "run maven clean and package."
  mvn clean package
  mv target/zj-mvn-demo.jar /tmp/hadoop_test
fi

if [ $1 == "check" ]; then
  echo "run checkstyle."
  #mvn checkstyle:checkstyle
  mvn site:site
fi

echo "Java build DONE."

set +uex
