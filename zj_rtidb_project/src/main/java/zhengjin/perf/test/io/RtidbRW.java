package zhengjin.perf.test.io;

import java.util.Map;
import java.util.concurrent.TimeoutException;

import com._4paradigm.rtidb.client.TabletException;

import zhengjin.rtidb.app.RtidbClient;

public final class RtidbRW implements DBReadWriter {

	@Override
	public boolean put(String tbName, Map<String, Object> row) throws TimeoutException, TabletException {
		return RtidbClient.getTableSyncClient().put(tbName, System.currentTimeMillis(), row);
	}

	@Override
	public Object[] get(String tbName, String key) throws TimeoutException, TabletException {
		return RtidbClient.getTableSyncClient().getRow(tbName, key, 0);
	}

}
