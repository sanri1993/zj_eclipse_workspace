#!/bin/bash
set -uex

if [[ $1 == "jar" ]]; then
  echo "run clean and package."
  mvn clean package -Dmaven.test.skip=true

  jar_dir="/tmp/hadoop_test"
  jar_file="zj-hadoop-app.jar"
  if [[ ! -d ${jar_dir} ]]; then
    mkdir ${jar_dir}
  fi
  mv target/${jar_file} ${jar_dir}/${jar_file}
fi

echo "Hadoop app build DONE."

set +uex
