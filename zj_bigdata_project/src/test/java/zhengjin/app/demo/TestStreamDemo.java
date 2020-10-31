package zhengjin.app.demo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.compress.utils.Lists;
import org.junit.Test;

public class TestStreamDemo {

	@Test
	public void testInitStreamByRandom() {
		Random rand = new Random();
		Stream<Integer> stream = Stream.generate(() -> rand.nextInt(100)).limit(10);
		stream.forEach(System.out::println);
	}

	@Test
	public void testInitStreamByIterator() {
		Stream<Integer> stream = Stream.iterate(1, n -> n + 1).limit(10);
		stream.forEach(System.out::println);
	}

	@Test
	public void testForEach() {
		List<String> list = Arrays.asList("you don't bird me".split(" "));

		System.out.println("#1");
		list.stream().forEach(item -> System.out.println(item));
		System.out.println("\n#2");
		list.forEach(System.out::println);
	}

	@Test
	public void testFilter() {
		List<User> users = new ArrayList<>(10);
		users.add(new User(1L, "mengday", 28));
		users.add(new User(2L, "guoguo", 18));
		users.add(new User(3L, "liangliang", 17));

		users.stream().filter(user -> user.age > 17).forEach(System.out::println);
	}

	@Test
	public void testMap() {
		List<String> list = Arrays.asList("how are you?".split(" "));
		list.stream().map(item -> item.toUpperCase()).forEach(System.out::println);
	}

	@Test
	public void testFlatMap01() {
		// flatmap: 将一个2维的集合映射成一个1维集合
		List<Integer> a = Arrays.asList(1, 2, 3);
		List<Integer> b = Arrays.asList(4, 5, 6);

		List<List<Integer>> collect = Stream.of(a, b).collect(Collectors.toList());
		System.out.println(collect);

		List<Integer> mergeList = Stream.of(a, b).flatMap(list -> list.stream()).collect(Collectors.toList());
		System.out.println(mergeList);
	}

	@Test
	public void testFlatMap02() {
		String[] words = new String[] { "Hello", "World" };
		Arrays.stream(words).map(word -> word.split("")).flatMap(Arrays::stream).distinct()
				.forEach(System.out::println);
	}

	@Test
	public void testSort() {
		List<String> list = Arrays.asList("c", "e", "a", "d", "b");
		list.stream().sorted((s1, s2) -> s1.compareTo(s2)).forEach(System.out::println);
	}

	@Test
	public void testCount() {
		Stream<String> stream = Stream.of("know", "is", "know", "noknow", "is", "noknow");
		System.out.print(stream.count());
	}

	@Test
	public void testMin() {
		List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);
		Optional<Integer> optional = list.stream().min((a, b) -> a.compareTo(b));
		System.out.println(optional.get());

		// max
		list.stream().max(Integer::compareTo).ifPresent(System.out::println);
	}

	@Test
	public void testConcat() {
		List<String> list1 = Arrays.asList("a", "b");
		List<String> list2 = Arrays.asList("c", "d");
		Stream.concat(list1.stream(), list2.stream()).forEach(System.out::println);
	}

	@Test
	public void testAnyMatch() {
		List<String> list = Arrays.asList("you give me stop".split(" "));
		boolean result = list.parallelStream().anyMatch(item -> "me".equals(item));
		System.out.println(result);
	}

	@Test
	public void testReduce() {
		Stream<String> stream = Stream.of("you", "give", "me", "stop");
		Optional<String> optional = stream.reduce((before, after) -> before + "," + after);
		optional.ifPresent(System.out::println);
	}

	@Test
	public void testMergeMapValues() {
		User user = new User(1L, "foo1", 31);

		List<User> users1 = Lists.newArrayList();
		users1.add(user);
		users1.add(new User(2L, "foo2", 32));
		users1.add(new User(3L, "foo3", 33));

		List<User> users2 = Lists.newArrayList();
		users2.add(user);
		users2.add(new User(4L, "foo4", 34));
		users2.add(new User(5L, "foo5", 35));

		System.out.println("intersect result (交集)");
		users1.stream().filter(users2::contains).forEach(item -> System.out.println(item.id));
		System.out.println("difference result (差集)");
		users1.stream().filter(item -> !users2.contains(item)).forEach(item -> System.out.println(item.id));
		System.out.println("union result (并集)");
		Stream.of(users1, users2).flatMap(Collection::stream).distinct().forEach(item -> System.out.println(item.id));
	}

	private static class User {

		private Long id;
		private String username;
		private Integer age;

		public User(Long id, String username, Integer age) {
			this.id = id;
			this.username = username;
			this.age = age;
		}

		@Override
		public String toString() {
			return String.format("user info: id=%d, name=%s, age=%d", this.id, this.username, this.age);
		}
	}

}
