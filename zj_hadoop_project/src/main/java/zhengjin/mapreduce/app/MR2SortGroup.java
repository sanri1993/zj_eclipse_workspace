package zhengjin.mapreduce.app;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.RawComparator;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;
//import org.apache.hadoop.io.compress.CompressionCodec;
//import org.apache.hadoop.io.compress.GzipCodec;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class MR2SortGroup extends Configured implements Tool {

	private static class MyNewKey implements WritableComparable<MyNewKey> {

		private long firstNum;
		private long secondNum;

		@SuppressWarnings("unused")
		public MyNewKey() {
		}

		public MyNewKey(long first, long second) {
			this.firstNum = first;
			this.secondNum = second;
		}

		@Override
		public void write(DataOutput out) throws IOException {
			out.writeLong(this.firstNum);
			out.writeLong(this.secondNum);
		}

		@Override
		public void readFields(DataInput in) throws IOException {
			this.firstNum = in.readLong();
			this.secondNum = in.readLong();
		}

		@Override
		public int compareTo(MyNewKey anotherKey) {
			long ret = this.firstNum - anotherKey.firstNum;
			return (int) (ret == 0 ? -(this.secondNum - anotherKey.secondNum) : ret);
		}
	}

	/** Comparator */
	private static class MyGroupingComparator implements RawComparator<MyNewKey> {

		@Override
		public int compare(MyNewKey key1, MyNewKey key2) {
			return (int) (key1.firstNum - key2.firstNum);
		}

		@Override
		public int compare(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
			return WritableComparator.compareBytes(b1, s1, 8, b2, s2, 8);
		}
	}

	/** Mapper */
	private static class SortGroupMapper extends Mapper<LongWritable, Text, MyNewKey, NullWritable> {

		@Override
		protected void map(LongWritable key, Text value,
				Mapper<LongWritable, Text, MyNewKey, NullWritable>.Context context)
				throws java.io.IOException, InterruptedException {
			String[] spilted = value.toString().split(" ");
			long firstNum = Long.parseLong(spilted[0]);
			long secondNum = Long.parseLong(spilted[1]);
			context.write(new MyNewKey(firstNum, secondNum), NullWritable.get()); // only key
		}
	}

	/** Reducer */
	private static class SortGroupReducer extends Reducer<MyNewKey, NullWritable, LongWritable, LongWritable> {

		@Override
		protected void reduce(MyNewKey key, java.lang.Iterable<NullWritable> values,
				Reducer<MyNewKey, NullWritable, LongWritable, LongWritable>.Context context)
				throws java.io.IOException, InterruptedException {
			context.write(new LongWritable(key.firstNum), new LongWritable(key.secondNum));
		}
	}

	public static void main(String[] args) {

		// input:
		// 3 2
		// 3 3
		// 3 1
		// 2 2
		// 2 1
		// 1 1

		// hadoop jar zj-mvn-demo.jar com.zjmvn.hadoop.MR2SortGroup \
		// sortgroup/input sortgroup/output false

		// output (bin/hdfs dfs -cat /user/root/sortgroup/output/*):
		// 1 1
		// 2 2
		// 2 1
		// 3 3
		// 3 2
		// 3 1

		// output by set MyGroupingComparator:
		// 1 1
		// 2 2
		// 3 3

		Configuration conf = new Configuration();
//		conf.setBoolean("mapred.compress.map.output", true);
//		conf.setBoolean("mapred.output.compress", true);
//		conf.setClass("mapred.output.compression.codec", GzipCodec.class, CompressionCodec.class);

		try {
			int res = ToolRunner.run(conf, new MR2SortGroup(), args);
			System.exit(res);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public int run(String[] args) throws Exception {
		Job job = Job.getInstance(this.getConf(), "SortGroupMapReduce02");

		// if not set, java.lang.ClassNotFoundException:
		// Class com.zjmvn.hadoop.MR2SortGroup$SortGroupMapper not found
		job.setJarByClass(MR2SortGroup.class);
		job.setMapperClass(SortGroupMapper.class);
		job.setReducerClass(SortGroupReducer.class);

		if (Boolean.parseBoolean(args[2])) {
			job.setGroupingComparatorClass(MyGroupingComparator.class);
		}

		job.setMapOutputKeyClass(MyNewKey.class);
		job.setMapOutputValueClass(NullWritable.class);

		job.setOutputKeyClass(LongWritable.class);
		job.setOutputValueClass(LongWritable.class);

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
