package zhengjin.perf.test.process;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import zhengjin.perf.test.PerfTest;
import zhengjin.perf.test.PerfTestEnv;
import zhengjin.perf.test.io.DBReadWriter;

/**
 * 
 * Performance test for get action by 2-8 hot key.
 *
 */
public final class GetRowsProcess implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(PutRowsProcess.class);

	private DBReadWriter rw;

	public GetRowsProcess(DBReadWriter rw) {
		this.rw = rw;
	}

	@Override
	public void run() {
		final String tag = Thread.currentThread().getName();
		final long interval = PerfTestEnv.matrixInterval * 1000L;

		int failCount = 0;
		List<Long> elapsedTimes = new LinkedList<Long>();

		LOG.info("[{}]: GET ROWS start", tag);
		long pStart = System.currentTimeMillis();
		long pEnd = pStart;
		while (PerfTest.isRunning) {
			PerfTest.limit.acquire();

			long getStart = System.nanoTime();
			try {
				Object[] row = rw.get("tbname", this.getHotKey());
				if (row != null) {
					LOG.debug("get row: " + Arrays.toString(row));
				}
			} catch (Exception e) {
				e.printStackTrace();
				failCount++;
			} finally {
				elapsedTimes.add(BaseUtils.formatTimeUnit(System.nanoTime() - getStart));
			}

			pEnd = System.currentTimeMillis();
			if ((pEnd - pStart) > interval) {
				LOG.info("[{}]: sync maxtrix data", tag);
				BaseUtils.syncMatrixData(failCount, elapsedTimes);

				pStart = pEnd;
				failCount = 0;
				elapsedTimes.clear();
			}
		}

		BaseUtils.syncMatrixData(failCount, elapsedTimes);
		LOG.info("[{}]: GET ROWS end", tag);
	}

	private String getHotKey() {
		// 80%的请求访问20%的热点key
		int count = PerfTestEnv.keyRangeEnd - PerfTestEnv.keyRangeStart + 1;
		Random rand = new Random();
		int percent = rand.nextInt(100);

		if (percent < 80) {
			float offset = 0.2F * rand.nextFloat();
			return PerfTestEnv.keyPrefix + (int) (PerfTestEnv.keyRangeStart + count * offset);
		} else {
			float offset = 0.8F * rand.nextFloat();
			return PerfTestEnv.keyPrefix + (int) (PerfTestEnv.keyRangeStart + count * 0.2 + count * offset);
		}
	}

}
