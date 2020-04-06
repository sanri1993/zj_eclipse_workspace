package zhengjin.app.demo;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestDemo {

	private final static Logger logger = Logger.getLogger(TestDemo.class);

	@Test
	public void testSample01() {
		// 默认调用父对象的无参构造函数
		Student s1 = new Student("A");
		Assert.assertNotNull(s1);
		logger.info(s1.toString());

		Student s2 = new Student("test1", "B");
		Assert.assertNotNull(s2);
		logger.info(s2.toString());
	}

	private static class Person {

		String name;

		public Person() {
			logger.info("Constructor Person()");
			this.name = "default";
		}

		public Person(String name) {
			logger.info("Constructor Person(name)");
			this.name = name;
		}

		@Override
		public String toString() {
			return String.format("[Person] name:" + this.name);
		}
	}

	private static class Student extends Person {

		String level;

		public Student(String level) {
			logger.info("Constructor Student(level)");
			this.level = level;
		}

		public Student(String name, String level) {
			super(name);
			logger.info("Constructor Student(name, level)");
//			this.name = name;
			this.level = level;
		}

		@Override
		public String toString() {
			return String.format("[Student] name:%s, level:%s", this.name, this.level);
		}
	}

	@Test
	public void testSample02() {
		// 1.默认调用父对象的无参构造函数
		// 2.构造函数不支持函数重载
		Apple a1 = new Apple();
		Assert.assertNotNull(a1);
		System.out.println(a1 + "\n");

		Apple a2 = new Apple("green");
		Assert.assertNotNull(a2);
		System.out.println(a2 + "\n");

		Apple a3 = new Apple("green", "usa");
		Assert.assertNotNull(a3);
		System.out.println(a3);
	}

	private static class Fruit {

		String name;

		public Fruit() {
			System.out.println("Constructor Fruit()");
			this.name = "fruit";
		}

		public Fruit(String name) {
			System.out.println("Constructor Fruit(name)");
			this.name = name;
		}

		@Override
		public String toString() {
			return "this is a " + this.name;
		}
	}

	private static class Apple extends Fruit {

		String color = "red";
		String from = "china";

		public Apple() {
		}

		public Apple(String color) {
			this.name = "apple";
			this.color = color;
		}

		public Apple(String color, String from) {
			super("apple");
			this.color = color;
			this.from = from;
		}

		@Override
		public String toString() {
			return String.format("this is a %s: %s and from %s", this.name, this.color, this.from);
		}
	}

	@Test
	public void testSample03() throws InterruptedException {
		List<String> list = new LinkedList<String>();
		List<Thread> pool = new LinkedList<Thread>();

		int count = 3;
		for (int i = 0; i < count; i++) {
			pool.add(new Thread(new ListAdd(list)));
		}

		for (Thread t : pool) {
			t.start();
		}
		for (Thread t : pool) {
			t.join();
		}
		System.out.println("list size: " + list.size());
	}

	@Test
	public void testSample04() throws InterruptedException {
		List<String> list = new LinkedList<String>();
		Collection<String> syncList = Collections.synchronizedCollection(list);
		List<Thread> pool = new LinkedList<Thread>();

		int count = 3;
		for (int i = 0; i < count; i++) {
			pool.add(new Thread(new ListAdd(syncList)));
		}

		for (Thread t : pool) {
			t.start();
		}
		for (Thread t : pool) {
			t.join();
		}
		System.out.println("list size: " + syncList.size());
	}

	private static class ListAdd implements Runnable {

		private Collection<String> list;

		public ListAdd(Collection<String> list) {
			this.list = list;
		}

		@Override
		public void run() {
			String tag = Thread.currentThread().getName();
			for (int i = 0; i < 10000; i++) {
				this.list.add(tag + i);
			}
		}
	}

	@Test
	public void testSample05() throws InterruptedException {
		// 连续数字分桶
		int rangeStart = 1;
		int rangeEnd = 100;
		int partition = 3;

		int range = (rangeEnd - rangeStart + 1) / partition;
		int remained = (rangeEnd - rangeStart + 1) % partition;

		for (int i = 0; i < partition; i++) {
			int start = rangeStart + range * i;
			int end = start + range;
			if (i == (partition - 1)) {
				end += remained;
			}
			System.out.printf("[%d,%d)\n", start, end);
		}
	}

}
