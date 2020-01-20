#!/bin/bash
set -uex

if [[ $1 == "jar" ]]; then
  echo "run clean and package."
  mvn clean package -Dmaven.test.skip=true
fi

echo "Jmeter app build DONE."

set +uex
