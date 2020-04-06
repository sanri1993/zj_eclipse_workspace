package zhengjin.perf.test.process;

import java.util.List;

import zhengjin.perf.test.PerfTestEnv;

public final class BaseUtils {

	static void syncMatrixData(int failCount, List<Long> elapsedTimes) {
		PerfTestMatrixProcess.matrixFailCounts.addAndGet(failCount);
		PerfTestMatrixProcess.matrixElapsed.addAll(elapsedTimes);
		PerfTestMatrixProcess.num.incrementAndGet();
	}

	static long formatTimeUnit(long time) {
		return "ms".equals(PerfTestEnv.rsTimeUnit) ? time / 1000L / 1000L : time / 1000L;
	}

}
