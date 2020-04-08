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
 * Performance test for put action. Put data by random and unique key, and
 * inserted ts. Put process sync perf data to Matrix thread at fixed interval.
 *
 */
public final class PutRowsProcess implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(PutRowsProcess.class);

	private DBReadWriter rw;

	public PutRowsProcess(DBReadWriter rw) {
		this.rw = rw;
	}

	@Override
	public void run() {
		final String tag = Thread.currentThread().getName();
		// interval for sync data to matrix process
		final long interval = PerfTestEnv.matrixInterval * 1000L;

		int failCount = 0;
		List<Long> elapsedTimes = new LinkedList<Long>();
		HashMap<String, Object> row = new HashMap<String, Object>();

		LOG.info("[{}]: PUT ROWS start", tag);
		long pStart = System.currentTimeMillis();
		long pEnd = pStart;
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
				elapsedTimes.add(BaseUtils.formatTimeUnit(System.nanoTime() - putStart));
				row.clear();
			}

			// sync matrix data by interval
			pEnd = System.currentTimeMillis();
			if ((pEnd - pStart) > interval) {
				LOG.info("[{}]: sync maxtrix data", tag);
				// TODO: save matrix data to IO file
				BaseUtils.syncMatrixData(failCount, elapsedTimes);

				pStart = pEnd;
				failCount = 0;
				elapsedTimes.clear();
			}
		}

		BaseUtils.syncMatrixData(failCount, elapsedTimes);
		LOG.info("[{}]: PUT ROWS end", tag);
	}

}
