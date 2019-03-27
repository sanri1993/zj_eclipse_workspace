package com.zjmvn.hdfs;

import java.io.IOException;
import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;

public class BaseHdfs {

	// #1
	// run cmd: java -jar src/zj-mvn-demo.jar
	// fix IOException: No FileSystem for scheme: hdfs
	// config.set("fs.hdfs.impl", "org.apache.hadoop.hdfs.DistributedFileSystem");

	// #2
	// run cmd:
	// bin/hadoop jar src/zj-mvn-demo.jar com.zjmvn.hadoop.MkdirHdfs

	public static FileSystem fs;

	static {
		String uri = "hdfs://3446e9827713:9000";
		Configuration config = new Configuration();
		config.set("fs.hdfs.impl", "org.apache.hadoop.hdfs.DistributedFileSystem");
		try {
			fs = FileSystem.get(URI.create(uri), config, "root");
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
}
