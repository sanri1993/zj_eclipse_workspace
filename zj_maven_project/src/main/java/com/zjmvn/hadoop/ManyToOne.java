package com.zjmvn.hadoop;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
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
public class ManyToOne {

	private static final Logger logger = Logger.getLogger(ManyToOne.class);

	private static class FileMapper extends Mapper<NullWritable, BytesWritable, Text, BytesWritable> {

		private Text filenamekey;

		@Override
		public void setup(Context context) throws IOException, InterruptedException {
			InputSplit split = context.getInputSplit();
			String filename = ((FileSplit) split).getPath().getName();
			this.filenamekey = new Text(filename);
		}

		@Override
		public void map(NullWritable key, BytesWritable value, Context context)
				throws IOException, InterruptedException {
			context.write(this.filenamekey, value);
		}
	}

	public static void main(String[] args) throws Exception {

		// input, create files, cmd:
		// for i in {1..10}; do echo "file$i for mapreduce ManyToOne test" > file$i.txt; done

		// input on hdfs:
		// bin/hdfs dfs -put src/file* ManyToOne/input
		// bin/hdfs dfs -ls ManyToOne/input

		// run cmd:
		// bin/hadoop jar src/zj-mvn-demo.jar com.zjmvn.hadoop.ManyToOne ManyToOne/input ManyToOne/output

		logger.info("ManyToOne mapreduce is started.");

		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf);

		job.setJarByClass(ManyToOne.class);
		job.setMapperClass(FileMapper.class);

		job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(BytesWritable.class);
        
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(BytesWritable.class);

		job.setInputFormatClass(MyInputFormat.class);
		job.setOutputFormatClass(SequenceFileOutputFormat.class);

		FileInputFormat.setInputPaths(job, new Path(args[1]));
		FileOutputFormat.setOutputPath(job, new Path(args[2]));

		if (!job.waitForCompletion(true)) {
			logger.info("ManyToOne mapreduce is failed.");
		}
	}

}
