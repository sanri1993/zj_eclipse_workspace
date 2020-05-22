package zhengjin.app.demo;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
		// 测试非线程安全的List.add()方法
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
		// 测试线程安全的List.add()方法
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
		// set RateLimiter, check wait time of each thread
		Thread[] pool = new Thread[3];
		RateLimiter limit = RateLimiter.create(0.5d);
		int[] ints = { 1, 6, 2 };

		for (int i = 0; i < ints.length; i++) {
			final int idx = i;
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					double wait = limit.acquire(ints[idx]);
					System.out.printf("[%s] wait for %.2f seconds.\n", Thread.currentThread().getName(), wait);
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

	@Test
	public void testSample09() throws InterruptedException {
		// monkey random events
		float[] pcts = new float[] { 0.2f, 0.3f, 0.5f };
		float sum = 0.0f;
		for (int i = 0; i < pcts.length; i++) {
			pcts[i] += sum;
			sum = pcts[i];
		}
		System.out.println("pcts: " + Arrays.toString(pcts));

		float tmp = 0;
		Random rand = new Random();
		MyEvents events = new MyEvents();
		for (int i = 0; i < 100; i++) {
			tmp = rand.nextFloat();
			if (tmp < pcts[0]) {
				events.event1();
			} else if (tmp < pcts[1]) {
				events.event2();
			} else if (tmp < pcts[2]) {
				events.event3();
			}
		}
		String result = String.format("event1_cnt=%d, event2_cnt=%d, event3_cnt=%d\n", events.count1, events.count2,
				events.count3);
		System.out.println(result);
	}

	private static class MyEvents {

		public int count1 = 0;
		public int count2 = 0;
		public int count3 = 0;

		private Random rand = new Random(66);
		private int sleepTime = 100;

		public void event1() throws InterruptedException {
			TimeUnit.MILLISECONDS.sleep(this.rand.nextInt(this.sleepTime));
			this.count1++;
		}

		public void event2() throws InterruptedException {
			TimeUnit.MILLISECONDS.sleep(this.rand.nextInt(this.sleepTime));
			this.count2++;
		}

		public void event3() throws InterruptedException {
			TimeUnit.MILLISECONDS.sleep(this.rand.nextInt(this.sleepTime));
			this.count3++;
		}
	}

	@Test
	public void testSample10() throws IllegalArgumentException, IllegalAccessException {
		TestObject obj1 = new TestObject(1, "test01");
		System.out.println(obj1);
		System.out.println();

		// 使用java反射将map转化为object
		Map<String, Object> map = new HashMap<>();
		map.put("id", new Integer(2));
		map.put("name", "test02");

		TestObject obj2 = new TestObject();
		TestObject newObj2 = mapToObject(map, obj2);
		System.out.println(obj2);
		System.out.println(newObj2);
	}

	private static <T> T mapToObject(Map<String, Object> map, T object)
			throws IllegalArgumentException, IllegalAccessException {
		@SuppressWarnings("unchecked")
		Class<T> cls = (Class<T>) object.getClass();
		for (Field field : cls.getFields()) {
			int mod = field.getModifiers();
			if (Modifier.isStatic(mod) || Modifier.isFinal(mod)) {
				continue;
			}
			field.setAccessible(true);
			field.set(object, map.get(field.getName().toLowerCase()));
		}

		return object;
	}

	private static class TestObject {

		public int id;
		public String name;

		public TestObject() {
		}

		public TestObject(int id, String name) {
			this.id = id;
			this.name = name;
		}

		@Override
		public String toString() {
			return String.format("TestObject{id=%d, name=%s}", this.id, this.name);
		}
	}

	@Test
	public void testSampler11() throws InterruptedException {
		// test keyword synchronized
		boolean sync = true;
		int iteratorCnt = 100_000;
		Runnable r = null;
		MyCalulation cal = new MyCalulation();
		if (sync) {
			r = new Runnable() {
				@Override
				public void run() {
					for (int j = 0; j < iteratorCnt; j++) {
						cal.syncAdd();
					}
				}
			};
		} else {
			r = new Runnable() {
				@Override
				public void run() {
					for (int j = 0; j < iteratorCnt; j++) {
						cal.add();
					}
				}
			};
		}

		final int threadsCnt = 20;
		Thread[] pool = new Thread[threadsCnt];
		for (int i = 0; i < threadsCnt; i++) {
			Thread t = new Thread(r);
			pool[i] = t;
		}

		long start = System.currentTimeMillis();
		for (Thread t : pool) {
			t.start();
		}
		System.out.println("all threads are running...");

		for (Thread t : pool) {
			t.join();
		}
		System.out.printf("exec time: %dms\n", System.currentTimeMillis() - start);
		System.out.println("calulation add results: " + cal.getBase());
	}

	private static class MyCalulation {

		int base = 0;

		public void add() {
			this.base++;
		}

		public synchronized void syncAdd() {
			this.base++;
		}

		public int getBase() {
			return this.base;
		}
	}

	@Test
	public void testSampler12() {
		Map<String, Integer> map = new HashMap<>();
		for (int i = 0; i < 100; i++) {
			map.put(String.valueOf(i), new Integer(i));
			map.remove(String.valueOf(i - 1));
		}
		System.out.println("map size: " + map.size());
	}

}
