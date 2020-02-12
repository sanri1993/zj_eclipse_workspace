package zhengjin.rtidb.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com._4paradigm.rtidb.client.KvIterator;
import com._4paradigm.rtidb.client.ScanOption;
import com._4paradigm.rtidb.client.TabletException;
import com._4paradigm.rtidb.common.Common;
import com._4paradigm.rtidb.common.Common.ColumnDesc;
import com._4paradigm.rtidb.common.Common.ColumnKey;
import com._4paradigm.rtidb.ns.NS;
import com._4paradigm.rtidb.tablet.Tablet;
import com.google.protobuf.ByteString;

public final class RtidbUtils {

	private static final String TAG = RtidbUtils.class.getSimpleName() + " => ";
	private static final Logger LOG = LoggerFactory.getLogger(RtidbUtils.class);
	private static final RtidbUtils INSTANCE = new RtidbUtils();

	private RtidbUtils() {
	}

	public static RtidbUtils getInstance() {
		return INSTANCE;
	}

	public boolean createKVTable(String tname) {
		LOG.info(TAG + "create kv table");
		NS.TableInfo.Builder builder = NS.TableInfo.newBuilder();

		Tablet.TTLDesc ttlDesc = Tablet.TTLDesc.newBuilder()
				// 设置ttl类型, 共有四种 kAbsoluteTime, kLatestTime, kAbsAndLat, kAbsOrLat
				// 如不设置此域默认为kAbsoluteTime
				.setTtlType(Tablet.TTLType.kAbsoluteTime)
				// 设置absolute ttl的值, 单位为分钟, 不设置默认为0, 即不限制
				.setAbsTtl(30)
				// 设置latest ttl的值, 单位为条, 不设置默认为0, 即不限制
				.setLatTtl(0).build();

		builder = NS.TableInfo.newBuilder().setName(tname) // 设置表名
				// 设置副本数, 此设置是可选的, 默认为3
				.setReplicaNum(1)
				// 设置分⽚数, 此设置是可选的, 默认为16
				.setPartitionNum(1)
				// 设置数据压缩类型, 此设置是可选的默认为不压缩
//				.setCompressType(NS.CompressType.kSnappy)
				// 设置表的存储模式, 默认为kMemory. 还可以设置为Common.StorageMode.kSSD和Common.StorageMode.kHDD
				.setStorageMode(Common.StorageMode.kMemory)
				// 设置ttl
				.setTtlDesc(ttlDesc);

		NS.TableInfo table = builder.build();
		boolean ok = RtidbClient.getNameServerClient().createTable(table);
		RtidbClient.getClusterClient().refreshRouteTable();
		return ok;
	}

	public boolean createSchemaTable01(String name) {
		LOG.info(TAG + "create schema table");
		NS.TableInfo.Builder builder = NS.TableInfo.newBuilder();
		Tablet.TTLDesc ttlDesc = Tablet.TTLDesc.newBuilder().setAbsTtl(30).setLatTtl(1).build();

		builder = NS.TableInfo.newBuilder().setName(name).setReplicaNum(1).setPartitionNum(1)
				// .setCompressType(NS.CompressType.kSnappy)
				.setStorageMode(Common.StorageMode.kMemory).setTtlDesc(ttlDesc);

		// 设置schema信息
		ColumnDesc col0 = ColumnDesc.newBuilder().setName("card").setAddTsIdx(true).setType("string")
				// .setIsTsCol(true)
				.build();
		ColumnDesc col1 = ColumnDesc.newBuilder().setName("mcc").setAddTsIdx(true).setType("string").build();
		ColumnDesc col2 = ColumnDesc.newBuilder().setName("money").setAddTsIdx(false).setType("float").build();
		builder.addColumnDescV1(col0).addColumnDescV1(col1).addColumnDescV1(col2);

		NS.TableInfo table = builder.build();
		boolean ok = RtidbClient.getNameServerClient().createTable(table);
		RtidbClient.getClusterClient().refreshRouteTable();
		return ok;
	}

	public boolean createSchemaTable02(String name) {
		LOG.info(TAG + "创建带有组合key的schema表");
		NS.TableInfo.Builder builder = NS.TableInfo.newBuilder();
		Tablet.TTLDesc ttlDesc = Tablet.TTLDesc.newBuilder().setTtlType(Tablet.TTLType.kAbsOrLat).setAbsTtl(30)
				.setLatTtl(1).build();

		builder = NS.TableInfo.newBuilder().setName(name).setReplicaNum(1).setPartitionNum(1)
				// .setCompressType(NS.CompressType.kSnappy)
				.setTtlDesc(ttlDesc);

		ColumnDesc col0 = ColumnDesc.newBuilder().setName("card").setAddTsIdx(false).setType("string").build();
		ColumnDesc col1 = ColumnDesc.newBuilder().setName("mcc").setAddTsIdx(false).setType("string").build();
		ColumnDesc col2 = ColumnDesc.newBuilder().setName("amt").setAddTsIdx(false).setType("double").build();
		ColumnDesc col3 = ColumnDesc.newBuilder().setName("ts").setAddTsIdx(false).setType("int64").setIsTsCol(true)
				.build();
		ColumnKey colKey1 = ColumnKey.newBuilder().setIndexName("card_mcc").addColName("card").addColName("mcc")
				.addTsName("ts").build();
		builder.addColumnDescV1(col0).addColumnDescV1(col1).addColumnDescV1(col2).addColumnDescV1(col3)
				.addColumnKey(colKey1);

		NS.TableInfo table = builder.build();
		boolean ok = RtidbClient.getNameServerClient().createTable(table);
		RtidbClient.getClusterClient().refreshRouteTable();
		return ok;
	}

