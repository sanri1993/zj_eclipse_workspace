#!/bin/bash
set -uex

# build hadoop jar package
# before check maven.compiler.source=1.7
mvn clean package
mv target/zj-mvn-demo.jar /tmp/hadoop_test


set +uex
