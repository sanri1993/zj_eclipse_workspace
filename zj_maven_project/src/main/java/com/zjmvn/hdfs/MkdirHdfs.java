package com.zjmvn.hdfs;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;

public class MkdirHdfs {

	private static final Logger logger = Logger.getLogger(MkdirHdfs.class);

	public static void main(String[] args) throws Exception {

		final FileSystem fs = BaseHdfs.fs;

		String path = "/user/root/mkdir/a/b";
		boolean res = fs.mkdirs(new Path(path));
		logger.info(String.format("mkdir %s: %s", path, res));
	}

}
