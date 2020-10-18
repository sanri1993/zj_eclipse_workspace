package zhengjin.future.app;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Function;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

public final class ListenableFutureApp {

	public static void main(String[] args) throws InterruptedException, ExecutionException {

		// Generic
		ListenableFutureApp app = new ListenableFutureApp();
		app.TestGeneric();

		// ListenableFuture
		int test = 3;
		if (test == 1) {
			app.TestFuture01();
		} else if (test == 2) {
			app.TestFuture02();
		} else if (test == 3) {
			app.TestFuture03();
		}

		String tag = Thread.currentThread().getName();
		for (int i = 0; i < 8; i++) {
			TimeUnit.SECONDS.sleep(1);
			System.out.println(tag + ": main is running ...");
		}
		System.out.println(Thread.currentThread().getName() + ": Future App Done");
	}

	/**
	 * Futures.addCallback()
	 */
	private void TestFuture01() {
		ListeningExecutorService service = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(2));

		final ListenableFuture<String> listenableFuture = service.submit(() -> {
			String tag = Thread.currentThread().getName();
			try {
				for (int i = 0; i < 3; i++) {
					TimeUnit.SECONDS.sleep(1);
					System.out.println(tag + ": sub listening task is running ...");
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return tag + " done";
		});

		Futures.addCallback(listenableFuture, new FutureCallback<String>() {
			@Override
			public void onSuccess(String result) {
				System.out.println(Thread.currentThread().getName() + ": success: " + result);
			}

			@Override
			public void onFailure(Throwable t) {
				System.out.println("fail: " + t.getMessage());
			}
		}, service);
	}

	/**
	 * Futures.transform()
	 */
	private void TestFuture02() {
		ListeningExecutorService service = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(3));

		final ListenableFuture<String> task1 = service.submit(new Callable<String>() {
			@Override
			public String call() throws Exception {
				String tag = Thread.currentThread().getName();
				try {
					for (int i = 0; i < 2; i++) {
						TimeUnit.SECONDS.sleep(1);
						System.out.println(tag + ": sub listening task1 is running ...");
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				return tag + " done";
			}
		});

		ListenableFuture<String> task2 = Futures.transform(task1, new Function<String, String>() {
			@Override
			public String apply(String input) {
				String tag = Thread.currentThread().getName();
				try {
					for (int i = 0; i < 2; i++) {
						TimeUnit.SECONDS.sleep(1);
						System.out.println(tag + ": sub listening task2 is running ...");
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				return input + "\t" + tag + " done";
			}
		}, service);

		ListenableFuture<String> task3 = Futures.transform(task2, new Function<String, String>() {
			@Override
			public String apply(String input) {
				String tag = Thread.currentThread().getName();
				try {
					for (int i = 0; i < 2; i++) {
						TimeUnit.SECONDS.sleep(1);
						System.out.println(tag + ": sub listening task3 is running ...");
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				return input + "\t" + tag + " done";
			}
		}, service);

		Futures.addCallback(task3, new FutureCallback<String>() {
			@Override
			public void onSuccess(String result) {
				System.out.println(Thread.currentThread().getName() + ": success: " + result);
			}

			@Override
			public void onFailure(Throwable t) {
				System.out.println("fail: " + t.getMessage());
			}
		}, service);
	}

	/**
	 * Futures.allAsList()
	 * 
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	private void TestFuture03() throws InterruptedException, ExecutionException {
		ListeningExecutorService service = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(3));

		ListenableFuture<Integer> future1 = service.submit(new Callable<Integer>() {
			@Override
			public Integer call() throws InterruptedException {
				String tag = Thread.currentThread().getName();
				for (int i = 0; i < 3; i++) {
					TimeUnit.SECONDS.sleep(1);
					System.out.println(tag + ": call future 1.");
				}
				return 1;
			}
		});

		ListenableFuture<Integer> future2 = service.submit(new Callable<Integer>() {
			@Override
			public Integer call() throws InterruptedException {
				String tag = Thread.currentThread().getName();
				for (int i = 0; i < 5; i++) {
					TimeUnit.SECONDS.sleep(1);
					System.out.println(tag + ": call future 2.");
				}
				return 2;
			}
		});

		final ListenableFuture<List<Integer>> allFutures = Futures.allAsList(future1, future2);

		final ListenableFuture<String> transform = Futures.transformAsync(allFutures,
				new AsyncFunction<List<Integer>, String>() {
					@Override
					public ListenableFuture<String> apply(List<Integer> results) throws Exception {
						return Futures.immediateFuture("success future: " + results.toString());
					}
				}, service);

		Futures.addCallback(transform, new FutureCallback<String>() {
			@Override
			public void onSuccess(String result) {
				System.out.println(result.getClass());
				System.out.println(Thread.currentThread().getName() + ": success: " + result);
			}

			@Override
			public void onFailure(Throwable t) {
				System.out.println("fail: " + t.getMessage());
			}
		}, service);

		// sync wait to complete
		System.out.println("transform results: " + transform.get());
	}

	/**
	 * Verify: <? extends T> and <? super T>
	 */
	private void TestGeneric() {
		// <? extends Fruit>会使set()方法失效
		Plate<? extends Fruit> p1 = new Plate<Apple>(new Apple());
//		p1.set(new Fruit()); // Error
//		p1.set(new Banana()); // Error
		Fruit f = p1.get();
		System.out.println(f);

		// <? super Fruit>使get()方法的返回值只能存放到Object对象里
		Plate<? super Fruit> p2 = new Plate<Fruit>(new Fruit());
		p2.set(new Banana());
		Object o = p2.get();
		System.out.println(o);
		System.out.println();

		PrintFruit(new Plate<Apple>(new Apple()));
		PrintFruit(new Plate<Banana>(new Banana()));
		System.out.println();
	}

	private void PrintFruit(Plate<? extends Fruit> p) {
		System.out.println(p.get().toString());
	}

	private static class Fruit {

		@Override
		public String toString() {
			return "this is fruit";
		}
	}

	private static class Apple extends Fruit {

		@Override
		public String toString() {
			return "this is apple";
		}
	}

	private static class Banana extends Fruit {

		@Override
		public String toString() {
			return "this is banana";
		}
	}

	private static class Plate<T> {

		private T item;

		public Plate(T t) {
			this.item = t;
		}

		public T get() {
			return item;
		}

		public void set(T item) {
			this.item = item;
		}
	}

}
