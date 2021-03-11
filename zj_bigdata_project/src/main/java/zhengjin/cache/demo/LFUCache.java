package zhengjin.cache.demo;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 淘汰算法 LFU: Least Frequently Used. 存储数据已满时，先检查访问次数，如果次数一致，则把最久没有被访问到的数据淘汰。
 * 
 */
public class LFUCache implements Cache {

	private int capacity;
	private MinFreq minFreq;
	private HashMap<String, Node> nodes;
	private HashMap<Integer, LinkedHashSet<Node>> sequence;

	public LFUCache(int capacity) {
		this.capacity = capacity;
		this.nodes = new HashMap<>(capacity * 2);
		this.sequence = new HashMap<>(capacity);
		this.minFreq = new MinFreq();
	}

	@Override
	public String get(String key) {
		if (!this.nodes.containsKey(key)) {
			System.out.println("Value is not found for key: " + key);
			return "";
		}
		this.addNodeFreq(key);
		return this.nodes.get(key).value;
	}

	@Override
	public void put(String key, String value) {
		if (this.nodes.containsKey(key)) {
			// update node value, and keep freq unchanged.
			this.nodes.get(key).value = value;
		} else {
			this.addNewNode(key, value);
		}
	}

	public int getSize() {
		return this.nodes.size();
	}

	public void printCacheItems() {
		Set<Integer> keys = this.sequence.keySet();
		Integer[] keysArray = new Integer[this.sequence.size()];
		keys.toArray(keysArray);
		Arrays.sort(keysArray, new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				return o1 - o2;
			}
		});

		System.out.println("Cache items by freq:");
		for (Integer key : keysArray) {
			for (Node node : this.sequence.get(key)) {
				System.out.printf("%s,", node);
			}
		}
		System.out.println();
	}

	/**
	 * Add new node with freq as 1. If size is greater than capacity, then eliminate
	 * a node.
	 * 
	 * @param key
	 * @param value
	 */
	private void addNewNode(String key, String value) {
		Node node = new Node(key, value);
		this.nodes.put(key, node);

		LinkedHashSet<Node> set = this.sequence.get(1);
		if (set == null) {
			set = new LinkedHashSet<>();
			this.sequence.put(1, set);
		}
		set.add(node);

		if ((this.nodes.size()) > this.capacity) {
			this.eliminateNode();
		}
		this.minFreq.setFreq(1);
	}

	private void addNodeFreq(String key) {
		Node node = this.nodes.get(key);
		LinkedHashSet<Node> set = this.sequence.get(node.freq);
		set.remove(node);

		set = this.sequence.get(++node.freq);
		if (set == null) {
			set = new LinkedHashSet<>();
			this.sequence.put(node.freq, set);
		}
		set.add(node);
		this.minFreq.setFreq(node.freq);
	}

	/**
	 * 淘汰一个节点。
	 */
	private void eliminateNode() {
		LinkedHashSet<Node> set = this.sequence.get(this.minFreq.getFreq());
		Node head = set.iterator().next();
		set.remove(head);
	}

	public static class Node {

		String key;
		String value;
		int freq = 1;

		public Node(String key, String value) {
			this.key = key;
			this.value = value;
		}

		@Override
		public String toString() {
			return String.format("[key=%s,value=%s,freq=%d]", this.key, this.value, this.freq);
		}
	}

	static class MinFreq {

		private int freq = 1;
		private int numberOfFreq;

		public void setFreq(int freq) {
			if (this.freq == freq) {
				this.numberOfFreq++;
			} else if (this.freq > freq) {
				this.freq = freq;
				this.numberOfFreq = 1;
			} else if ((freq - this.freq) == 1) {
				this.numberOfFreq--;
				if (this.numberOfFreq == 0) {
					this.freq = freq;
				}
			}
		}

		public int getFreq() {
			return this.freq;
		}
	}

}
