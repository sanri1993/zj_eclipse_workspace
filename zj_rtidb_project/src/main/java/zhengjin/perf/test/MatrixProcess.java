package zhengjin.perf.test;

import java.util.Collections;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author zhengjin Performance test daemon process: 1) Print performance test
 *         summary info matrix at specified interval. 2) Check all active
 *         threads.
 *
 */
public class MatrixProcess implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(MatrixProcess.class);

	// fields save matrix data from performance test started.
	static AtomicInteger matrixPassCounts = new AtomicInteger(0);
	static AtomicInteger matrixFailCounts = new AtomicInteger(0);
	static CopyOnWriteArrayList<Long> matrixElapsed = new CopyOnWriteArrayList<Long>();
	// number of threads set the matrix data.
	static AtomicInteger num = new AtomicInteger(0);

	@Override
	public void run() {
		final String tag = Thread.currentThread().getName();
		ThreadPoolExecutor pool = (ThreadPoolExecutor) PerfTest.svc;

		LOG.info("[{}]: MATRIX started", tag);
		while (PerfTest.isRunning) {
			if (num.get() > 0 && num.get() % PerfTestEnv.threads == 0) {
				LOG.info("[{}]: print perf test summary info matrix.", tag);
				this.printSummaryContent();
				this.printLinesContent();
				num.set(0);
			}

			try {
				// check all threads are running
				if (pool.getActiveCount() < (PerfTestEnv.threads)) {
					PerfTest.isRunning = false;
					PerfTest.svc.shutdown();
					PerfTest.scheSvc.shutdown();
					LOG.error("[{}]: active threads {} is less than {}", tag, pool.getActiveCount(),
							PerfTestEnv.threads);
					LOG.error("PERF TEST END WITH ERROR");
					return;
				}
				TimeUnit.SECONDS.sleep(3L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		this.printSummaryContent();
		this.printLinesContent();
	}

	private void printSummaryContent() {
		int allCnt = matrixElapsed.size();
		int failCnt = matrixFailCounts.get();
		float rps = allCnt / ((System.currentTimeMillis() - PerfTest.start) / 1000L);
		String summaryContent = String.format("samplers:%d, failed:%d, qps:%.2f", allCnt, failCnt, rps);
		LOG.info("[Matrix] " + summaryContent);
	}

	private void printLinesContent() {
		Collections.sort(matrixElapsed);
		int size = matrixElapsed.size();
		int line90 = ((int) Math.round(size * 0.9F) - 1);
		int line99 = ((int) Math.round(size * 0.99F) - 1);
		int line999 = ((int) Math.round(size * 0.999F) - 1);
		int line9999 = ((int) Math.round(size * 0.9999F) - 1);
		String lineContent = String.format("tp90:%dus, tp99:%dus, tp999:%dus, tp999:%dus", matrixElapsed.get(line90),
				matrixElapsed.get(line99), matrixElapsed.get(line999), matrixElapsed.get(line9999));
		LOG.info(lineContent);
	}

}
