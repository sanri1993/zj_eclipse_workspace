package zhengjin.hdfs.app;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;

public class MkdirHdfs {

	private static final Logger logger = Logger.getLogger(MkdirHdfs.class);

	public static void main(String[] args) throws Exception {
		// run: bin/hadoop jar src/zj-mvn-demo.jar com.zjmvn.hdfs.MkdirHdfs

		String path = "/user/root/mkdir/a/b";

		final FileSystem fs = BaseHdfs.getFileSystem();
		try {
			if (!fs.exists(new Path(path))) {
				boolean res = fs.mkdirs(new Path(path));
				logger.info(String.format("mkdir %s: %s", path, res));
			}
		} finally {
			BaseHdfs.fsClose();
		}
	}

}
