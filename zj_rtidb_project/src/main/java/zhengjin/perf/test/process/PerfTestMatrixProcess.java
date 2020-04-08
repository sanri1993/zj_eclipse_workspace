package zhengjin.perf.test.process;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
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
 * 1) Print performance test summary info matrix at fixed interval for Put and
 * Get; 2) Check all active threads.
 *
 */
public final class PerfTestMatrixProcess implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(PerfTestMatrixProcess.class);

	// number of threads which set matrix data
	static AtomicInteger num = new AtomicInteger(0);
	// sync fields save matrix data from perf test started
	static AtomicInteger matrixFailCounts = new AtomicInteger(0);
	static CopyOnWriteArrayList<Long> matrixElapsed = new CopyOnWriteArrayList<Long>();

	// TODO: all matrix data are in memory, prefer to dump to IO file at the
	// sametime.

	@Override
	public void run() {
		final String tag = Thread.currentThread().getName();
		ThreadPoolExecutor pool = (ThreadPoolExecutor) PerfTest.svc;

		LOG.info("[{}]: MATRIX start", tag);
		while (PerfTest.isRunning) {
			if (num.get() == PerfTestEnv.threads) {
				this.printSummaryContent();
				this.printLinesContent();
				MockRW.debugInfo();
				num.set(0);
			}

			// check all threads are active
			if (PerfTest.isRunning && (pool.getActiveCount() < PerfTestEnv.threads)) {
				PerfTest.stop();
				LOG.error("now active threads count is {}, less than {}", pool.getActiveCount(), PerfTestEnv.threads);
				LOG.error("PERF TEST END WITH ERROR");
				return;
			}

			try {
				TimeUnit.SECONDS.sleep(3L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		while (!PerfTest.svc.isTerminated()) {
			try {
				TimeUnit.MILLISECONDS.sleep(300L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		this.printSummaryContent();
		this.printLinesContent();
		MockRW.debugInfo();
		LOG.info("PERF TEST END");
	}

	private void printSummaryContent() {
		int allCnt = matrixElapsed.size();
		int failCnt = matrixFailCounts.get();

		long sum = 0;
		for (long elapsed : matrixElapsed) {
			sum += elapsed;
		}
		float avg = sum / (float) allCnt;

		float rps = allCnt / ((System.currentTimeMillis() - PerfTest.start) / 1000L);

		String summaryContent = String.format("samplers:%d, failed:%d, rps:%.2f, avg:%.2fms", allCnt, failCnt, rps,
				avg);
		LOG.info("[Matrix]: " + summaryContent);
	}

	private void printLinesContent() {
		Collections.sort(matrixElapsed);
		int size = matrixElapsed.size();
		int line90 = ((int) Math.round(size * 0.9F) - 1);
		int line99 = ((int) Math.round(size * 0.99F) - 1);
		int line999 = ((int) Math.round(size * 0.999F) - 1);
		int line9999 = ((int) Math.round(size * 0.9999F) - 1);

		List<String> lines = new ArrayList<String>();
		lines.add("line90:" + matrixElapsed.get(line90));
		lines.add("line99:" + matrixElapsed.get(line99));
		lines.add("line999:" + matrixElapsed.get(line999));
		lines.add("line9999:" + matrixElapsed.get(line9999));
		String lineContent = String.join(PerfTestEnv.rsTimeUnit + ", ", lines);
		LOG.info("[Lines]: " + lineContent + PerfTestEnv.rsTimeUnit);
	}

}
