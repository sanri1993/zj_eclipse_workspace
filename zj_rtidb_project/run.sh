#!/bin/bash
set -uex

if [[ $1 == "jar" ]]; then
  # build jar package for hadoop, flink
  # if hadoop, check maven.compiler.source=1.7
  echo "run clean and package."
  mvn clean package -Dmaven.test.skip=true

  jar_dir="/tmp/target_jars"
  jar_file="zj-rtidb-app.jar"
  if [[ ! -d ${jar_dir} ]]; then
    mkdir ${jar_dir}
  fi
  mv target/${jar_file} ${jar_dir}/${jar_file}
fi

echo "Rtidb project build DONE."

set +uex
