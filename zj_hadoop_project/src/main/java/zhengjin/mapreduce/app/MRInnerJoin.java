package zhengjin.mapreduce.app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.log4j.Logger;

public class MRInnerJoin {

	private static final Logger logger = Logger.getLogger(MRInnerJoin.class);

	/** Mapper */
	private static class JoinMapper extends Mapper<LongWritable, Text, Text, InfoBean> {

		InfoBean bean = new InfoBean();

		@Override
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			FileSplit inputSplit = (FileSplit) context.getInputSplit();
			String filename = inputSplit.getPath().getName();
			String[] fields = value.toString().split(",");
			String pid = "";

			if (filename.startsWith("order")) {
				pid = fields[2];
				bean.set(Integer.parseInt(fields[0]), fields[1], pid, Integer.parseInt(fields[3]), "", 0, 0, "0");
			} else {
				logger.info("zhengjin " + Arrays.toString(fields));
				pid = fields[0];
				bean.set(0, "", pid, 0, fields[1], Integer.parseInt(fields[2]), Float.parseFloat(fields[3]), "1");
			}
			context.write(new Text(pid), bean);
		}
	}

	/** Reducer */
	private static class JoinReducer extends Reducer<Text, InfoBean, InfoBean, NullWritable> {

		@Override
		public void reduce(Text pid, Iterable<InfoBean> beans, Context context)
				throws IOException, InterruptedException {

			InfoBean pdBean = new InfoBean();
			ArrayList<InfoBean> orderBeans = new ArrayList<InfoBean>();

			try {
				for (InfoBean bean : beans) {
					if ("1".equals(bean.getFlag())) {
						// product
						BeanUtils.copyProperties(pdBean, bean);
					} else {
						// order
						InfoBean odBean = new InfoBean();
						BeanUtils.copyProperties(odBean, bean);
						orderBeans.add(odBean);
					}
				}
			} catch (Exception e) {
				logger.error(e.getMessage());
			}

			for (InfoBean bean : orderBeans) {
				bean.setPName(pdBean.getPName());
				bean.setCategory_id(pdBean.getCategory_id());
				bean.setPrice(pdBean.getPrice());
				context.write(bean, NullWritable.get());
			}
		}
	}

	public static void main(String[] args) throws Exception {

		// input of orders:
		// 1001,20150710,P0001,12
		// 1002,20150810,P0001,13
		// 1003,20150910,P0002,13
		// 1004,20150915,P0003,3
		// input of products:
		// P0001,小米5,1,1999.0
		// P0002,锤子T1,2,1599.0
		// P0003,锤子T2,3,1999.0

		// hadoop jar zj-mvn-demo.jar com.zjmvn.hadoop.MRInnerJoin \
		// joinmr/input joinmr/output

		// output:
		// order_id=1002,dataString=20150810,p_id=P0001,amount=13,pname=小米5,category_id=1,price=1999.00
		// order_id=1001,dataString=20150710,p_id=P0001,amount=12,pname=小米5,category_id=1,price=1999.00
		// order_id=1003,dataString=20150910,p_id=P0002,amount=13,pname=锤子T1,category_id=2,price=1599.00
		// order_id=1004,dataString=20150915,p_id=P0003,amount=3,pname=锤子T2,category_id=3,price=1999.00

		logger.info("JoinMapReduce task is started.");

		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf);

		job.setJarByClass(MRInnerJoin.class);
		job.setMapperClass(JoinMapper.class);
		job.setReducerClass(JoinReducer.class);

		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(InfoBean.class);

		job.setOutputKeyClass(InfoBean.class);
		job.setOutputValueClass(NullWritable.class);

		FileInputFormat.setInputPaths(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));

		if (!job.waitForCompletion(true)) {
			logger.info("JoinMapReduce task is failed.");
			System.exit(1);
		}
	}

}
