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

	private static volatile boolean running = true;

	private ExecutorService execSvc;
	private ScheduledExecutorService scheExecSvc;
	private int threadNum;
	private int runSecs;

	public MultipleProcess(int threadNum, int runSecs) {
		this.threadNum = threadNum;
		this.runSecs = runSecs;
	}

	public static boolean isRunning() {
		return running;
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
		running = false;
	}

	private void stopProcessesByDelay(int delay) {
		this.scheExecSvc = Executors.newScheduledThreadPool(1);
		this.scheExecSvc.schedule(new MyProcess() {
			@Override
			public void run() {
				LOG.info("Stop Processes");
				running = false;
				MultipleProcess.this.execSvc.shutdown();
				MultipleProcess.this.scheExecSvc.shutdown();
			}
		}, delay, TimeUnit.SECONDS);
	}

	private static class MyProcess implements Runnable {

		@Override
		public void run() {
			while (isRunning()) {
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
		p.runProcesses(new MyProcess());
		LOG.info("mutliple processes test DONE.");
	}

}
