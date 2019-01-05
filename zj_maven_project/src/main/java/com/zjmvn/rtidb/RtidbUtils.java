package com.zjmvn.rtidb;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

import com._4paradigm.rtidb.client.GetFuture;
import com._4paradigm.rtidb.client.PutFuture;
import com._4paradigm.rtidb.client.TableAsyncClient;
import com._4paradigm.rtidb.client.TableSyncClient;
import com._4paradigm.rtidb.client.TabletException;
import com._4paradigm.rtidb.client.ha.impl.NameServerClientImpl;
import com._4paradigm.rtidb.client.ha.impl.RTIDBClusterClient;
import com._4paradigm.rtidb.ns.NS;
import com.google.protobuf.ByteString;

public class RtidbUtils {

	private static final String TAG = RtidbUtils.class.getSimpleName() + " => ";
	private static final Logger logger = Logger.getLogger(RtidbUtils.class);
	private static final RtidbUtils INSTANCE = new RtidbUtils();

	private NameServerClientImpl nsc = null;
	private RTIDBClusterClient clusterClient = null;
	private TableSyncClient tableSyncClient = null;
	private TableAsyncClient tableAsyncClient = null;

	private RtidbUtils() {
	}

	public static RtidbUtils getInstance() {
		return INSTANCE;
	}

	public RtidbUtils build(RtidbClient client) {
		this.nsc = client.getNameServerClient();
		this.clusterClient = client.getClusterClient();
		this.tableSyncClient = client.getTableSyncClient();
		this.tableAsyncClient = client.getTableAsyncClient();
		return this;
	}

	public boolean createKVTable(String tname) {
		NS.TableInfo.Builder builder = NS.TableInfo.newBuilder();
		builder = NS.TableInfo.newBuilder().setName(tname)
//				.setReplicaNum(3)
//				.setPartitionNum(8)
//				.setCompressType(NS.CompressType.kSnappy)
//				.setTtlType("kLatestTime")
				.setTtl(144000);
		NS.TableInfo table = builder.build();
		boolean ok = nsc.createTable(table);

		if (clusterClient != null) {
			clusterClient.refreshRouteTable();
		}
		return ok;
	}

	public boolean showKVTables(String tname) {
		List<NS.TableInfo> tlist = nsc.showTable(tname);
		if (tlist.size() == 0) {
			logger.info("table not found: " + tname);
			return false;
		}

		for (NS.TableInfo t : tlist) {
			logger.info(TAG + "table: " + t.getName());
		}
		return true;
	}

	public boolean syncPutKVTable(String tname, String key, String value) throws TimeoutException, TabletException {
		long ts = System.currentTimeMillis();
		return tableSyncClient.put(tname, key, ts, value);
	}

	public String syncGetKVTable(String tname, String key) throws TimeoutException, TabletException {
		ByteString bs = tableSyncClient.get(tname, key, 0L);
		return new String(bs.toByteArray());
	}

	public boolean createSchemaTable(String tname, Map<String, String> schema) {
		List<NS.ColumnDesc> cols = new ArrayList<>(schema.size() * 2);
		for (Entry<String, String> field : schema.entrySet()) {
			cols.add(NS.ColumnDesc.newBuilder().setName(field.getKey()).setAddTsIdx(true).setType(field.getValue())
					.build());
		}

		NS.TableInfo.Builder builder = NS.TableInfo.newBuilder();
		builder = NS.TableInfo.newBuilder().setName(tname)
				// .setReplicaNum(3)
				// .setPartitionNum(8)
				// .setCompressType(NS.CompressType.kSnappy)
				// .setTtlType("kLatestTime")
				.setTtl(144000);
		for (NS.ColumnDesc col : cols) {
			builder.addColumnDesc(col);
		}
		NS.TableInfo table = builder.build();
		boolean ok = nsc.createTable(table);

		if (clusterClient != null) {
			clusterClient.refreshRouteTable();
		}
		return ok;
	}

	public boolean showSchemaTables(String tname) {
		List<NS.TableInfo> tlist = nsc.showTable(tname);
		if (tlist.size() == 0) {
			logger.info("table not found: " + tname);
			return false;
		}

		for (NS.TableInfo table : tlist) {
			logger.info(TAG + "table: " + table.getName());
			for (NS.ColumnDesc col : table.getColumnDescList()) {
				logger.info(TAG + String.format("column %s, type: %s", col.getName(), col.getType()));
			}
		}
		return true;
	}

	public boolean syncPutSchemaTable(String tname, Map<String, Object> row) throws TimeoutException, TabletException {
		long ts = System.currentTimeMillis();
		return tableSyncClient.put(tname, ts, row);
	}

	public Object[] syncGetSchemaTable(String tname, String key, String value)
			throws TimeoutException, TabletException {
		return tableSyncClient.getRow(tname, value, key, 0L);
	}

	public PutFuture asyncPutSchemaTable(String tname, Map<String, Object> row)
			throws TabletException, InterruptedException, ExecutionException {
		long ts = System.currentTimeMillis();
		return tableAsyncClient.put(tname, ts, row);

	}

	public GetFuture asyncGetSchemaTable(String tname, String key, String value)
			throws TabletException, InterruptedException, ExecutionException {
		return tableAsyncClient.get(tname, value, key, 0L);
	}

}
