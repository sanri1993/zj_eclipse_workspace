package zhengjin.hdfs.app;

import java.io.File;
import java.io.FileNotFoundException;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;

public class PutGetFileHdfs {

	private static final Logger logger = Logger.getLogger(PutGetFileHdfs.class);

	public static void main(String[] args) throws Exception {
		// run: bin/hadoop jar src/zj-mvn-demo.jar com.zjmvn.hdfs.PutGetFileHdfs
		
		String fileName = "zj-mvn-demo.jar";
		String localPath = "/usr/local/hadoop/src";
		String hdfsPath = "/user/root/mkdir";
		String localSavePath = "/tmp";

		String fromPath;
		String toPath;

		final FileSystem fs = BaseHdfs.getFileSystem();
		try {
			if (!fs.exists(new Path(hdfsPath))) {
				fs.mkdirs(new Path(hdfsPath));
			}
			fromPath = localPath + File.separator + fileName;
			toPath = hdfsPath;
			logger.info("put file to hdfs: " + hdfsPath);
			fs.copyFromLocalFile(new Path(fromPath), new Path(toPath));

			fromPath = hdfsPath + File.separator + fileName;
			toPath = localSavePath;
			if (!fs.exists(new Path(fromPath))) {
				throw new FileNotFoundException("src file not exist on hdfs: " + fromPath);
			}
			logger.info("get file from hdfs: " + localSavePath);
			fs.copyToLocalFile(new Path(fromPath), new Path(toPath));
		} finally {
			BaseHdfs.fsClose();
		}
	}

}
