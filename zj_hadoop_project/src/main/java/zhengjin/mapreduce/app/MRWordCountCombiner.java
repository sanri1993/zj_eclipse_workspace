package zhengjin.mapreduce.app;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
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

public class MRWordCountCombiner {

	private static final Logger logger = Logger.getLogger(MRWordCountCombiner.class);

	/** Combiner */
	private static class WordCountCombiner extends Reducer<Text, IntWritable, Text, IntWritable> {

		@Override
		protected void reduce(Text key, java.lang.Iterable<IntWritable> values,
				Reducer<Text, IntWritable, Text, IntWritable>.Context context)
				throws java.io.IOException, InterruptedException {
			logger.info(String.format("WordCountCombiner input <%s,N(N>=1)>", key));

			int count = 0;
			for (IntWritable value : values) {
				count += value.get();
				logger.info(String.format("WordCountCombiner input kv <%s,%d>", key.toString(), value.get()));
			}
			context.write(key, new IntWritable(count));
			logger.info(String.format("WordCountCombiner output kv <%s,%d>", key.toString(), count));
		}
	}

	/** Mapper */
	private static class WordCountMapper extends Mapper<LongWritable, Text, Text, IntWritable> {

		@Override
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			String line = value.toString();
			String[] words = line.split(" ");
			for (String word : words) {
				context.write(new Text(word), new IntWritable(1));
				logger.info(String.format("WordCountMapper output <%s,1>", word));
			}
		}
	}

	/** Reducer */
	private static class WordCountReducer extends Reducer<Text, IntWritable, Text, IntWritable> {

		@Override
		public void reduce(Text key, Iterable<IntWritable> values, Context context)
				throws IOException, InterruptedException {
			logger.info(String.format("WordCountReducer input <%s,N(N>=1)>", key));

			int count = 0;
			for (IntWritable value : values) {
				count += value.get();
				logger.info(String.format("WordCountReducer input kv <%s,%d>", key.toString(), value.get()));
			}
			context.write(key, new IntWritable(count));
		}
	}

	public static void main(String[] args) throws Exception {

		// input of 4 files:
		// good weather is good
		// good today is good
		// good weather is good
		// good today has good weather good

		// run mapreduce (4 mapper tasks, and each for a input file):
		// hadoop jar zj-mvn-demo.jar com.zjmvn.hadoop.MRWordCountCombiner \
		// wordcount/input wordcount/output true

		// output (hdfs dfs -cat /user/root/wordcount/output/*):
		// WordCountReducer input <good,N(N>=1)>
		// WordCountReducer input kv <good,2>
		// WordCountReducer input kv <good,2>
		// WordCountReducer input kv <good,3>
		// WordCountReducer input kv <good,2>

		// good 9
		// has 1
		// is 3
		// today 2
		// weather 3

		logger.info("WordCount mapreduce is started.");

		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf, "wordcount");

		job.setJarByClass(MRWordCountCombiner.class);
		job.setMapperClass(WordCountMapper.class);
		job.setReducerClass(WordCountReducer.class);

		if (Boolean.parseBoolean(args[2])) {
			job.setCombinerClass(WordCountCombiner.class); // map side Combiner
		}

		// set map output key value
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(IntWritable.class);
		// set reduce output key value
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);

		for (String arg : args) {
			logger.info("argument: " + arg);
		}
		FileInputFormat.setInputPaths(job, new Path(args[0]));
		Path outDir = new Path(args[1]);
		FileOutputFormat.setOutputPath(job, outDir);

		FileSystem fs = FileSystem.get(conf);
		if (fs.exists(outDir)) {
			fs.delete(outDir, true);
		}

		if (!job.waitForCompletion(true)) {
			logger.warn("WordCount mapreduce is failed.");
			System.exit(1);
		}
	}

}
