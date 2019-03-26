package com.zjmvn.hadoop;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.log4j.Logger;

public class WordCountMapReduce {

	private static final Logger logger = Logger.getLogger(WordCountMapReduce.class);

	private static class WordCountMapper extends Mapper<LongWritable, Text, Text, IntWritable> {

		@Override
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			String line = value.toString();
			String[] words = line.split(" ");
			for (String word : words) {
				context.write(new Text(word), new IntWritable(1));
			}
		}
	}

	private static class WordCountReducer extends Reducer<Text, IntWritable, Text, IntWritable> {

		@Override
		public void reduce(Text key, Iterable<IntWritable> values, Context context)
				throws IOException, InterruptedException {
			Integer count = 0;
			for (IntWritable value : values) {
				count += value.get();
			}
			context.write(key, new IntWritable(count));
		}
	}

	public static void main(String[] args) throws Exception {

		// input:
//		Text1：the weather is good
//		Text2：today is good
//		Text3：good weather is good
//		Text4：today has good weather

		// run cmd:
		// bin/hadoop jar src/zj-mvn-demo.jar com.zjmvn.hadoop.WordCountMapReduce wordcount/input wordcount/output

		// output:
//		good	5
//		has	1
//		is	3
//		the	1
//		today	2
//		weather	3

		logger.info("WordCount mapreduce is started.");

		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf, "wordcount");

		job.setJarByClass(WordCountMapReduce.class);
		job.setMapperClass(WordCountMapper.class);
		job.setReducerClass(WordCountReducer.class);

		// set map output key value
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(IntWritable.class);

		// set reduce output key value
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);

		for (String arg : args) {
			logger.info("argument: " + arg);
		}
		FileInputFormat.setInputPaths(job, new Path(args[1]));
		FileOutputFormat.setOutputPath(job, new Path(args[2]));

		if (!job.waitForCompletion(true)) {
			logger.info("WordCount mapreduce is failed.");
		}
	}

}
