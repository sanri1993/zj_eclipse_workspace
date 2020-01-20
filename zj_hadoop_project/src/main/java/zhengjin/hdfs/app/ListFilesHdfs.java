package zhengjin.hdfs.app;

import org.apache.hadoop.fs.BlockLocation;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.apache.log4j.Logger;

public class ListFilesHdfs {

	private static final Logger logger = Logger.getLogger(ListFilesHdfs.class);

	public static void main(String[] args) throws Exception {
		// run: bin/hadoop jar src/zj-mvn-demo.jar com.zjmvn.hdfs.ListFilesHdfs
		
		String path = "/user/root";

		for (String arg : args) {
			logger.info("argument: " + arg);
		}

		final FileSystem fs = BaseHdfs.getFileSystem();
		try {
			// #1
			FileStatus[] listStatus = fs.listStatus(new Path(path));
			logger.info("=====================================");
			for (FileStatus status : listStatus) {
				logger.info(String.format("[%s] %s", status.isFile() ? "file" : "dir", status.getPath().getName()));
			}

			// #2
			RemoteIterator<LocatedFileStatus> listFiles = fs.listFiles(new Path(path), true);
			while (listFiles.hasNext()) {
				logger.info("=====================================");
				LocatedFileStatus fileStatus = listFiles.next();
				logger.info("name: " + fileStatus.getPath().getName());
				logger.info("block size: " + fileStatus.getBlockSize());
				logger.info("replication: " + fileStatus.getReplication());
				logger.info("owner: " + fileStatus.getOwner());
				logger.info("permssion" + fileStatus.getPermission());

				logger.info("---------block info----------");
				BlockLocation[] blockLocations = fileStatus.getBlockLocations();
				for (BlockLocation block : blockLocations) {
					logger.info("block offset: " + block.getOffset());
					logger.info("block length: " + block.getLength());

					String[] datanodes = block.getHosts();
					for (String dn : datanodes) {
						logger.info("datanode: " + dn);
					}
				}
			}
		} finally {
			BaseHdfs.fsClose();
		}
	}

}
