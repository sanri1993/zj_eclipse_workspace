package zhengjin.rtidb.runner;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import zhengjin.rtidb.app.RtidbApp;

public final class MultipleProcess {

	private static final Logger LOG = LoggerFactory.getLogger(RtidbApp.class);

	private volatile boolean running = false;

	private final int runSecs;

	private ExecutorService execSvc;
	private ScheduledExecutorService scheExecSvc;

	public MultipleProcess(int runSecs) {
		this.runSecs = runSecs;
	}

	public boolean isRunning() {
		return this.running;
	}

	public void runProcesses(Runnable r, int threadNum) throws InterruptedException, ExecutionException {
		if (this.isRunning()) {
			LOG.warn("There are processes running, and exit!");
			return;
		}

		List<Runnable> list = new LinkedList<>();
		for (int i = 0; i < threadNum; i++) {
			list.add(r);
		}
		this.runProcesses(list);
	}

	public void runProcesses(List<Runnable> list) throws InterruptedException, ExecutionException {
		if (this.isRunning()) {
			LOG.warn("There are processes running, and exit!");
			return;
		}

		this.running = true;
		this.execSvc = Executors.newFixedThreadPool(list.size());
		List<Future<?>> futures = new LinkedList<>();
		LOG.info("Start Processes");
		for (Runnable r : list) {
			futures.add(this.execSvc.submit(r));
		}
		this.stopProcessesByDelay(this.runSecs);

		for (Future<?> f : futures) {
			f.get();
		}
		if (this.running) {
			LOG.info("All tasks are finished, and Stop.");
			this.running = false;
			this.execSvc.shutdown();
			this.scheExecSvc.shutdownNow();
		}
	}

	private void stopProcessesByDelay(int delay) {
		this.scheExecSvc = Executors.newScheduledThreadPool(1);
		this.scheExecSvc.schedule(new Runnable() {
			@Override
			public void run() {
				LOG.info("After run seconds, and Stop processes.");
				MultipleProcess.this.running = false;
				MultipleProcess.this.execSvc.shutdown();
				MultipleProcess.this.scheExecSvc.shutdown();
			}
		}, delay, TimeUnit.SECONDS);
	}

	/**
	 * Generate unique key for each iterator.
	 * 
	 * @author zhengjin
	 *
	 */
	private static class MyProcess implements Runnable {

		private static AtomicInteger key1 = new AtomicInteger(new Random().nextInt(100));

		private MultipleProcess runner;

		public MyProcess(MultipleProcess p) {
			this.runner = p;
		}

		@Override
		public void run() {
			long threadId = Thread.currentThread().getId();
			while (this.runner.isRunning()) {
				LOG.info("MyProcess Thread [{}]: running process test...", threadId);
				LOG.info("MyProcess Thread [{}]: key1={}", threadId, key1.getAndIncrement());
				LOG.info("MyProcess Thread [{}]: key2={}", threadId, System.nanoTime());
				try {
					TimeUnit.MILLISECONDS.sleep(500L);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Generate unique key for each iterator.
	 * 
	 * @author zhengjin
	 *
	 */
	private static class MyProcess02 implements Runnable {

		private int start, end;
		private MultipleProcess runner;

		public MyProcess02(MultipleProcess p, int start, int end) {
			this.runner = p;
			this.start = start;
			this.end = end;
		}

		@Override
		public void run() {
			long threadId = Thread.currentThread().getId();
			while (this.runner.isRunning() && this.start < this.end) {
				LOG.info("MyProcess02 Thread [{}]: running process test...", threadId);
				LOG.info("MyProcess02 Thread [{}]: key={}", threadId, this.start++);
				try {
					TimeUnit.MILLISECONDS.sleep(50L);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void main(String[] args) throws Exception {

		MultipleProcess p;

		// run same processes
		boolean runFlag1 = false;
		if (runFlag1) {
			final int runSeconds = 5;
			p = new MultipleProcess(runSeconds);
			p.runProcesses(new MyProcess(p), 3);
		}

		// run diff processes
		boolean runFlag2 = true;
		if (runFlag2) {
			final int timeout = 3;
			p = new MultipleProcess(timeout);

			List<Runnable> list = new LinkedList<>();
			list.add(new MyProcess02(p, 0, 100));
			list.add(new MyProcess02(p, 100, 200));
			list.add(new MyProcess02(p, 200, 300));
			p.runProcesses(list);
		}

		LOG.info("mutliple processes runner test DONE.");
	}

}
