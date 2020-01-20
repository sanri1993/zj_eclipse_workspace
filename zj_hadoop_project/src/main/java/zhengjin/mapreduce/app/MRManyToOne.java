package zhengjin.mapreduce.app;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.log4j.Logger;

/**
 * 合并多个小文件
 */
public class MRManyToOne {

	private static final Logger logger = Logger.getLogger(MRManyToOne.class);

	/** Mapper */
	private static class FileMapper extends Mapper<NullWritable, BytesWritable, Text, BytesWritable> {

		private int myCounter = 0;

		@Override
		public void setup(Context context) throws IOException, InterruptedException {
			logger.info("FileMapper.setup() is invoked: " + this.hashCode());
		}

		@Override
		public void map(NullWritable key, BytesWritable value, Context context)
				throws IOException, InterruptedException {
			logger.info("FileMapper.map() is invoked: " + (++this.myCounter));
			String filename = ((FileSplit) context.getInputSplit()).getPath().getName();
			context.write(new Text(filename), value);
		}

		@Override
		public void cleanup(Context context) throws IOException, InterruptedException {
			logger.info("FileMapper.clearup() is invoked: " + this.hashCode());
		}
	}

	public static void main(String[] args) throws Exception {

		// input script:
		// for i in {1..10}; do echo "file$i for mapreduce ManyToOne test" > file$i.txt;
		// done

		// input on hdfs:
		// hdfs dfs -put src/file* ManyToOne/input
		// hdfs dfs -ls ManyToOne/input

		// hadoop jar zj-mvn-demo.jar com.zjmvn.hadoop.MRManyToOne \
		// ManyToOne/input ManyToOne/output

		// create map task for each input file
		logger.info("ManyToOne mapreduce is started.");

		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf);

		job.setJarByClass(MRManyToOne.class);
		job.setMapperClass(FileMapper.class);

		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(BytesWritable.class);

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(BytesWritable.class);

		job.setInputFormatClass(MyInputFormat.class); // custom input format
		job.setOutputFormatClass(SequenceFileOutputFormat.class);

		FileInputFormat.setInputPaths(job, new Path(args[0]));
		Path outDir = new Path(args[1]);
		FileOutputFormat.setOutputPath(job, outDir);

		FileSystem fs = FileSystem.get(conf);
		if (fs.exists(outDir)) {
			fs.delete(outDir, true);
		}

		if (!job.waitForCompletion(true)) {
			logger.info("ManyToOne mapreduce is failed.");
			System.exit(1);
		}
	}

}
