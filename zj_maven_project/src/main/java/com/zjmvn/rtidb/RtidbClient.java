package com.zjmvn.rtidb;

import org.apache.log4j.Logger;

import com._4paradigm.rtidb.client.TableAsyncClient;
import com._4paradigm.rtidb.client.TableSyncClient;
import com._4paradigm.rtidb.client.ha.RTIDBClientConfig;
import com._4paradigm.rtidb.client.ha.impl.NameServerClientImpl;
import com._4paradigm.rtidb.client.ha.impl.RTIDBClusterClient;
import com._4paradigm.rtidb.client.impl.TableAsyncClientImpl;
import com._4paradigm.rtidb.client.impl.TableSyncClientImpl;

public class RtidbClient {

	private final static String TAG = RtidbClient.class.getSimpleName() + " => ";
	private static final Logger logger = Logger.getLogger(RtidbClient.class);
	private static final RtidbClient INSTANCE = new RtidbClient();

	private NameServerClientImpl nsc = null;
	private RTIDBClusterClient clusterClient = null;
	private TableSyncClient tableSyncClient = null;
	private TableAsyncClient tableAsyncClient = null;

	private RtidbClient() {
	}

	public static RtidbClient GetInstance() throws Exception {
		return INSTANCE;
	}

	public RtidbClient build(String zkEndpoints, String zkRootPath) throws Exception {
		logger.info(TAG + "build rtidb client instance.");
		String leaderPath = zkRootPath + "/leader";

		nsc = new NameServerClientImpl(zkEndpoints, leaderPath);
		nsc.init();

		RTIDBClientConfig config = new RTIDBClientConfig();
		config.setZkEndpoints(zkEndpoints);
		config.setZkRootPath(zkRootPath);
		clusterClient = new RTIDBClusterClient(config);
		clusterClient.init();

		tableSyncClient = new TableSyncClientImpl(clusterClient);
		tableAsyncClient = new TableAsyncClientImpl(clusterClient);
		return this;
	}

	public NameServerClientImpl getNameServerClient() {
		return this.nsc;
	}

	public RTIDBClusterClient getClusterClient() {
		return this.clusterClient;
	}

	public TableSyncClient getTableSyncClient() {
		return this.tableSyncClient;
	}

	public TableAsyncClient getTableAsyncClient() {
		return this.tableAsyncClient;
	}

	public void close() {
		if (this.clusterClient != null) {
			this.clusterClient.close();
		}
		if (this.nsc != null) {
			this.nsc.close();
		}
	}

}
