package scala.demo

import java.io.File

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.ListBuffer
import scala.io.Source
import scala.util.Random

/**
 * #1
 */
class Person(val name: String, val age: Int) {
  println("Hello Spark")

  val x = 1
  if (x > 1) {
    println("gt")
  } else if (x < 1) {
    println("lt")
  } else {
    println("eq")
  }

  private var addr = "BeiJing"

  def this(name: String, age: Int, address: String) {
    this(name, age)
    println("run this() constructor")
    this.addr = address
  }
}

/**
 * #2
 */
class Session {
  println("Session is created.")
}

object SessionFactory {
  println("SessionFactory init.")
  var counts = 5
  val sessions = new ArrayBuffer[Session]()
  while (counts > 0) {
    sessions += new Session
    counts -= 1
  }

  def getSession(): Session = {
    sessions.remove(0)
  }
}

/**
 * #3
 */
class People {
  private var name = "person:"
  def getName: String = name
}

class Student extends People {
  private var score = 79
  def getScore = score
  override def getName: String = super.getName + "tester"
}

/**
 * #4
 */
trait HelloTrait {
  def sayHello(msg: String)
}

trait MakeFriendsTrait {
  def makeFriends(w: Worker)
}

class Worker(var name: String) extends HelloTrait with MakeFriendsTrait {
  def sayHello(msg: String) = println("hello " + name + msg)
  def makeFriends(w: Worker) = println("hello, my name is " + this.name + ", you name is " + w.name)
}

/**
 * #5
 */
trait Logged {
  def log(msg: String)
}

trait AMyLogger extends Logged {
  override def log(msg: String): Unit = {
    println("test:" + msg)
  }
}

trait BMyLogger extends Logged {
  override def log(msg: String): Unit = {
    println("log:" + msg)
  }
}

class MyPerson(val name: String) extends AMyLogger {
  def sayHello(): Unit = {
    println("Hi ,i'm " + name)
    log("sayHello is invoked!")
  }
}

/**
 * 隐式转换
 */
class RichFile(val file: File) {
  def read(): String = Source.fromFile(file.getPath).mkString
}

object RichFile {
  implicit def file2RichFile(file: File) = new RichFile(file)
}

object demo01 {

  /**
   * test if else statements
   */
  def testDemo01(): Unit = {
    val x = 1
    val y = if (x > 0) 1 else -1
    println("y=" + y)

    val z = 0
    val result = {
      if (z < 0)
        1
      else if (z >= 1)
        -1
      else
        "error"
    }
    println("z=" + result)
  }

  /**
   * test for loop statements
   */
  def testDemo02(): Unit = {
    for (i <- 1 to 10)
      print(i + ",")
    println()

    val arr = Array("a", "b", "c")
    for (i <- arr)
      print(i + ",")
    println()

    for (i <- 1 to 3; j <- 1 to 3 if i != j)
      print((10 * i + j) + ",")
    println()

    val v = for (i <- 1 to 10) yield i * 10
    println(v)
  }

  /**
   * test for method and function
   */
  def testDemo03(): Unit = {
    // method
    def m1(f: (Int, Int) => Int): Int = {
      f(2, 6)
    }

    // function
    val f1 = (x: Int, y: Int) => x + y
    val f2 = (m: Int, n: Int) => m * n

    val res1 = m1(f1)
    println("result1=" + res1)
    val res2 = m1(f2)
    println("result2=" + res2)
  }

  /**
   * test for array
   */
  def testDemo04(): Unit = {
    // 一个长度为8的定长数组, 其所有元素均为0
    val arr1 = new Array[Int](8)
    println("arr1=" + arr1.toBuffer)

    // 一个长度为3的定长数组
    val arr2 = Array("hadoop", "storm", "spark")
    println("arr2=" + arr2.toBuffer)
    // 使用()来访问元素
    println("arr2(2)=" + arr2(2))

    // 变长数组（数组缓冲）
    var ab = ArrayBuffer[Int]()
    ab += 1
    ab += (2, 3, 4, 5)
    ab ++= Array(6, 7)
    ab ++= ArrayBuffer(8, 9)
    println("ArrayBuffer ab=" + ab)

    // 遍历数组
    for (i <- (0 until ab.length).reverse)
      print(ab(i) + ",")
    println()

    // 数组转换
    val res1 = for (e <- ab if e % 2 == 0) yield e * 10
    println("result1=" + res1.toBuffer)

    val res2 = ab.filter(_ % 2 == 0).map(_ * 10)
    println("result2=" + res2.toBuffer)
  }

