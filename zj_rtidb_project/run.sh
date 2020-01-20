#!/bin/bash
set -uex

if [[ $1 == "jar" ]]; then
  echo "run clean and package."
  mvn clean package -Dmaven.test.skip=true
fi

echo "Rtidb app build DONE."

set +uex
