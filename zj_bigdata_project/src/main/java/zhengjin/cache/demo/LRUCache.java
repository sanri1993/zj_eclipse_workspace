package zhengjin.cache.demo;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

/**
 * 淘汰算法 LRU: Least Recently Used. 当存储的数据已满时，把最久没有被访问到的数据淘汰。
 *
 */
public class LRUCache implements Cache {

	private int capacity;
	private LinkedHashMap<String, String> map;

	public LRUCache(int capacity) {
		this.capacity = capacity;
		this.map = new LinkedHashMap<>(capacity * 2);
	}

	@Override
	public String get(String key) {
		String ret = this.map.get(key);
		if (ret == null) {
			System.out.println("Value is not found for key: " + key);
			return "";
		}
		this.moveToEnd(key);
		return ret;
	}

	@Override
	public void put(String key, String value) {
		this.map.put(key, value);
		if (this.map.size() > this.capacity) {
			this.removeHead();
		}
	}

	public int getSize() {
		return this.map.size();
	}

	public void printCacheItems() {
		System.out.println("Cache items:");
		for (Entry<String, String> entry : this.map.entrySet()) {
			System.out.printf("[%s=%s],", entry.getKey(), entry.getValue());
		}
		System.out.println();
	}

	private void removeHead() {
		Entry<String, String> head = this.map.entrySet().iterator().next();
		this.map.remove(head.getKey());
	}

	private void moveToEnd(String key) {
		String value = this.map.get(key);
		this.map.remove(key);
		this.map.put(key, value);
	}

}
