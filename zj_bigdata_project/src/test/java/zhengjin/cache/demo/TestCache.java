package zhengjin.cache.demo;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;

import org.junit.Test;

public class TestCache {

	@Test
	public void testSortHashMapKeys() {
		// print HashMap value by sorted keys
		HashMap<Integer, String> map = new HashMap<>();
		map.put(1, "a");
		map.put(3, "c");
		map.put(4, "d");
		map.put(2, "b");

		System.out.println("before sort:");
		Set<Integer> keys = map.keySet();
		for (Integer key : keys) {
			System.out.println(key + "=" + map.get(key));
		}

		System.out.println("\nafter sort:");
		Integer[] keysArray = new Integer[map.size()];
		keys.toArray(keysArray);
		Arrays.sort(keysArray, new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				return o2 - o1;
			}
		});

		for (Integer key : keysArray) {
			System.out.println(key + "=" + map.get(key));
		}
	}

	@Test
	public void testCacheMinFreq() {
		LFUCache.MinFreq minFreq = new LFUCache.MinFreq();
		// 1 1 1
		minFreq.setFreq(1);
		minFreq.setFreq(1);
		minFreq.setFreq(1);
		System.out.println(minFreq.getFreq());

		// 1 2 2
		minFreq.setFreq(2);
		minFreq.setFreq(2);
		System.out.println(minFreq.getFreq());

		// 1 2 3
		minFreq.setFreq(3);
		System.out.println(minFreq.getFreq());

		// 2 2 3
		minFreq.setFreq(2);
		System.out.println(minFreq.getFreq());
	}

	@Test
	public void testLRUCache() {
		LRUCache lru = new LRUCache(3);
		// a b c
		lru.put("a", "1");
		lru.put("b", "1");
		lru.put("c", "1");
		lru.printCacheItems();

		// b c a
		lru.get("a");
		lru.printCacheItems();

		// a b c
		for (String key : new String[] { "b", "c" }) {
			lru.get(key);
		}
		lru.printCacheItems();

		// b c d
		lru.put("d", "1");
		lru.printCacheItems();
	}

	@Test
	public void testLFUCache() {
		LFUCache lfu = new LFUCache(3);
		// a b c
		lfu.put("a", "1");
		lfu.put("b", "1");
		lfu.put("c", "1");
		lfu.printCacheItems();

		// b c a
		lfu.get("a");
		lfu.printCacheItems();

		// b c a
		lfu.get("a");
		lfu.get("a");
		lfu.printCacheItems();

		// c b a
		lfu.get("b");
		lfu.printCacheItems();

		// b c a
		lfu.get("c");
		lfu.printCacheItems();

		// d c a
		lfu.put("d", "1");
		lfu.printCacheItems();
	}

}
