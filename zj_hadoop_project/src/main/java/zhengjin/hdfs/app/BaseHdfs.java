package zhengjin.hdfs.app;

import java.io.IOException;
import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;

public class BaseHdfs {

	// method #1
	// run: java -jar src/zj-mvn-demo.jar com.zjmvn.hdfs.ListFilesHdfs
	// "IOException: No FileSystem for scheme: hdfs" fix:
	// config.set("fs.hdfs.impl", "org.apache.hadoop.hdfs.DistributedFileSystem");

	// method #2
	// run: bin/hadoop jar src/zj-mvn-demo.jar com.zjmvn.hdfs.ListFilesHdfs

	private static FileSystem fs;

	public static FileSystem getFileSystem() throws IOException, InterruptedException {
		if (fs == null) {
			String uri = "hdfs://3446e9827713:9000";
			Configuration config = new Configuration();
			config.set("fs.hdfs.impl", "org.apache.hadoop.hdfs.DistributedFileSystem");
			fs = FileSystem.get(URI.create(uri), config, "root");
		}
		return fs;
	}

	public static void fsClose() throws IOException {
		if (fs != null) {
			fs.close();
		}
	}

}
