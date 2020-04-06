package zhengjin.perf.test.process;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import zhengjin.perf.test.PerfTest;
import zhengjin.perf.test.PerfTestEnv;

public final class CreateDataMatrixProcess implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(CreateDataMatrixProcess.class);

	static AtomicInteger matrixCounts = new AtomicInteger(0);
	static AtomicInteger num = new AtomicInteger(0);

	@Override
	public void run() {
		final String tag = Thread.currentThread().getName();
		LOG.info("[{}]: MATRIX started", tag);

		while (!PerfTest.svc.isTerminated()) {
			if (num.get() > 0 && (num.get() % PerfTestEnv.threads == 0)) {
				float percent = matrixCounts.get() / (float) (PerfTestEnv.keyRangeEnd - PerfTestEnv.keyRangeStart + 1);
				LOG.info(String.format("[Matrix]: create data process %.2f", (percent * 100)) + "%");
				num.set(0);
			}

			try {
				TimeUnit.SECONDS.sleep(3L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		LOG.info("[Matrix]: create data process: 100%");
		LOG.info("CREATE DATA END");
	}

}
