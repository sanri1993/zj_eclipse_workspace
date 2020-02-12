#!/bin/bash
set -uex

if [[ $1 == "jar" ]]; then
  echo "run mvn clean and package."
  mvn clean package -Dmaven.test.skip=true
fi

if [[ $1 == "exec" ]]; then
  echo "run ritdb app."
  java -cp target/zj-rtidb-app.jar zhengjin.rtidb.app.RtidbApp
fi

echo "Rtidb app DONE."

set +uex