	/**
	 * kv表 同步put, scan, get
	 */
	public boolean syncPutKVTable(String name, String key, long ts, String value)
			throws TimeoutException, TabletException {
		LOG.debug(TAG + "kv table, sync put record");
		return RtidbClient.getTableSyncClient().put(name, key, ts, value);
	}

	public String syncGetKVTable(String name, String key) throws TimeoutException, TabletException {
		LOG.debug(TAG + "kv table, sync get record");
		ByteString bs = RtidbClient.getTableSyncClient().get(name, key);
		if (bs != null) {
			return new String(bs.toByteArray());
		}
		return "";
	}

	public String syncGetKVTable(String name, String key, long ts) throws TimeoutException, TabletException {
		LOG.info(TAG + "kv table, sync get record by ts");
		// get数据, 查询指定ts的值. 如果ts设置为0, 返回最新插⼊的⼀条数据
		ByteString bs = RtidbClient.getTableSyncClient().get(name, key, ts);
		if (bs != null) {
			return new String(bs.toByteArray());
		}
		return "";
	}

	public List<String> syncScanKVTable(String name, String key, long st, long et)
			throws TimeoutException, TabletException {
		LOG.info(TAG + "kv table, sync scan records");
		// scan数据, 查询范围需要传⼊st和et分别表示起始时间和结束时间, 其中起始时间⼤于结束时间
		// 如果结束时间et设置为0, 返回起始时间之前的所有数据
		List<String> list = new ArrayList<>();
		KvIterator it = RtidbClient.getTableSyncClient().scan(name, key, st, et);
		while (it.valid()) {
			byte[] buffer = new byte[it.getValue().remaining()];
			it.getValue().get(buffer);
			list.add(new String(buffer));
			it.next();
		}
		return list;
	}

	public List<String> syncScanKVTable(String name, String key, long ts, int limit, int atleast)
			throws TimeoutException, TabletException {
		LOG.info(TAG + "kv table, sync scan records by options");
		// 可以通过limit限制最多返回的条数, 如果不设置或设置为0, 则不限制
		// 可以通过atleast忽略et的限制, 限制⾄少返回的条数. 如果不设置或设置为0, 则不限制
		// 如果st和et都设置为0则返回最近N条记录
		ScanOption option = new ScanOption();
		option.setLimit(limit);
		option.setAtLeast(atleast);

		List<String> list = new ArrayList<>();
		KvIterator it = RtidbClient.getTableSyncClient().scan(name, key, ts, 0, option);
		while (it.valid()) {
			byte[] buffer = new byte[it.getValue().remaining()];
			it.getValue().get(buffer);
			list.add(new String(buffer));
			it.next();
		}
		return list;
	}

	/**
	 * schema表 同步put, scan, get
	 */
	public boolean syncPutSchemaTable(String name, long ts, Object[] row) throws TimeoutException, TabletException {
		LOG.debug(TAG + "schema table, sync put record");
		return RtidbClient.getTableSyncClient().put(name, ts, row);
	}

	public boolean syncPutSchemaTable(String name, long ts, Map<String, Object> row)
			throws TimeoutException, TabletException {
		LOG.debug(TAG + "schema table, sync put records");
		return RtidbClient.getTableSyncClient().put(name, ts, row);
	}

	public Object[] syncGetSchemaTable(String name, long ts, String key, String idx)
			throws TimeoutException, TabletException {
		LOG.debug(TAG + "schema table, sync get record");
		// key是需要查询字段的值, idxName是需要查询的字段名
		// 查询指定ts的值. 如果ts设置为0, 返回最新插⼊的⼀条数据
		return RtidbClient.getTableSyncClient().getRow(name, key, idx, ts);
	}

	public List<Object[]> syncScanSchemaTable(String name, long st, long et, String key, String idx)
			throws TimeoutException, TabletException {
		LOG.info(TAG + "schema table, sync scan records");
		// key是需要查询字段的值, idxName是需要查询的字段名
		// 查询范围需要传⼊st和et分别表示起始时间和结束时间, 其中起始时间⼤于结束时间
		// 如果结束时间et设置为0, 返回起始时间之前的所有数据
		List<Object[]> list = new ArrayList<>();
		KvIterator it = RtidbClient.getTableSyncClient().scan(name, key, idx, st, et);
		while (it.valid()) {
			list.add(it.getDecodedValue());
			it.next();
		}
		return list;
	}

	public List<Object[]> syncScanSchemaTable(String name, long st, long et, String key, String idx, int limit,
			int atleast) throws TimeoutException, TabletException {
		LOG.info(TAG + "schema table, sync scan records with options");
		// 可以通过limit限制最多返回的条数. 如果不设置或设置为0, 则不限制
		// 可以通过atleast忽略et的限制, 限制⾄少返回的条数. 如果不设置或设置为0, 则不限制
		ScanOption option = new ScanOption();
		option.setLimit(limit);
		option.setAtLeast(atleast);
		option.setIdxName(idx);

		List<Object[]> list = new ArrayList<>();
		KvIterator it = RtidbClient.getTableSyncClient().scan(name, key, st, et, option);
		while (it.valid()) {
			list.add(it.getDecodedValue());
			it.next();
		}
		return list;
	}

}
