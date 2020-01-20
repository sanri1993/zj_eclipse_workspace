package zhengjin.mapreduce.app;

import java.util.TreeMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;

public class MRTopKNums extends Configured implements Tool {

	private static final Logger logger = Logger.getLogger(MRTopKNums.class);

	/** Mapper */
	private static class TopKNumMapper extends Mapper<LongWritable, Text, NullWritable, LongWritable> {

		private static final int TOP = 10;
		private TreeMap<Long, Long> tm = new TreeMap<Long, Long>();

		@Override
		protected void map(LongWritable key, Text value,
				Mapper<LongWritable, Text, NullWritable, LongWritable>.Context context)
				throws java.io.IOException, InterruptedException {
			long temp = Long.parseLong(value.toString().trim());
			tm.put(temp, temp);
			if (tm.size() > TOP) {
				tm.remove(tm.firstKey()); // remove min value
//				tm.remove(tm.lastKey());
			}
		}

		@Override
		protected void cleanup(Mapper<LongWritable, Text, NullWritable, LongWritable>.Context context)
				throws java.io.IOException, InterruptedException {
			for (Long num : tm.values()) {
				context.write(NullWritable.get(), new LongWritable(num)); // use null key
			}
		}
	}

	/** Reducer */
	private static class TopKNumReducer extends Reducer<NullWritable, LongWritable, NullWritable, LongWritable> {

		private static final int TOP = 10;
		private TreeMap<Long, Long> tm = new TreeMap<Long, Long>();

		@Override
		protected void reduce(NullWritable key, java.lang.Iterable<LongWritable> values,
				Reducer<NullWritable, LongWritable, NullWritable, LongWritable>.Context context)
				throws java.io.IOException, InterruptedException {
			// values: Iterable contains top k numbers from all mapper tasks
			for (LongWritable value : values) {
				logger.info("TopKNumReducer, iterable value: " + value.get());
				tm.put(value.get(), value.get());
				if (tm.size() > TOP) {
					tm.remove(tm.firstKey());
				}
			}

			for (Long value : tm.descendingKeySet()) {
				context.write(NullWritable.get(), new LongWritable(value));
			}
		}
	}

	public static void main(String[] args) throws Exception {
		// input:
		// for i in {1..10000}; do echo $(($RANDOM + $RANDOM)) >> file_nums.txt; done

		// run cmd:
		// bin/hadoop jar src/zj-mvn-demo.jar com.zjmvn.hadoop.TopKNumMapReduce
		// topk/input topk/output

		// output:
		// bin/hdfs dfs -cat /user/root/topk/output/*

		// verify:
		// sort -n file_nums.txt | tail -10

		Configuration conf = new Configuration();
		int res = ToolRunner.run(conf, new MRTopKNums(), args);
		System.exit(res);
	}

	@Override
	public int run(String[] args) throws Exception {
		Job job = Job.getInstance(getConf(), "TopKNumMapReduce");
		job.setJarByClass(MRTopKNums.class);

		// 设置自定义Mapper
		job.setMapperClass(TopKNumMapper.class);
		job.setMapOutputKeyClass(NullWritable.class);
		job.setMapOutputValueClass(LongWritable.class);

		// 设置自定义Reducer
		job.setReducerClass(TopKNumReducer.class);
		job.setOutputKeyClass(NullWritable.class);
		job.setOutputValueClass(LongWritable.class);

		job.setNumReduceTasks(1);

		FileInputFormat.setInputPaths(job, new Path(args[0]));
		Path outDir = new Path(args[1]);
		FileOutputFormat.setOutputPath(job, outDir);

		FileSystem fs = FileSystem.get(this.getConf());
		if (fs.exists(outDir)) {
			fs.delete(outDir, true);
		}

		return job.waitForCompletion(true) ? 0 : 1;
	}

}
