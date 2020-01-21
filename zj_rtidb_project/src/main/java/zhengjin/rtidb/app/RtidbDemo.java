package zhengjin.rtidb.app;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public final class RtidbDemo {

	private static final RtidbUtils rtidb = RtidbUtils.getInstance();

	public void schemaTablePutPerfTest01(int count) throws Exception {
		long ts = System.currentTimeMillis();
		long start;
		Random rand = new Random();
		List<Long> timestamps = new LinkedList<>();
		Map<String, Object> row = new HashMap<String, Object>();

		String tbname = "zj_schema_test1";
		row.put("card", "card1");
		row.put("mcc", "mcc1");
		row.put("money", rand.nextFloat() * 100F);
		rtidb.syncPutSchemaTable(tbname, ts, row);

		for (int i = 0; i < count; i++) {
			row.put("card", "card1");
			row.put("mcc", "mcc1");
			row.put("money", rand.nextFloat() * 100F);

			start = System.currentTimeMillis();
			rtidb.syncPutSchemaTable(tbname, ts + i, row);
			timestamps.add(System.currentTimeMillis() - start);
			row.clear();
		}

		long sum = 0L;
		System.out.println("put time:");
		for (long t : timestamps) {
			System.out.print(t + "ms,");
			sum += t;
		}
		System.out.println(String.format("put total time: %d(ms), avg time: %d(ms)", sum, sum / count));
	}

	public void schemaTablePutPerfTest02(int count) throws Exception {
		// TODO:
	}

}
