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

public final class PerfTest {

	private static final Logger LOG = LoggerFactory.getLogger(PerfTest.class);

	static long start;
	static boolean isRunning = false;
	static ExecutorService svc = Executors.newFixedThreadPool(PerfTestEnv.threads + 1);
	static ScheduledExecutorService scheSvc = Executors.newScheduledThreadPool(1);
	static RateLimiter limit;

	public void exec(DBReadWriter rw) throws InterruptedException {
		PerfTestEnv.printPerfTestEnv();
		limit = RateLimiter.create(PerfTestEnv.rpsLimit);

		LOG.info("PERF TEST START");
		isRunning = true;
		start = System.currentTimeMillis();
		for (int i = 0; i < PerfTestEnv.threads; i++) {
			svc.submit(new PutProcess(rw));
		}
		TimeUnit.MILLISECONDS.sleep(1000L);
		svc.submit(new MatrixProcess());

		scheSvc.schedule(new Runnable() {
			@Override
			public void run() {
				isRunning = false;
				svc.shutdown();
				scheSvc.shutdown();
				LOG.info("PERF TEST END");
			}

		}, PerfTestEnv.runTime, TimeUnit.SECONDS);
	}

	public static void main(String[] args) throws InterruptedException {

		PerfTest test = new PerfTest();
		test.exec(new MockRW());
	}

}
