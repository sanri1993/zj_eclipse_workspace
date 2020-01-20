# zj_maven_project

maven lifecycle commands:

```sh
$ mvn validate
$ mvn compile
$ mvn test-compile
$ mvn test
$ mvn package
$ mvn clean
```

run project:

```sh
$ mvn package -Dmaven.test.skip=true
$ java -jar zj-mvn-demo.jar

# for hadoop-docker java1.7
$ mvn clean package
```

check files in archived jar:

```sh
tarl zj-mvn-demo.jar | grep zjmvn
```

check dependency:

```sh
$ mvn dependency:list | less
$ mvn dependency:tree | less
```

