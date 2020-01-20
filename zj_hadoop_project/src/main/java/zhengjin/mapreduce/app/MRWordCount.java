package zhengjin.mapreduce.app;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;
import org.apache.hadoop.mapreduce.filecache.DistributedCache;
import org.apache.hadoop.util.StringUtils;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;

@SuppressWarnings("deprecation")
public class MRWordCount extends Configured implements Tool {

	private static final Logger logger = Logger.getLogger(MRWordCount.class);

	/** Mapper */
	private static class Map extends MapReduceBase implements Mapper<LongWritable, Text, Text, IntWritable> {

		static enum Counters {
			INPUT_WORDS
		};

		private final static IntWritable one = new IntWritable(1);
		private Text word = new Text();

		private boolean caseSensitive = true;
		private Set<String> patternsToSkip = new HashSet<String>();

		private String inputFile;
		private long numRecords = 0;

		@Override
		public void configure(JobConf job) {
			this.caseSensitive = job.getBoolean("wordcount.case.sensitive", true);
			this.inputFile = job.get("map.input.file");

			if (job.getBoolean("wordcount.skip.patterns", false)) {
				Path[] patternsFiles = new Path[0];
				try {
					patternsFiles = DistributedCache.getLocalCacheFiles(job);
				} catch (IOException e) {
					logger.error("Caught exception while getting cached files: " + StringUtils.stringifyException(e));
				}

				for (Path file : patternsFiles) {
					this.parseSkipFile(file);
				}
			}
		}

		private void parseSkipFile(Path patternsFile) {
			BufferedReader fis = null;
			try {
				fis = new BufferedReader(new FileReader(patternsFile.toString()));
				String pattern = null;
				while ((pattern = fis.readLine()) != null) {
					this.patternsToSkip.add(pattern);
				}
			} catch (IOException e) {
				e.printStackTrace();
				logger.error(String.format("Caught exception while parsing the cached file '%s': %s", patternsFile,
						StringUtils.stringifyException(e)));
			} finally {
				if (fis != null) {
					try {
						fis.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

		@Override
		public void map(LongWritable key, Text value, OutputCollector<Text, IntWritable> output, Reporter reporter)
				throws IOException {
			String line = this.caseSensitive ? value.toString() : value.toString().toLowerCase();

			for (String pattern : this.patternsToSkip) {
				line = line.replaceAll(pattern, "");
			}

			StringTokenizer tokenizer = new StringTokenizer(line);
			while (tokenizer.hasMoreTokens()) {
				word.set(tokenizer.nextToken());
				output.collect(word, one);
				reporter.incrCounter(Counters.INPUT_WORDS, 1);
			}

			if ((++numRecords % 100) == 0) {
				reporter.setStatus(String.format("Finished processing %s records from the input file: %s",
						this.numRecords, this.inputFile));
			}
		}
	}

	/** Reducer */
	private static class Reduce extends MapReduceBase implements Reducer<Text, IntWritable, Text, IntWritable> {

		@Override
		public void reduce(Text key, Iterator<IntWritable> values, OutputCollector<Text, IntWritable> output,
				Reporter reporter) throws IOException {
			int sum = 0;
			while (values.hasNext()) {
				sum += values.next().get();
			}
			output.collect(key, new IntWritable(sum));
		}
	}

	@Override
	public int run(String[] args) throws Exception {
		logger.info("WordCount mapreduce is started.");

		JobConf conf = new JobConf(this.getConf(), MRWordCount.class);
		conf.setJobName("wordcount");

		conf.setOutputKeyClass(Text.class);
		conf.setOutputValueClass(IntWritable.class);

		conf.setCombinerClass(Reduce.class);
		conf.setMapperClass(Map.class);
		conf.setReducerClass(Reduce.class);

		conf.setInputFormat(TextInputFormat.class);
		conf.setOutputFormat(TextOutputFormat.class);

		List<String> other_args = new ArrayList<String>(10);
		for (int i = 0; i < args.length; ++i) {
			if ("-skip".equals(args[i])) {
				conf.setBoolean("wordcount.skip.patterns", true);
				DistributedCache.addCacheFile(new Path(args[++i]).toUri(), conf);
			} else {
				other_args.add(args[i]);
			}
		}

		FileInputFormat.setInputPaths(conf, new Path(args[0]));
		FileOutputFormat.setOutputPath(conf, new Path(args[1]));

		JobClient.runJob(conf);
		return 0;
	}

	public static void main(String[] args) throws Exception {

		// Example1, 2 files input:
		// Hello World, Bye World!
		// Hello Hadoop, Goodbye to hadoop.

		// put files:
		// hdfs dfs -mkdir -p /user/root/wordcount/input
		// hdfs dfs -put *.txt /user/root/wordcount/input

		// run mapreduce:
		// hadoop jar zj-hadoop-app.jar zhengjin.mapreduce.app.MRWordCount \
		// /user/root/wordcount/input/* /user/root/wordcount/output

		// output (hdfs dfs -text /user/root/wordcount/output/*):
		// Bye 1
		// Goodbye 1
		// Hadoop, 1
		// Hello 2
		// World! 1
		// World, 1
		// hadoop. 1
		// to 1

		// Example2, input pattern:
		// ,
		// !
		// \.
		// to

		// hadoop jar zj-mvn-demo.jar zhengjin.mapreduce.app.MRWordCount \
		// -Dwordcount.case.sensitive=false \
		// wordcount/input/file* wordcount/output -skip wordcount/input/patterns.txt

		// output:
		// bye 1
		// goodbye 1
		// hadoop 2
		// hello 2
		// world 2

		int res = ToolRunner.run(new Configuration(), new MRWordCount(), args);
		System.exit(res);
	}

}
