package zhengjin.app.demo;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.collect.Range;
import com.google.common.collect.UnmodifiableIterator;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.google.common.primitives.Ints;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestGuavaUtils {

	private final static String TAG = TestGuavaUtils.class.getSimpleName() + " => ";
	private final static Logger logger = Logger.getLogger(TestGuavaUtils.class);

	@Test
	public void test01GuavaSumWithOptional() {
		logger.info(TAG + "Test guava utils Optional.");

		Integer value1 = null;
		Integer value2 = new Integer(10);

		// Optional.fromNullable - allows passed parameter to be null.
		Optional<Integer> a = Optional.fromNullable(value1);
		// Optional.of - throws NullPointerException if passed parameter is null
		Optional<Integer> b = Optional.of(value2);

		int actual = GuavaUtils.sumWithOptional(a, b);
		Assert.assertEquals(10, actual);
	}

	@Test(expected = NullPointerException.class)
	public void test02GuavaSumWithPrecondition() {
		logger.info(TAG + "Test guava utils Precondition.");
		System.out.println(GuavaUtils.sumWithPrecondition(null, 3));
	}

	@Test(expected = IllegalArgumentException.class)
	public void test03GuavasqrtWithPrecondition() {
		logger.info(TAG + "Test guava utils Precondition.");
		System.out.println(GuavaUtils.sqrtWithPrecondition(-3.0));
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void test04GuavaGetValueWithPrecondition() {
		logger.info(TAG + "Test guava utils Precondition.");
		System.out.println(GuavaUtils.getValueWithPrecondition(6));
	}

	@Test
	public void test05GuavaCollectionIsOrder() {
		logger.info(TAG + "Test guava utils Ordering.");
		List<Integer> numbers = new ArrayList<Integer>(20);

		numbers.add(new Integer(5));
		numbers.add(new Integer(2));
		numbers.add(new Integer(15));
		numbers.add(new Integer(51));
		numbers.add(new Integer(53));
		numbers.add(new Integer(35));
		numbers.add(new Integer(45));
		numbers.add(new Integer(32));
		numbers.add(new Integer(43));
		numbers.add(new Integer(16));

		Ordering<Integer> ordering = Ordering.natural();

		Assert.assertFalse(GuavaUtils.collectionIsOrder(ordering, numbers));
		Collections.sort(numbers, ordering);
		Assert.assertTrue(GuavaUtils.collectionIsOrder(ordering, numbers));

		Assert.assertEquals(new Integer(2), ordering.min(numbers));
		Assert.assertEquals(new Integer(53), ordering.max(numbers));
	}

	@Test
	public void test06GuavaRange() {
		logger.info(TAG + "Test guava utils Range.");
		Range<Integer> range1 = GuavaUtils.createClosedRange(0, 9);
		System.out.print("[0,9] : ");
		GuavaUtils.printRange(range1);

		Assert.assertTrue(range1.contains(5));
		Assert.assertTrue(range1.containsAll(Ints.asList(1, 2, 3)));
		Assert.assertEquals(new Integer(0), range1.lowerEndpoint());
		Assert.assertEquals(new Integer(9), range1.upperEndpoint());

		Range<Integer> range2 = GuavaUtils.createOpenedRange(0, 9);
		System.out.print("\n(0,9) : ");
		GuavaUtils.printRange(range2);
	}

	@Test
	public void test07GuavaIterators() {
		logger.info(TAG + "Test guava utils Iterators.");
		List<String> list = Lists.newArrayList("Apple", "Pear", "Peach", "Banana");
		Assert.assertFalse(GuavaUtils.AllStartWithChar(list, "P"));

		// filter
		UnmodifiableIterator<String> iter1 = Iterators.filter(list.iterator(), new Predicate<String>() {
			@Override
			public boolean apply(@Nullable String input) {
				return input.startsWith("P");
			}

			@Override
			public boolean test(@Nullable String input) {
				return input.startsWith("P");
			}
		});

		System.out.println("\nItems start with 'P':");
		while (iter1.hasNext()) {
			System.out.println(iter1.next());
		}

		// transform
		Iterator<Integer> iter2 = Iterators.transform(list.iterator(), new Function<String, Integer>() {
			@Override
			public @Nullable Integer apply(@Nullable String input) {
				return input.length();
			}
		});
		System.out.println("\nMapped items:");
		while (iter2.hasNext()) {
			System.out.println(iter2.next());
		}
	}

	@Test
	public void test11GuavaStringUtils() {
		logger.info(TAG + "Test guava string utils.");
		Assert.assertTrue(Strings.isNullOrEmpty(""));
		Assert.assertTrue(Strings.isNullOrEmpty(null));

		String base = "com.jd.coo.";
		Assert.assertEquals(base, Strings.commonPrefix(base + "hello", base + "Hi"));

		Assert.assertEquals("0123", Strings.padStart("123", 4, '0'));
		Assert.assertEquals("12300", Strings.padEnd("123", 5, '0'));

		// split
		String testStr = " a=b;c=d,e=f ";
		Map<String, String> entries = Splitter.onPattern("[,;]{1,}").trimResults().omitEmptyStrings()
				.withKeyValueSeparator("=").split(testStr);
		for (Map.Entry<String, String> entry : entries.entrySet()) {
			System.out.println(String.format("%s:%s", entry.getKey(), entry.getValue()));
		}

		// join
		String content = Joiner.on("|").join(new String[] { "hello", "world" });
		System.out.println("Join results: " + content);
	}

	@Test
	public void test12GuavaBloomFilter() {
		final int count = 1000000;

		// 默认误判率 fpp 0.03
		BloomFilter<CharSequence> bf = BloomFilter.create(Funnels.stringFunnel(Charsets.UTF_8), count, 0.002);
		for (Integer i = 0; i < count; i++) {
			bf.put(i.toString());
		}

		int foundCount = 0;
		for (Integer i = 0; i < count + 10000; i++) {
			if (bf.mightContain(i.toString())) {
				foundCount++;
			}
		}
		System.out.println("match count: " + foundCount);
	}

}
