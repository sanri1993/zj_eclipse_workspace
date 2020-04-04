package zhengjin.perf.test;

import zhengjin.perf.test.io.DBReadWriter;

public final class PerfTest {

	public static class PutProcess implements Runnable {

		DBReadWriter rw;

		public PutProcess(DBReadWriter rw) {
			super();
			this.rw = rw;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
		}
	}

}
