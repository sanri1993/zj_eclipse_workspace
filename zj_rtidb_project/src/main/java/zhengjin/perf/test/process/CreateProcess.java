package zhengjin.perf.test.process;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import zhengjin.perf.test.PerfTest;
import zhengjin.perf.test.PerfTestEnv;
import zhengjin.perf.test.io.DBReadWriter;

/**
 * 
 * Create rows data by key range [start, end).
 *
 */
public final class CreateProcess implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(PutRowsProcess.class);

	private DBReadWriter rw;
	int keyStart;
	int keyEnd;

	public CreateProcess(DBReadWriter rw, int start, int end) {
		this.rw = rw;
		this.keyStart = start;
		this.keyEnd = end;
	}

	@Override
	public void run() {
		final String tag = Thread.currentThread().getName();
		final long interval = PerfTestEnv.matrixInterval * 1000L;

		int count = 0;
		HashMap<String, Object> row = new HashMap<String, Object>();

		LOG.info("[{}]: CREATE DATA start by key range [{}:{})", tag, this.keyStart, this.keyEnd);
		long pStart = System.currentTimeMillis();
		long pEnd = pStart;
		for (int i = this.keyStart; i < this.keyEnd; i++) {
			PerfTest.limit.acquire();

			row.put("id", PerfTestEnv.keyPrefix + String.valueOf(i));
			row.put("insert_ts", System.nanoTime());
			try {
				rw.put("tbname", row);
			} catch (Exception e) {
				LOG.warn(e.getMessage());
			} finally {
				count++;
				row.clear();
			}

			pEnd = System.currentTimeMillis();
			if ((pEnd - pStart) > interval) {
				LOG.info("[{}]: sync maxtrix data", tag);
				CreateMatrixProcess.matrixCounts.addAndGet(count);
				CreateMatrixProcess.num.incrementAndGet();

				pStart = pEnd;
				count = 0;
			}
		}

		CreateMatrixProcess.matrixCounts.addAndGet(count);
		LOG.info("[{}]: CREATE DATA end", tag);
	}

}
