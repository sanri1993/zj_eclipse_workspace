package zhengjin.hdfs.app;

import java.io.FileNotFoundException;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;

public class StreamGetHdfs {

	private static final Logger logger = Logger.getLogger(StreamGetHdfs.class);

	public static void main(String[] args) throws Exception {
		// run: bin/hadoop jar src/zj-mvn-demo.jar com.zjmvn.hdfs.StreamGetHdfs

		String inPath = "/user/root/mkdir/file_in.txt";
		String outPath = "/user/root/mkdir/file_out.txt";

		final FileSystem fs = BaseHdfs.getFileSystem();
		FSDataInputStream in = null;
		FSDataOutputStream out = null;
		try {
			if (!fs.exists(new Path(inPath))) {
				throw new FileNotFoundException("input file not exist: " + inPath);
			}
			if (!fs.exists(new Path(outPath))) {
				if (!fs.createNewFile(new Path(outPath))) {
					throw new Exception("create output file failed: " + outPath);
				}
			}

			logger.info(String.format("read from %s, and write to %s", inPath, outPath));
			in = fs.open(new Path(inPath));
			in.seek(5); // 指定读取的开始位置
			out = fs.create(new Path(outPath));
			IOUtils.copy(in, out);
		} finally {
			if (out != null) {
				out.close();
			}
			if (in != null) {
				in.close();
			}
			if (fs != null) {
				BaseHdfs.fsClose();
			}
		}
	}

}
