package zhengjin.rtidb.app;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RtidbDemo {

	private static final Logger LOG = LoggerFactory.getLogger(RtidbDemo.class);
	private static final RtidbUtils rtidb = RtidbUtils.getInstance();

	/**
	 * Verify rtidb put action.
	 * 
	 * @param count
	 * @throws Exception
	 */
	public void schemaTablePutPerfTest01(int count) throws Exception {
		long ts = System.currentTimeMillis();
		long start;
		Random rand = new Random();
		List<Long> timestamps = new LinkedList<>();
		Map<String, Object> row = new HashMap<String, Object>();

		final String tbname = "zj_schema_test1";
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
		LOG.info("Put Times:");
		for (long t : timestamps) {
			System.out.print(t + "ms,");
			sum += t;
		}
		LOG.info(String.format("Put total time: %d(ms), Avg time: %d(ms)", sum, sum / count));
	}

	/**
	 * Verify rtidb put action profile.
	 * 
	 * @param count
	 * @throws Exception
	 */
	public void schemaTablePutPerfTest02(int count) throws Exception {
		long ts = System.currentTimeMillis();
		Random rand = new Random();
		Map<String, Object> row = new HashMap<String, Object>();

		final String tbname = RtidbEnv.tbName;
		row.put("user_code", "00011");
		row.put("sell_codes", String.valueOf("00011".hashCode()));
		row.put("vals", String.valueOf(String.valueOf(ts).hashCode()));
		row.put("ranks", String.valueOf(rand.nextInt(6)));
		row.put("sub_categories", "sub_categories_1");
		row.put("biz_date", "2020-02-06");
		row.put("insert_time", ts);
		row.put("p_biz_date", (int) ts);
		rtidb.syncPutSchemaTable(tbname, ts, row);

		long start;
		String userCode;
		String subCategory;
		List<Long> timestamps = new LinkedList<>();
		for (int i = 0; i < count; i++) {
			ts += i;
			userCode = "000" + String.valueOf(rand.nextInt(1000));
			subCategory = "sub_categories_" + String.valueOf(rand.nextInt(30));

			row.put("user_code", userCode);
			row.put("sell_codes", "010" + String.valueOf(rand.nextInt(10000)));
			row.put("vals", String.valueOf(userCode.hashCode()));
			row.put("ranks", String.valueOf(rand.nextInt(6)));
			row.put("sub_categories", subCategory);
			row.put("biz_date", "2020-02-06");
			row.put("insert_time", ts);
			row.put("p_biz_date", (int) ts);

			start = System.currentTimeMillis();
			rtidb.syncPutSchemaTable(tbname, ts, row);
			timestamps.add(System.currentTimeMillis() - start);
			row.clear();
		}

		long sum = 0L;
		StringBuilder sb = new StringBuilder(count * 2);
		for (long t : timestamps) {
			sb.append(t + "ms,");
			sum += t;
		}
		LOG.info("Put Times: {}", sb.toString());

		Collections.sort(timestamps);
		float avg = sum / count;
		long line90 = timestamps.get((int) ((int) (timestamps.size() * 0.9) - 1));
		long line95 = timestamps.get((int) ((int) (timestamps.size() * 0.95) - 1));
		long line99 = timestamps.get((int) ((int) (timestamps.size() * 0.99) - 1));

		LOG.info(String.format("Put samplers: %d, Avg: %.2f(ms), Min: %d(ms), Max: %d(ms)", count, avg,
				timestamps.get(0), timestamps.get(timestamps.size() - 1)));
		LOG.info("Line90: {}(ms), Line95: {}(ms), Line99: {}(ms)", line90, line95, line99);
	}

	/**
	 * Insert count of records with same key.
	 * 
	 * @throws Exception
	 */
	public void schemaTablePutDataTest03() throws Exception {
		final String tbname = RtidbEnv.tbName;
		final int count = 100;

		Random rand = new Random();
		Map<String, Object> row = new HashMap<String, Object>();

		long ts = System.currentTimeMillis();
		String userCode = String.valueOf(ts + rand.nextInt(10000));
		LOG.info("put {} of records in to {} with key {}.", count, tbname, userCode);
		for (int i = 0; i < count; i++, ts++) {
			row.put("u_user_code", userCode);
			row.put("p_biz_date", (int) ts - rand.nextInt(1000));
			row.put("insert_time", new Timestamp(ts));
			rtidb.syncPutSchemaTable(tbname, ts, row);
			row.clear();
		}
	}

}
