package zhengjin.app.demo;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.google.common.util.concurrent.RateLimiter;

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
	public void testSample05() {
		// 连续数字分桶
		int rangeStart = 1;
		int rangeEnd = 100;
		int partition = 2;

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

	@Test
	public void testSample06() {
		// 2-8原则访问热点key
		int count = 60;
		int KeyCount = 0;
		List<String> list = new LinkedList<String>();

		for (int i = 0; i < count; i++) {
			String key = this.getHotKey(101, 200);
			if (key.startsWith("key")) {
				KeyCount++;
				list.add(key);
			}
		}

		System.out.printf("hot keys count: %d / %d\n", KeyCount, count);
		Collections.sort(list, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				int val1 = Integer.valueOf(o1.split("_")[1]);
				int val2 = Integer.valueOf(o2.split("_")[1]);
				return val1 - val2;
			}

		});
		System.out.println("sorted hot keys:\n" + list);
	}

	private String getHotKey(int start, int end) {
		int count = end - start + 1;
		Random rand = new Random();
		int percent = rand.nextInt(100);

		if (percent < 80) {
			float offset = 0.2F * rand.nextFloat(); // [0.0f, 1.0f)
			return String.format("hotkey_%d", (int) (start + count * offset));
		} else {
			float offset = 0.8F * rand.nextFloat();
			return String.format("key_%d", (int) (start + count * 0.2 + count * offset));
		}
	}

	@Test
	public void testSample07() {
		// get key number
		String prefix = "user_id";
		String key = prefix + 1001;
		System.out.println("key number: " + key.substring(prefix.length()));
	}

	@Test
	public void testSample08() throws InterruptedException {
		// when set RateLimiter, check wait time of each thread
		Thread[] pool = new Thread[3];
		RateLimiter limit = RateLimiter.create(0.5d);
		int[] ints = { 1, 6, 2 };

		for (int i = 0; i < ints.length; i++) {
			final int idx = i;
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					double wait = limit.acquire(ints[idx]);
					System.out.printf("[%s] wait for %.2f seconds\n", Thread.currentThread().getName(), wait);
				}
			});
			t.start();
			pool[i] = t;
			TimeUnit.MILLISECONDS.sleep(100l);
		}

		for (Thread t : pool) {
			t.join();
		}
		System.out.println("RateLimiter test done");
	}

}
