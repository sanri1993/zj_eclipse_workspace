package zhengjin.rtidb.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com._4paradigm.rtidb.client.TableAsyncClient;
import com._4paradigm.rtidb.client.TableSyncClient;
import com._4paradigm.rtidb.client.ha.RTIDBClientConfig;
import com._4paradigm.rtidb.client.ha.impl.NameServerClientImpl;
import com._4paradigm.rtidb.client.ha.impl.RTIDBClusterClient;
import com._4paradigm.rtidb.client.impl.TableAsyncClientImpl;
import com._4paradigm.rtidb.client.impl.TableSyncClientImpl;

public final class RtidbClient {

	private static final String TAG = RtidbClient.class.getSimpleName() + " => ";
	private static final Logger LOG = LoggerFactory.getLogger(RtidbClient.class);

	private static boolean isReady = false;
	private static NameServerClientImpl nsc;
	private static RTIDBClusterClient clusterClient;
	private static TableSyncClient tableSyncClient;
	private static TableAsyncClient tableAsyncClient;

	static {
		try {
			LOG.info(TAG + "init ritdb");
			String leaderPath = RtidbEnv.zkRootPath + "/leader";
			// NameServerClientImpl要么做成单例, 要么⽤完之后就调⽤close, 否则会导致fd泄露
			nsc = new NameServerClientImpl(RtidbEnv.zkEndpoints, leaderPath);
			nsc.init();

			RTIDBClientConfig config = new RTIDBClientConfig();
			config.setZkEndpoints(RtidbEnv.zkEndpoints);
			config.setZkRootPath(RtidbEnv.zkRootPath);

			clusterClient = new RTIDBClusterClient(config);
			clusterClient.init();
			tableSyncClient = new TableSyncClientImpl(clusterClient);
			tableAsyncClient = new TableAsyncClientImpl(clusterClient);
			isReady = true;
		} catch (Exception e) {
			close();
			e.printStackTrace();
		}
	}

	public static void close() {
		if (clusterClient != null) {
			clusterClient.close();
		}
		if (nsc != null) {
			nsc.close();
		}
	}

	public static boolean isReady() {
		return isReady;
	}

	public static NameServerClientImpl getNameServerClient() {
		return nsc;
	}

	public static RTIDBClusterClient getClusterClient() {
		return clusterClient;
	}

	public static TableSyncClient getTableSyncClient() {
		return tableSyncClient;
	}

	public static TableAsyncClient getTableAsyncClient() {
		return tableAsyncClient;
	}

}
