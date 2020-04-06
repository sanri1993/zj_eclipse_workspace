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
import zhengjin.perf.test.process.CreateDataProcess;
import zhengjin.perf.test.process.CreateDataMatrixProcess;
import zhengjin.perf.test.process.GetRowsProcess;
import zhengjin.perf.test.process.PerfTestMatrixProcess;
import zhengjin.perf.test.process.PutRowsProcess;

public final class PerfTest {

	private static final Logger LOG = LoggerFactory.getLogger(PerfTest.class);

	public static long start;
	public static boolean isRunning = false;
	public static ExecutorService svc = Executors.newFixedThreadPool(PerfTestEnv.threads);
	public static ScheduledExecutorService scheSvc = Executors.newScheduledThreadPool(2);
	public static RateLimiter limit;

	void runCreateData(DBReadWriter rw) {
		int rangeStart = PerfTestEnv.keyRangeStart;
		int rangeEnd = PerfTestEnv.keyRangeEnd;

		int range = (rangeEnd - rangeStart + 1) / PerfTestEnv.threads;
		int remained = (rangeEnd - rangeStart + 1) % PerfTestEnv.threads;
		for (int i = 0; i < PerfTestEnv.threads; i++) {
			int start = rangeStart + range * i;
			int end = start + range;
			if (i == (PerfTestEnv.threads - 1)) {
				end += remained;
			}
			svc.submit(new CreateDataProcess(rw, start, end));
		}

		scheSvc.schedule(new CreateDataMatrixProcess(), PerfTestEnv.matrixInterval, TimeUnit.SECONDS);
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
		test.runPut(rw);
	}

}
