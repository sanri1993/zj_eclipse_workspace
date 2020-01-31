#!/bin/bash
set -ue

target_jar="target/zj-jmeter-app.jar"

function create_jar() {
  echo "run clean and package."
  mvn clean package -Dmaven.test.skip=true
  cp ${target_jar} /tmp
}

if [[ $1 == "jar" ]]; then
  create_jar
fi

cls_path="${target_jar}:${HOME}/Workspaces/mvn_repository/junit/junit/4.12/junit-4.12.jar"
test_class="zhengjin.jmeter.junitsampler.JunitSampler01"
test_method="test02PostMethod"

echo "run test for class:"
echo "java -cp ${cls_path} org.junit.runner.JUnitCore ${test_class}"
if [[ $1 == "testc" ]]; then
  create_jar
  java -cp ${cls_path} org.junit.runner.JUnitCore ${test_class} 
fi

echo "run test for specified method:"
echo "java -cp ${cls_path} zhengjin.jmeter.app.SingleJUnitTestRunner ${test_class}#${test_method}"
if [[ $1 == "testm" ]]; then
  create_jar
  java -cp ${cls_path} zhengjin.jmeter.app.SingleJUnitTestRunner ${test_class}#${test_method}
fi

echo "Jmeter app build and run DONE."

set +ue
