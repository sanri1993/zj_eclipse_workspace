package zhengjin.rtidb.app;

public final class RtidbConstants {

	// rtidb configs
	// ./bin/rtidb
	// --zk_cluster=172.27.128.33:5181,172.27.128.32:5181,172.27.128.31:5181
	// --zk_root_path=/rtidb_cluster --role=ns_client
	public static final String zkEndpoints = "172.27.128.33:5181,172.27.128.32:5181,172.27.128.31:5181";
	public static final String zkRootPath = "/rtidb_cluster";

}
