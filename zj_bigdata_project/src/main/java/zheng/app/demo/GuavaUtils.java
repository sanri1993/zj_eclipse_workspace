package zheng.app.demo;

import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Iterators;
import com.google.common.collect.Ordering;
import com.google.common.collect.Range;

public class GuavaUtils {

	public static Integer sumWithOptional(Optional<Integer> a, Optional<Integer> b) {
		// Optional.isPresent - checks the value is present or not
		System.out.println("1st parameter is present: " + a.isPresent());
		System.out.println("2nd parameter is present: " + b.isPresent());

		// Optional.or - returns the value if present otherwise returns the default
		// value passed.
		Integer val1 = a.or(new Integer(0));
		// Optional.get - gets the value, value should be present
		Integer val2 = b.get();

		return val1 + val2;
	}

	public static double sqrtWithPrecondition(double input) throws IllegalArgumentException {
		Preconditions.checkArgument(input > 0.0, "Illegal Argument passed: Negative value %s.", input);
		return Math.sqrt(input);
	}

	public static int sumWithPrecondition(Integer a, Integer b) {
		a = Preconditions.checkNotNull(a, "Illegal Argument passed: 1st parameter is Null.");
		b = Preconditions.checkNotNull(b, "Illegal Argument passed: 2nd parameter is Null.");
		return a + b;
	}

	public static int getValueWithPrecondition(int input) {
		int[] data = { 1, 2, 3, 4, 5 };
		Preconditions.checkElementIndex(input, data.length, "Illegal Argument passed: Invalid index.");
		return 0;
	}

	public static boolean collectionIsOrder(Ordering<Integer> ordering, List<Integer> numbers) {
		System.out.println("Input Numbers: " + numbers);
		return ordering.isOrdered(numbers);
	}

	public static Range<Integer> createOpenedRange(int start, int end) {
		return Range.open(start, end);
	}

	public static Range<Integer> createClosedRange(int start, int end) {
		return Range.closed(start, end);
	}

	public static void printRange(Range<Integer> range) {
		System.out.print("[ ");
		for (int grade : ContiguousSet.create(range, DiscreteDomain.integers())) {
			System.out.print(grade + " ");
		}
		System.out.print(" ]");
	}

	public static boolean AllStartWithChar(List<String> list, final String c) {
		Predicate<String> predicate = new Predicate<String>() {
			@Override
			public boolean apply(@Nullable String input) {
				return input.startsWith(c);
			}

			@Override
			public boolean test(@Nullable String input) {
				return input.startsWith(c);
			}
		};

		return Iterators.all(list.iterator(), predicate);
	}

}
