package zhengjin.mapreduce.app;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.log4j.Logger;

public class MRFlowCount {

	private static final Logger logger = Logger.getLogger(MRFlowCount.class);

	/** Mapper */
	private static class FlowCountMapper extends Mapper<LongWritable, Text, Text, FlowBean> {

		private int myCounter = 0;

		@Override
		public void setup(Context context) throws IOException, InterruptedException {
			logger.info("FlowCountMapper.setup() is invoked: " + this.hashCode());
			FileSplit split = (FileSplit) context.getInputSplit();
			logger.info("FlowCountMapper.setup() get file: " + split.getPath().getName());
		}

		@Override
		public void map(LongWritable key, Text value, Mapper<LongWritable, Text, Text, FlowBean>.Context context)
				throws IOException, InterruptedException {
			logger.info("FlowCountMapper.map() is invoked: " + (++this.myCounter));
			String line = value.toString();
			String[] fields = line.split("\t");
			String phoneNum = fields[0];
			long upFlow = Long.parseLong(fields[1]);
			long dFlow = Long.parseLong(fields[2]);

			context.write(new Text(phoneNum), new FlowBean(upFlow, dFlow));
		}

		@Override
		public void cleanup(Context context) throws IOException, InterruptedException {
			logger.info("FlowCountMapper.clearup() is invoked: " + this.hashCode());
		}
	}

	/** Reducer */
	private static class FlowCountReducer extends Reducer<Text, FlowBean, Text, FlowBean> {

		private int myCounter = 0;

		@Override
		public void setup(Context context) throws IOException, InterruptedException {
			logger.info("FlowCountReducer.setup() is invoked: " + this.hashCode());
		}

		@Override
		public void reduce(Text key, Iterable<FlowBean> values, Reducer<Text, FlowBean, Text, FlowBean>.Context context)
				throws IOException, InterruptedException {
			logger.info("FlowCountReducer.reduce() is invoked: " + (++this.myCounter));
			long sumUpFlow = 0;
			long sumdFlow = 0;

			for (FlowBean value : values) {
				sumUpFlow += value.getUpFlow();
				sumdFlow += value.getdFlow();
			}
			FlowBean resultBean = new FlowBean(sumUpFlow, sumdFlow);
			context.write(key, resultBean);
		}

		@Override
		public void cleanup(Context context) throws IOException, InterruptedException {
			logger.info("FlowCountReducer.clearup() is invoked: " + this.hashCode());
		}
	}

	public static void main(String[] args) throws Exception {

		// input:
		// 13726230501 200 1100
		// 13396230502 300 1200
		// 13897230503 400 1300
		// 13897230503 100 300
		// 13597230534 500 1400
		// 13597230534 300 1200

		// hadoop jar zj-mvn-demo.jar com.zjmvn.hadoop.MRFlowCount \
		// flowcount/input flowcount/output

		// output of 5 partitions (hdfs dfs -ls flowcount/output):
		// 2019-03-25 05:13 flowcount/output/part-r-00000
		// 2019-03-25 05:13 flowcount/output/part-r-00001
		// 2019-03-25 05:13 flowcount/output/part-r-00002
		// 2019-03-25 05:13 flowcount/output/part-r-00003
		// 2019-03-25 05:13 flowcount/output/part-r-00004

		// output text (hdfs dfs -cat flowcount/output/*):
		// 13726230501 200 1100 1300
		// 13396230502 300 1200 1500
		// 13897230503 500 1600 2100
		// 13597230534 800 2600 3400

		logger.info("FlowCount mapreduce is started.");

		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf);

		job.setJarByClass(MRFlowCount.class);
		job.setMapperClass(FlowCountMapper.class);
		job.setReducerClass(FlowCountReducer.class);

		// 指定分区数量的reducetask
		job.setNumReduceTasks(5);
		// 指定自定义分区器
		job.setPartitionerClass(ProvincePartitioner.class);

		// 指定map输出数据的kv类型
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(FlowBean.class);

		// 指定最终输出数据的kv类型
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(FlowBean.class);

		// 指定job输入目录
		FileInputFormat.setInputPaths(job, new Path(args[0]));
		// 指定job输出目录
		Path outDir = new Path(args[1]);
		FileOutputFormat.setOutputPath(job, outDir);

		FileSystem fs = FileSystem.get(conf);
		if (fs.exists(outDir)) {
			fs.delete(outDir, true);
		}

		boolean res = job.waitForCompletion(true);
		System.exit(res ? 0 : 1);
	}

}
