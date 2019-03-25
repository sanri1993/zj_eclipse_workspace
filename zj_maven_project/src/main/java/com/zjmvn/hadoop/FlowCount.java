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

public class FlowCount {

	private static final Logger logger = Logger.getLogger(FlowCount.class);

	private static class FlowCountMapper extends Mapper<LongWritable, Text, Text, FlowBean> {

		@Override
		public void map(LongWritable key, Text value, Mapper<LongWritable, Text, Text, FlowBean>.Context context)
				throws IOException, InterruptedException {
			String line = value.toString();
			String[] fields = line.split("\t");
			String phoneNum = fields[0];
			long upFlow = Long.parseLong(fields[1]);
			long dFlow = Long.parseLong(fields[2]);

			context.write(new Text(phoneNum), new FlowBean(upFlow, dFlow));
		}
	}

	private static class FlowCountReducer extends Reducer<Text, FlowBean, Text, FlowBean> {
		@Override
		public void reduce(Text key, Iterable<FlowBean> values, Reducer<Text, FlowBean, Text, FlowBean>.Context context)
				throws IOException, InterruptedException {
			long sumUpFlow = 0;
			long sumdFlow = 0;

			for (FlowBean value : values) {
				sumUpFlow += value.getUpFlow();
				sumdFlow += value.getdFlow();
			}
			FlowBean resultBean = new FlowBean(sumUpFlow, sumdFlow);
			context.write(key, resultBean);
		}
	}

	public static void main(String[] args) throws Exception {

		// input:
//		13726230501	200	1100
//		13396230502	300	1200
//		13897230503	400	1300
//		13897230503	100	300
//		13597230534	500	1400
//		13597230534	300	1200

		// run cmd:
		// bin/hadoop jar src/zj-mvn-demo.jar com.zjmvn.hadoop.FlowCount flowcount/input flowcount/output

		// output:
		// 5 partition
		// bin/hdfs dfs -ls flowcount/output
//		-rw-r--r--   1 root supergroup          0 2019-03-25 05:13 flowcount/output/_SUCCESS
//		-rw-r--r--   1 root supergroup         26 2019-03-25 05:13 flowcount/output/part-r-00000
//		-rw-r--r--   1 root supergroup         26 2019-03-25 05:13 flowcount/output/part-r-00001
//		-rw-r--r--   1 root supergroup         26 2019-03-25 05:13 flowcount/output/part-r-00002
//		-rw-r--r--   1 root supergroup         26 2019-03-25 05:13 flowcount/output/part-r-00003
//		-rw-r--r--   1 root supergroup          0 2019-03-25 05:13 flowcount/output/part-r-00004

		// bin/hdfs dfs -cat flowcount/output/*
//		13726230501	200	1100	1300
//		13396230502	300	1200	1500
//		13897230503	500	1600	2100
//		13597230534	800	2600	3400
		
		logger.info("FlowCount mapreduce is started.");

		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf);

		job.setJarByClass(FlowCount.class);
		job.setMapperClass(FlowCountMapper.class);
		job.setReducerClass(FlowCountReducer.class);

		// 指定自定义分区器
		job.setPartitionerClass(ProvincePartitioner.class);
		// 指定分区数量的reducetask
		job.setNumReduceTasks(5);

		// 指定map输出数据的kv类型
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(FlowBean.class);

		// 指定最终输出数据的kv类型
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(FlowBean.class);

		for (String arg : args) {
			logger.info("argument: " + arg);
		}
		// 指定job输入目录
		FileInputFormat.setInputPaths(job, new Path(args[1]));
		// 指定job输出目录
		FileOutputFormat.setOutputPath(job, new Path(args[2]));

		boolean res = job.waitForCompletion(true);
		System.exit(res ? 0 : 1);
	}

}
