package zhengjin.mapreduce.app;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.log4j.Logger;

public class MR1SortGroup {

	private static final Logger logger = Logger.getLogger(MR1SortGroup.class);

	/** Comparator */
	private static class MyGroupingComparator extends WritableComparator {

		@SuppressWarnings("unused")
		public MyGroupingComparator() {
			super(OrderBean.class, true);
		}

		@Override
		@SuppressWarnings("rawtypes")
		public int compare(WritableComparable a, WritableComparable b) {
			// 排序: 根据bean的item_id和amount来排序
			// <{ Order_0000001 222.8 }, null>
			// <{ Order_0000001 25.8 }, null>
			// 分组: 通过item_id分组, 每个分组取第一个bean做为key, 也就是amount最大的bean
			// <{ Order_0000001 222.8 }, [null, null]>
			OrderBean order1 = (OrderBean) a;
			OrderBean order2 = (OrderBean) b;
			return order1.getItemid().compareTo(order2.getItemid());
		}
	}

	/** Mapper */
	private static class SortMapper extends Mapper<LongWritable, Text, OrderBean, NullWritable> {

		OrderBean order = new OrderBean();

		@Override
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			String line = value.toString();
			String[] fields = line.split(",");
			order.set(new Text(fields[0]), new DoubleWritable(Double.parseDouble(fields[2])));
			context.write(order, NullWritable.get()); // only key
		}
	}

	/** Reducer */
	private static class SortReducer extends Reducer<OrderBean, NullWritable, OrderBean, NullWritable> {

		@Override
		public void reduce(OrderBean key, Iterable<NullWritable> values, Context context)
				throws IOException, InterruptedException {
			context.write(key, NullWritable.get()); // only key
		}
	}

	public static void main(String[] args) throws Exception {

		// input:
		// Order_000004,Pdt_06,102.8
		// Order_000001,Pdt_01,222.8
		// Order_000002,Pdt_03,522.8
		// Order_000003,Pdt_01,282.8
		// Order_000002,Pdt_04,122.4
		// Order_000001,Pdt_05,25.8
		// Order_000003,Pdt_01,612.1
		// Order_000004,Pdt_07,716.2

		// hadoop jar zj-mvn-demo.jar com.zjmvn.hadoop.MR1SortGroup \
		// groupcount/input groupcount/output

		// output files (hdfs dfs -ls groupcount/output):
		// 2019-03-25 06:30 groupcount/output/part-r-00000
		// 2019-03-25 06:30 groupcount/output/part-r-00001

		// output text (hdfs dfs -text groupcount/output/*):
		// Order_000002 522.8
		// Order_000004 716.2
		// Order_000001 222.8
		// Order_000003 612.1

		logger.info("GroupSort mapreduce is started.");

		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf);

		job.setJarByClass(MR1SortGroup.class);
		job.setMapperClass(SortMapper.class);
		job.setReducerClass(SortReducer.class);

		job.setMapOutputKeyClass(OrderBean.class);
		job.setMapOutputValueClass(NullWritable.class);

		job.setOutputKeyClass(OrderBean.class);
		job.setOutputValueClass(NullWritable.class);

		job.setGroupingComparatorClass(MyGroupingComparator.class); // reduce side Comparator
		job.setPartitionerClass(ItemIdPartitioner.class);
		job.setNumReduceTasks(2);

		FileInputFormat.setInputPaths(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));

		if (!job.waitForCompletion(true)) {
			logger.info("GroupSort mapreduce is failed.");
			System.exit(1);
		}
	}

}
