package com.zjmvn.demo;

import com.google.common.base.Optional;
import com.google.common.collect.Ordering;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestGuavaUtils {

	private final static String TAG = TestDemo.class.getSimpleName() + " => ";
	private final static Logger logger = Logger.getLogger(TestDemo.class);

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

}
