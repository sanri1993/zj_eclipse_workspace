package com.zjmvn.hadoop;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.log4j.Logger;

public class StepFirstMapReduce {

	private static final Logger logger = Logger.getLogger(StepFirstMapReduce.class);

	private static class FirstMapper extends Mapper<LongWritable, Text, Text, Text> {

		@Override
		protected void map(LongWritable key, Text value, Mapper<LongWritable, Text, Text, Text>.Context context)
				throws IOException, InterruptedException {
			String line = value.toString();
			String user_friends[] = line.split(":");
			String user = user_friends[0];
			String friends = user_friends[1];

			for (String friend : friends.split(",")) {
				context.write(new Text(friend), new Text(user));
			}
		}
	}

	private static class FirstReducer extends Reducer<Text, Text, Text, Text> {

		@Override
		protected void reduce(Text friend, Iterable<Text> users, Context context)
				throws IOException, InterruptedException {
			StringBuffer buf = new StringBuffer();
			for (Text user : users) {
				buf.append(user).append(",");
			}

			context.write(friend, new Text(buf.toString()));
		}
	}

	public static void main(String[] args) throws Exception {

		// input:
		// A:B,C,D,F,E,O
		// B:A,C,E,K
		// C:F,A,D,I
		// D:A,E,F,L
		// E:B,C,D,M,L
		// F:A,B,C,D,E,O,M
		// G:A,C,D,E,F
		// H:A,C,D,E,O

		// run cmd:
		// bin/hadoop jar src/zj-mvn-demo.jar com.zjmvn.hadoop.StepFirstMapReduce first/input first/output
		
		// output:
		// A  G,F,B,H,D,C,
		// B  F,E,A,
		// C  E,H,G,F,B,A,
		// D  A,E,G,C,H,F,
		// E  F,B,D,G,A,H,
		// F  A,D,G,C,
		// I  C,
		// K  B,
		// L  E,D,
		// M  F,E,
		// O  F,H,A,

		logger.info("StepFirstMapReduce task is started.");

		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf);

		job.setJarByClass(StepFirstMapReduce.class);
		job.setMapperClass(FirstMapper.class);
		job.setReducerClass(FirstReducer.class);

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);

		FileInputFormat.setInputPaths(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));

		if (!job.waitForCompletion(true)) {
			logger.info("StepFirstMapReduce task is failed.");
			System.exit(1);
		}
	}

}