  /**
   * test for collection List
   */
  def testDemo05(): Unit = {
    // 不可变集合
    val list1 = List(1, 2, 3)

    // 将0插入到集合list1的前面生成一个新的list
    val list2 = 0 :: list1
    val list3 = list1.::(0)
    val list4 = 0 +: list1
    val list5 = list1.+:(0)
    println("list2=" + list2)
    println("list3=" + list3)
    println("list4=" + list4)
    println("list5=" + list5)

    // 将3插入到集合list1的后面生成一个新的list
    val list6 = list1 :+ 3
    println("list6=" + list6)

    // 将2个list集合合并成一个新的list集合
    val list0 = List(7, 8, 9)
    println("list0=" + list0)
    val list7 = list1 ++ list0
    println("list7=" + list7)

    // 将list0插入到list1前面生成一个新的集合
    val list8 = list0 ++: list1
    println("list8=" + list8)

    // 可变集合
    val list10 = ListBuffer[Int](1, 2, 3)
    list10 += 4
    list10.append(6)
    println("list10=" + list10)
  }

  /**
   * test for keyword Lazy
   */
  def init(): String = {
    println("init")
    return "lazy"
  }

  def testDemo06(): Unit = {
    lazy val msg = init();
    println("test demo")
    println(msg)
  }

  /**
   * class test
   */
  def testDemo07(): Unit = {
    // #1
    val p = new Person("tester01", 33, "SH")
    println()

    // #2
    val session = SessionFactory.getSession()
    println(session)
    println()

    // #3
    val student = new Student
    println("name=" + student.getName)
    println("score=" + student.getScore)
    println()

    // #4
    val w1 = new Worker("Java");
    val w2 = new Worker("Scala")
    w1.sayHello(" dev")
    w1.makeFriends(w2)
    println()

    // #5
    val p1 = new MyPerson("java")
    p1.sayHello()
    val p2 = new MyPerson("scala") with BMyLogger
    p2.sayHello()
    println()
  }

  /**
   * case match
   */
  def func1: PartialFunction[String, Int] = {
    case "one" => 1
    case "two" => 2
    case _ => -1
  }

  def func2(num: String): Int = num match {
    case "one" => 1
    case "two" => 2
    case _ => -1
  }

  def testDemo08: Unit = {
    // #1, value match
    var arr1 = Array("Hadoop", "HBase", "Spark")
    val name = arr1(Random.nextInt(arr1.length))
    name match {
      case "Hadoop" => println("hadoop")
      case "HBase" => println("hbase")
      case _ => println("nothing")
    }
    println()

    // #2, type match
    val arr2 = Array("hello", 1, 2.0)
    val v = arr2(Random.nextInt(arr2.length))
    v match {
      case x: Int => println("Int " + x)
      case y: Double if (y >= 0) => println("Double " + y)
      case z: String => println("String " + z)
      case _ => throw new Exception("not match exception")
    }
    println()

    // #3
    println(func1("one"))
    println(func2("two"))
  }

  /**
   * 柯里化
   */
  def mysum(x: Int)(y: Int): Int = x + y

  def testDemo09: Unit = {
    val f = mysum _
    val f1 = f(1)
    println(f1(2))
  }

  /**
   * 隐式转换
   */
  def testDemo10: Unit = {
    var file = new File("/Users/zhengjin/Downloads/tmp_files/test.file")
    import RichFile._
    println(file.read())
  }

  /**
   * main
   */
  def main(args: Array[String]): Unit = {

    testDemo10
    println("scala demo01 done.")
  }

}