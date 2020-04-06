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
import zhengjin.perf.test.process.MatrixProcess;
import zhengjin.perf.test.process.PutProcess;

public final class PerfTest {

	private static final Logger LOG = LoggerFactory.getLogger(PerfTest.class);

	public static long start;
	public static boolean isRunning = false;
	public static ExecutorService svc = Executors.newFixedThreadPool(PerfTestEnv.threads);
	public static ScheduledExecutorService scheSvc = Executors.newScheduledThreadPool(2);
	public static RateLimiter limit;

	public void exec(DBReadWriter rw) throws InterruptedException {
		PerfTestEnv.printPerfTestEnv();
		limit = RateLimiter.create(PerfTestEnv.rpsLimit);

		LOG.info("PERF TEST START");
		isRunning = true;
		start = System.currentTimeMillis();
		for (int i = 0; i < PerfTestEnv.threads; i++) {
			svc.submit(new PutProcess(rw));
		}

		scheSvc.schedule(new MatrixProcess(), PerfTestEnv.matrixInterval, TimeUnit.SECONDS);

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

		PerfTest test = new PerfTest();
		test.exec(new MockRW());
	}

}
