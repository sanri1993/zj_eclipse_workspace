package com.zjmvn.hdfs;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;

public class DeleteFileHdfs {

	private static final Logger logger = Logger.getLogger(DeleteFileHdfs.class);

	public static void main(String[] args) throws Exception {

		String oldPath = "/user/root/mkdir/a";
		String newPath = "/user/root/mkdir/a2";
		String deletePath = "/user/root/mkdir/zj-mvn-demo.jar";

		FileSystem fs = BaseHdfs.getFileSystem();
		try {
			if (fs.exists(new Path(oldPath))) {
				logger.info("file rename to: " + newPath);
				fs.rename(new Path(oldPath), new Path(newPath));
			}

			if (fs.exists(new Path(deletePath))) {
				logger.info("delete file: " + deletePath);
				logger.info("file deleted: " + fs.delete(new Path(deletePath), true));
			}
		} finally {
			BaseHdfs.fsClose();
		}
	}

}
