package zhengjin.perf.test.process;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import zhengjin.perf.test.PerfTest;
import zhengjin.perf.test.PerfTestEnv;
import zhengjin.perf.test.io.DBReadWriter;

/**
 * 
 * @author zhengjin Create data by key range [start, end).
 *
 */
public final class CreateDataProcess implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(PutRowsProcess.class);

	private DBReadWriter rw;
	int keyStart;
	int keyEnd;

	public CreateDataProcess(DBReadWriter rw, int start, int end) {
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

		LOG.info("[{}]: CREATE DATA started", tag);
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

			if ((pEnd - pStart) > interval) {
				LOG.info("[{}]: sync maxtrix data", tag);
				CreateDataMatrixProcess.matrixCounts.addAndGet(count);

				pStart = pEnd;
				count = 0;
			}
		}

		CreateDataMatrixProcess.matrixCounts.addAndGet(count);
		LOG.info("[{}]: CREATE DATA end", tag);
	}

}
