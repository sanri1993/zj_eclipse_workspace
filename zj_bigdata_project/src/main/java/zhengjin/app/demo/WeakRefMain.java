package zhengjin.app.demo;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 
 * LeakCanary 原理例子
 *
 */
public final class WeakRefMain {

	private static class MyKeyedWeakReference<T> extends WeakReference<T> {

		public String key;
		private String className;

		public MyKeyedWeakReference(String key, T referent, ReferenceQueue<? super T> gcQueue) {
			super(referent, gcQueue);
			this.key = key;
			this.className = referent.getClass().getSimpleName();
		}

		@Override
		public String toString() {
			return String.format("key=%s, className=%s", this.key, this.className);
		}
	}

	// 需要观察的对象
	Map<String, MyKeyedWeakReference<?>> watchedReferences = new HashMap<>();
	// 如果最后retainedReferences还存在引用, 说明泄漏了
	Map<String, MyKeyedWeakReference<?>> retainedReferences = new HashMap<>();
	// 当与之关联的弱引用中的实例被回收, 则会加入到 gcQueue
	ReferenceQueue<? super Object> gcQueue = new ReferenceQueue<>();

	private void sleepMills(long mills) {
		try {
			TimeUnit.MILLISECONDS.sleep(mills);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void runGc() {
		System.out.println("run system gc");
		Runtime.getRuntime().gc();
		sleepMills(100);
		System.runFinalization();
	}

	private String watch(Object obj) {
		System.out.println("#2. add watch");
		this.removeWeaklyReachableReferences();

		String key = UUID.randomUUID().toString();
		System.out.println("#3.key=" + key);
		MyKeyedWeakReference<?> reference = new MyKeyedWeakReference<Object>(key, obj, this.gcQueue);
		System.out.println("#4.reference=" + reference.toString());

		// 加入 watch 列表
		this.watchedReferences.put(key, reference);
		return key;
	}

	// 移除队列中将要被 GC 的引用
	private void removeWeaklyReachableReferences() {
		System.out.println("remove weakly reachable references");
		MyKeyedWeakReference<?> ref;
		// 队列 gcQueue 中的对象都是被 GC 的
		while ((ref = (MyKeyedWeakReference<?>) this.gcQueue.poll()) != null) {
			System.out.println(String.format("[%s] object remove from watch refs", ref.toString()));
			Object removedRef = this.watchedReferences.remove(ref.key);
			if (removedRef == null) {
				System.out.println("removedRef is null");
				this.retainedReferences.remove(ref.key);
			}
		}
	}

	private synchronized void cbMoveToRetained(String key) {
		// 对象被GC后, 回调 cbMoveToRetained
		System.out.println("#5.cbMoveToRetained, key=" + key);
		this.removeWeaklyReachableReferences();
		MyKeyedWeakReference<?> retainedRef = this.watchedReferences.remove(key);
		// 如果还有值说明没有被释放
		if (retainedRef != null) {
			this.retainedReferences.put(key, retainedRef);
		}
	}

	/**
	 * Scenario: 1. both object and related weak reference are GC; 2. object is GC
	 * and related weak reference exists; 3. object is not GC
	 */
	public static void main(String[] args) {

		boolean retained = true;
		WeakRefMain app = new WeakRefMain();

		Object obj = new Object();
		System.out.println("#1.create an object");
		String key = app.watch(obj);

		Object objRetained = null;
		if (retained) {
			objRetained = obj;
			System.out.println("create a retained object: " + objRetained);
		}

		// callback for object release event
		new Thread(new Runnable() {
			@Override
			public void run() {
				app.sleepMills(5000);
				app.cbMoveToRetained(key);
			}
		}).start();

		app.sleepMills(2000);
		obj = null;
		if (obj == null) {
			System.out.println("object is released");
		}
		app.runGc();

		app.sleepMills(5000);
		System.out.println("watchedReferences: " + app.watchedReferences);
		System.out.println("retainedReferences: " + app.retainedReferences);
		System.out.println("app done.");
	}

}
