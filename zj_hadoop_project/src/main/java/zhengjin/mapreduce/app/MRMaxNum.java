package zhengjin.mapreduce.app;

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

public class MRMaxNum extends Configured implements Tool {

	/** Mapper */
	private static class MaxNumMapper extends Mapper<LongWritable, Text, LongWritable, NullWritable> {

		private long max = Long.MIN_VALUE;

		@Override
		protected void map(LongWritable key, Text value,
				Mapper<LongWritable, Text, LongWritable, NullWritable>.Context context)
				throws java.io.IOException, InterruptedException {
			long temp = Long.parseLong(value.toString().trim());
			if (temp > this.max) {
				this.max = temp;
			}
		}

		@Override
		protected void cleanup(Mapper<LongWritable, Text, LongWritable, NullWritable>.Context context)
				throws java.io.IOException, InterruptedException {
			// set max number by key, instead of value
			context.write(new LongWritable(this.max), NullWritable.get());
		}
	}

	/** Reducer */
	private static class MaxNumReducer extends Reducer<LongWritable, NullWritable, LongWritable, NullWritable> {
		long max = Long.MIN_VALUE;

		@Override
		protected void reduce(LongWritable key, java.lang.Iterable<NullWritable> values,
				Reducer<LongWritable, NullWritable, LongWritable, NullWritable>.Context context)
				throws java.io.IOException, InterruptedException {
			long temp = key.get();
			if (temp > this.max) {
				this.max = temp;
			}
		}

		@Override
		protected void cleanup(Reducer<LongWritable, NullWritable, LongWritable, NullWritable>.Context context)
				throws java.io.IOException, InterruptedException {
			// write final results in cleanup (as compare to MRTopKNums)
			context.write(new LongWritable(this.max), NullWritable.get());
		}
	}

	public static void main(String[] args) throws Exception {

		// input:
		// for i in {1..10000}; do echo $(($RANDOM + $RANDOM)) >> file_nums.txt; done

		// hadoop jar zj-mvn-demo.jar com.zjmvn.hadoop.MRMaxNum \
		// maxnum/input maxnum/output

		// output:
		// hdfs dfs -text /user/root/maxnum/output/*

		// verify:
		// sort -n file_nums.txt | tail -1

		Configuration conf = new Configuration();
		int res = ToolRunner.run(conf, new MRMaxNum(), args);
		System.exit(res);
	}

	@Override
	public int run(String[] args) throws Exception {
		Job job = Job.getInstance(getConf(), "MaxNumMapReduce");
		job.setJarByClass(MRTopKNums.class);

		// 设置自定义Mapper
		job.setMapperClass(MaxNumMapper.class);
		job.setMapOutputKeyClass(LongWritable.class);
		job.setMapOutputValueClass(NullWritable.class);

		// 设置自定义Reducer
		job.setReducerClass(MaxNumReducer.class);
		job.setOutputKeyClass(LongWritable.class);
		job.setOutputValueClass(NullWritable.class);

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
