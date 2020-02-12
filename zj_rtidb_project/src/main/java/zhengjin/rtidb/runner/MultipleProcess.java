package zhengjin.rtidb.runner;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import zhengjin.rtidb.app.RtidbApp;

public final class MultipleProcess {

	private static final Logger LOG = LoggerFactory.getLogger(RtidbApp.class);

	private volatile boolean running = true;

	private final int threadNum;
	private final int runSecs;

	private ExecutorService execSvc;
	private ScheduledExecutorService scheExecSvc;

	public MultipleProcess(int threadNum, int runSecs) {
		this.threadNum = threadNum;
		this.runSecs = runSecs;
	}

	public boolean isRunning() {
		return this.running;
	}

	public void runProcesses(Runnable r) {
		LOG.info("Start Processes");
		this.execSvc = Executors.newFixedThreadPool(this.threadNum);
		for (int i = 0; i < this.threadNum; i++) {
			this.execSvc.submit(r);
		}
		this.stopProcessesByDelay(this.runSecs);
	}

	public void stopProcesses() {
		this.running = false;
		this.execSvc.shutdown();
		this.scheExecSvc.shutdown();
	}

	private void stopProcessesByDelay(int delay) {
		this.scheExecSvc = Executors.newScheduledThreadPool(1);
		this.scheExecSvc.schedule(new Runnable() {
			@Override
			public void run() {
				LOG.info("Stop Processes");
				MultipleProcess.this.stopProcesses();
			}
		}, delay, TimeUnit.SECONDS);
	}

	private static class MyProcess implements Runnable {

		private MultipleProcess runner;

		public MyProcess(MultipleProcess p) {
			this.runner = p;
		}

		@Override
		public void run() {
			while (this.runner.isRunning()) {
				LOG.info("Thread [{}]: running process test...", Thread.currentThread().getId());
				try {
					TimeUnit.SECONDS.sleep(3);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void main(String[] args) throws Exception {

		MultipleProcess p = new MultipleProcess(3, 10);
		p.runProcesses(new MyProcess(p));
		LOG.info("mutliple processes test DONE.");
	}

}
