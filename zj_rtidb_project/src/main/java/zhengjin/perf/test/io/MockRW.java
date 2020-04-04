package zhengjin.perf.test.io;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public final class MockRW implements DBReadWriter {

	Random rand = new Random();

	@Override
	public boolean put(String tbName, Map<String, Object> row) throws Exception {
		TimeUnit.MILLISECONDS.sleep(rand.nextInt(100));
		return true;
	}

	@Override
	public Object[] get(String tbName, String key) throws Exception {
		TimeUnit.MILLISECONDS.sleep(rand.nextInt(100));
		return new String[] { key, String.valueOf(System.nanoTime()) };
	}

}
