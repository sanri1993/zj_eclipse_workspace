package zhengjin.perf.test.process;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import zhengjin.perf.test.PerfTest;
import zhengjin.perf.test.PerfTestEnv;
import zhengjin.perf.test.io.MockRW;

/**
 * 
 * Print performance test summary info matrix at fixed interval for Create Data
 * action.
 *
 */
public final class CreateMatrixProcess implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(CreateMatrixProcess.class);

	static AtomicInteger matrixCounts = new AtomicInteger(0);
	static AtomicInteger num = new AtomicInteger(0);

	@Override
	public void run() {
		final String tag = Thread.currentThread().getName();
		final int allCount = PerfTestEnv.keyRangeEnd - PerfTestEnv.keyRangeStart + 1;
		ThreadPoolExecutor pool = (ThreadPoolExecutor) PerfTest.svc;

		LOG.info("[{}]: MATRIX start", tag);
		while (pool.getActiveCount() > 0) {
			if (num.get() == PerfTestEnv.threads) {
				float percent = matrixCounts.get() / (float) allCount;
				LOG.info(String.format("[Matrix]: create data process: %.1f", (percent * 100)) + "%");
				MockRW.debugInfo();
				num.set(0);
			}

			try {
				TimeUnit.SECONDS.sleep(3L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		PerfTest.svc.shutdown();
		PerfTest.scheSvc.shutdown();
		LOG.info("[Matrix]: create data process: 100%");
		MockRW.debugInfo();
		LOG.info("PERF TEST END");
	}

}
