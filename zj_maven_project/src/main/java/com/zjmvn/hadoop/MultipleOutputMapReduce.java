package com.zjmvn.hadoop;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;
import org.apache.log4j.Logger;

public class MultipleOutputMapReduce {

	private static final Logger logger = Logger.getLogger(MultipleOutputMapReduce.class);

	private static class MultipleOutputMapper extends Mapper<LongWritable, Text, Text, Text> {

		@Override
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			String line = value.toString();
			String[] fields = line.split(",");
			context.write(new Text(fields[0]), value);
		}
	}

	private static class MultipleOutputReducer extends Reducer<Text, Text, NullWritable, Text> {

		private MultipleOutputs<NullWritable, Text> multipleOutputs;

		@Override
		public void setup(Context context) throws IOException, InterruptedException {
			multipleOutputs = new MultipleOutputs<NullWritable, Text>(context);
		}

		@Override
		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
			for (Text value : values) {
				this.multipleOutputs.write(NullWritable.get(), value, key.toString());
			}
		}

		@Override
		public void cleanup(Context context) throws IOException, InterruptedException {
			if (this.multipleOutputs != null) {
				this.multipleOutputs.close();
			}
		}
	}

	public static void main(String[] args) throws Exception {

		// input:
//		Order_000004,Pdt_06,102.8
//		Order_000001,Pdt_01,222.8
//		Order_000002,Pdt_03,522.8
//		Order_000003,Pdt_01,282.8
//		Order_000002,Pdt_04,122.4
//		Order_000001,Pdt_05,25.8
//		Order_000003,Pdt_01,322.1
//		Order_000004,Pdt_07,716.2

		// run cmd:
		// bin/hadoop jar src/zj-mvn-demo.jar com.zjmvn.hadoop.MultipleOutput muloutput/input muloutput/output

		// output:
//		-rw-r--r--   1 root supergroup         51 2019-03-26 04:05 muloutput/output/Order_000001-r-00000
//		-rw-r--r--   1 root supergroup         52 2019-03-26 04:05 muloutput/output/Order_000002-r-00000
//		-rw-r--r--   1 root supergroup         52 2019-03-26 04:05 muloutput/output/Order_000003-r-00000
//		-rw-r--r--   1 root supergroup         52 2019-03-26 04:05 muloutput/output/Order_000004-r-00000

		logger.info("MultipleOutput mapreduce is started.");

		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf);

		job.setJarByClass(MultipleOutputMapReduce.class);
		job.setMapperClass(MultipleOutputMapper.class);
		job.setReducerClass(MultipleOutputReducer.class);

		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);

		job.setOutputKeyClass(NullWritable.class);
		job.setOutputValueClass(Text.class);

		FileInputFormat.setInputPaths(job, new Path(args[1]));
		FileOutputFormat.setOutputPath(job, new Path(args[2]));

		if (!job.waitForCompletion(true)) {
			logger.info("MultipleOutput mapreduce is failed.");
		}
	}

}
