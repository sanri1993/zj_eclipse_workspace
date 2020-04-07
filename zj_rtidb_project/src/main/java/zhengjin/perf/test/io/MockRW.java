package zhengjin.perf.test.io;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import zhengjin.perf.test.PerfTestEnv;

public final class MockRW implements DBReadWriter {

	private static final Logger LOG = LoggerFactory.getLogger(MockRW.class);

	// Note: debug fields are non atomic
	private static boolean isDebug = PerfTestEnv.isDebug;
	private static int debugCount = 0;
	private static long debugSum = 0L;

	private static int debugHotKeyCount = 0;
	private static int hotKeyLine = PerfTestEnv.keyRangeStart
			+ (int) ((PerfTestEnv.keyRangeEnd - PerfTestEnv.keyRangeStart) * 0.2);

	private final int randBase = 100;
	private Random rand;

	public MockRW() {
		rand = new Random();
		rand.setSeed(666L);

		debugCount = 0;
		debugSum = 0L;
	}

	@Override
	public boolean put(String tbName, Map<String, Object> row) throws Exception {
		int t = rand.nextInt(randBase);
		if (isDebug) {
			debugCount++;
			debugSum += t;
		}
		TimeUnit.MILLISECONDS.sleep(t);
		return true;
	}

	@Override
	public Object[] get(String tbName, String key) throws Exception {
		int t = rand.nextInt(randBase);
		if (isDebug) {
			int val = Integer.valueOf(key.substring(PerfTestEnv.keyPrefix.length()));
			if (val < hotKeyLine) {
				debugHotKeyCount++;
			}
			debugCount++;
			debugSum += t;
		}
		TimeUnit.MILLISECONDS.sleep(t);
		return new String[] { key, String.valueOf(System.nanoTime()) };
	}

	public static void debugInfo() {
		if (isDebug) {
			String text = String.format("[RW debug]: count:%d, avg rt:%.2f", debugCount,
					(debugSum / (float) debugCount)) + PerfTestEnv.rsTimeUnit;
			if ("get".equals(PerfTestEnv.action)) {
				String more = String.format("hotkey count:%d, hotkey rate:%.2f", debugHotKeyCount,
						(100 * debugHotKeyCount / (float) debugCount)) + "%";
				text += ", " + more;
			}
			LOG.info(text);
		}
	}

}
