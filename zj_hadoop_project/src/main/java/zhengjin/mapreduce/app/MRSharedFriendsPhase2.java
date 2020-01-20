package zhengjin.mapreduce.app;

import java.io.IOException;
import java.util.Arrays;

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

public class MRSharedFriendsPhase2 {

	private static final Logger logger = Logger.getLogger(MRSharedFriendsPhase2.class);

	/** Mapper */
	private static class SecondMapper extends Mapper<LongWritable, Text, Text, Text> {

		@Override
		protected void map(LongWritable key, Text value, Mapper<LongWritable, Text, Text, Text>.Context context)
				throws IOException, InterruptedException {
			String line = value.toString();
			String[] friend_users = line.split("\t");
			String friend = friend_users[0];
			String[] users = friend_users[1].split(",");

			Arrays.sort(users);
			for (int i = 0; i < users.length - 1; i++) {
				for (int j = i + 1; j < users.length; j++) {
					context.write(new Text(users[i] + "-" + users[j]), new Text(friend));
				}
			}
		}
	}

	/** Reducer */
	private static class SecondReducer extends Reducer<Text, Text, Text, Text> {

		@Override
		protected void reduce(Text user_user, Iterable<Text> friends, Context context)
				throws IOException, InterruptedException {
			StringBuffer buf = new StringBuffer();
			for (Text friend : friends) {
				buf.append(friend).append(" ");
			}
			context.write(user_user, new Text(buf.toString()));
		}
	}

	public static void main(String[] args) throws Exception {

		// input:
		// A G,F,B,H,D,C,
		// B F,E,A,
		// C E,H,G,F,B,A,
		// D A,E,G,C,H,F,
		// E F,B,D,G,A,H,
		// F A,D,G,C,
		// I C,
		// K B,
		// L E,D,
		// M F,E,
		// O F,H,A,

		// hadoop jar zj-mvn-demo.jar com.zjmvn.hadoop.MRSharedFriendsPhase2 \
		// first/output/part-r-0* second/output

		// output:
		// A-B E C
		// A-C D F
		// A-D E F
		// A-E D B C
		// A-F E B O D C
		// A-G E F C D
		// A-H D C O E
		// B-C A
		// B-D E A
		// ...

		logger.info("StepSecondMapReduce task is started.");

		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf);

		job.setJarByClass(MRSharedFriendsPhase2.class);
		job.setMapperClass(SecondMapper.class);
		job.setReducerClass(SecondReducer.class);

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);

		FileInputFormat.setInputPaths(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));

		if (!job.waitForCompletion(true)) {
			logger.info("StepSecondMapReduce task is failed.");
			System.exit(1);
		}
	}

}
