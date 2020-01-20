package zhengjin.mapreduce.app;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.log4j.Logger;

public class MyRecordReader extends RecordReader<NullWritable, BytesWritable> {

	private static final Logger logger = Logger.getLogger(MRManyToOne.class);

	private Configuration conf;
	private FileSplit fileSplit;
	private BytesWritable value = new BytesWritable();
	private boolean processed = false;

	@Override
	public void initialize(InputSplit split, TaskAttemptContext context) throws IOException, InterruptedException {
		this.fileSplit = (FileSplit) split;
		this.conf = context.getConfiguration();
	}

	@Override
	public boolean nextKeyValue() throws IOException, InterruptedException {
		if (!this.processed) {
			// MyInputFormat.isSplitable=false, handle for each input file here
			byte[] contents = new byte[(int) this.fileSplit.getLength()];
			Path file = this.fileSplit.getPath();
			FileSystem fs = file.getFileSystem(this.conf);

			FSDataInputStream in = null;
			try {
				in = fs.open(file);
				IOUtils.readFully(in, contents, 0, contents.length);
				this.value.set(contents, 0, contents.length);
				logger.info("MyRecordReader for file: " + file.toString());
				logger.info("MyRecordReader, file text: " + new String(contents));
			} finally {
				if (in != null) {
					IOUtils.closeStream(in);
				}
			}
			processed = true;
			return true;
		}
		return false;
	}

	@Override
	public NullWritable getCurrentKey() throws IOException, InterruptedException {
		return NullWritable.get();
	}

	@Override
	public BytesWritable getCurrentValue() throws IOException, InterruptedException {
		return this.value;
	}

	@Override
	public float getProgress() throws IOException, InterruptedException {
		return this.processed ? 1.0f : 0.0f;
	}

	@Override
	public void close() throws IOException {
	}

}
