package zhengjin.perf.test.process;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import zhengjin.perf.test.PerfTest;
import zhengjin.perf.test.PerfTestEnv;
import zhengjin.perf.test.io.DBReadWriter;

/**
 * 
 * @author zhengjin Performance test for put action. Put data by random unique
 *         key and inserted ts.
 *
 */
public final class PutProcess implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(PutProcess.class);

	DBReadWriter rw;

	public PutProcess(DBReadWriter rw) {
		this.rw = rw;
	}

	@Override
	public void run() {
		final String tag = Thread.currentThread().getName();
		final long interval = PerfTestEnv.matrixInterval * 1000L;

		int failCount = 0;
		List<Long> elapsedTimes = new LinkedList<Long>();
		HashMap<String, Object> row = new HashMap<String, Object>();

		long pStart = System.currentTimeMillis();
		LOG.info("[{}]: PUT started", tag);
		while (PerfTest.isRunning) {
			PerfTest.limit.acquire();

			// TODO: create random row with unique key and ts depend on table schema
			long uniqueTs = System.nanoTime();
			row.put("id", PerfTestEnv.keyPrefix + String.valueOf(uniqueTs));
			row.put("insert_ts", uniqueTs);

			long putStart = System.nanoTime();
			try {
				if (!rw.put("tbname", row)) {
					failCount++;
				}
			} catch (Exception e) {
				LOG.warn(e.getMessage());
				failCount++;
			} finally {
				elapsedTimes.add(this.formatTimeUnit(System.nanoTime() - putStart));
				row.clear();
			}

			long pEnd = System.currentTimeMillis();
			if ((pEnd - pStart) > interval) {
				LOG.info("[{}]: sync maxtrix data", tag);
				syncMatrixData(failCount, elapsedTimes);

				pStart = pEnd;
				failCount = 0;
				elapsedTimes.clear();
			}
		}

		syncMatrixData(failCount, elapsedTimes);
		LOG.info("[{}]: PUT end", tag);
	}

	private void syncMatrixData(int failCount, List<Long> elapsedTimes) {
		MatrixProcess.matrixFailCounts.addAndGet(failCount);
		MatrixProcess.matrixElapsed.addAll(elapsedTimes);
		MatrixProcess.num.incrementAndGet();
	}

	private long formatTimeUnit(long time) {
		return "ms".equals(PerfTestEnv.rsTimeUnit) ? time / 1000L / 1000L : time / 1000L;
	}

}
