package zhengjin.perf.test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.RateLimiter;

import zhengjin.perf.test.io.DBReadWriter;
import zhengjin.perf.test.io.MockRW;
import zhengjin.perf.test.process.CreateProcess;
import zhengjin.perf.test.process.CreateMatrixProcess;
import zhengjin.perf.test.process.GetRowsProcess;
import zhengjin.perf.test.process.PerfTestMatrixProcess;
import zhengjin.perf.test.process.PutRowsProcess;

/**
 * 
 * 1.rw线程指定间隔同步数据到matrix线程，展示matrix数据 2.对keyRange进行分桶，来造数据 3.
 * 基于2-8原则，对热点key进行get压测
 *
 */
public final class PerfTest {

	private static final Logger LOG = LoggerFactory.getLogger(PerfTest.class);

	public static long start;
	public static boolean isRunning = false;
	public static ExecutorService svc = Executors.newFixedThreadPool(PerfTestEnv.threads);
	public static ScheduledExecutorService scheSvc;
	public static RateLimiter limit;

	void runCreateData(DBReadWriter rw) {
		int rangeStart = PerfTestEnv.keyRangeStart;
		int rangeEnd = PerfTestEnv.keyRangeEnd;

		// key根据执行的线程数进行分桶
		int range = (rangeEnd - rangeStart + 1) / PerfTestEnv.threads;
		int remained = (rangeEnd - rangeStart + 1) % PerfTestEnv.threads;
		for (int i = 0; i < PerfTestEnv.threads; i++) {
			int start = rangeStart + range * i;
			int end = start + range;
			if (i == (PerfTestEnv.threads - 1)) {
				end += remained;
			}
			svc.submit(new CreateProcess(rw, start, end));
		}
		scheSvc = Executors.newScheduledThreadPool(1);
		scheSvc.schedule(new CreateMatrixProcess(), PerfTestEnv.matrixInterval, TimeUnit.SECONDS);
	}

	void runPut(DBReadWriter rw) throws InterruptedException {
		this.exec(new PutRowsProcess(rw));
	}

	void runGet(DBReadWriter rw) throws InterruptedException {
		this.exec(new GetRowsProcess(rw));
	}

	private void exec(Runnable process) throws InterruptedException {
		isRunning = true;
		start = System.currentTimeMillis();
		for (int i = 0; i < PerfTestEnv.threads; i++) {
			svc.submit(process);
		}

		scheSvc = Executors.newScheduledThreadPool(2);
		scheSvc.schedule(new PerfTestMatrixProcess(), PerfTestEnv.matrixInterval, TimeUnit.SECONDS);

		scheSvc.schedule(new Runnable() {
			@Override
			public void run() {
				stop();
			}
		}, PerfTestEnv.runTime, TimeUnit.SECONDS);
	}

	public static void stop() {
		isRunning = false;
		svc.shutdown();
		scheSvc.shutdown();
	}

	public static void main(String[] args) throws InterruptedException {

		PerfTestEnv.printPerfTestEnv();
		limit = RateLimiter.create(PerfTestEnv.rpsLimit);
		DBReadWriter rw = new MockRW();

		LOG.info("PERF TEST START");
		PerfTest test = new PerfTest();
		if ("put".equals(PerfTestEnv.action)) {
			// perf test for db put by random and unique key
			test.runPut(rw);
		} else if ("get".equals(PerfTestEnv.action)) {
			// perf test for db get with 2-8 hotkey
			test.runGet(rw);
		} else if ("create".equals(PerfTestEnv.action)) {
			// create data by key range
			test.runCreateData(rw);
		} else {
			LOG.error("invalid action: " + PerfTestEnv.action);
		}
	}

}
