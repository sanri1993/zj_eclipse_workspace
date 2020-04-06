package zhengjin.perf.test.process;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import zhengjin.perf.test.PerfTest;
import zhengjin.perf.test.io.DBReadWriter;

public final class GetRowsProcess implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(PutRowsProcess.class);

	private DBReadWriter rw;

	public GetRowsProcess(DBReadWriter rw) {
		this.rw = rw;
	}

	@Override
	public void run() {
		final String tag = Thread.currentThread().getName();
		LOG.info("[{}]: GET ROWS started", tag);

		while (PerfTest.isRunning) {
			PerfTest.limit.acquire();
			try {
				rw.get("tbname", this.getHotKey());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		LOG.info("[{}]: GET ROWS end", tag);
	}

	private String getHotKey() {
		return "";
	}

}
