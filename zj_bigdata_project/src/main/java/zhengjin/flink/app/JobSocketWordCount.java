package zhengjin.flink.app;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.datastream.WindowedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobSocketWordCount {

	private static Logger LOG = LoggerFactory.getLogger(JobSocketWordCount.class);

	public static void main(String[] args) throws Exception {

		final String hostname;
		final int port;
		try {
			final ParameterTool params = ParameterTool.fromArgs(args);
			hostname = params.get("host");
			port = params.getInt("port");
		} catch (Exception e) {
			LOG.error("No host or port specified. Please run 'FlinkSocketWordCount --host <hostname> --port <port>'");
			return;
		}

		LOG.info("WordCountDemo flink job source operator.");
		final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		DataStreamSource<String> text = env.socketTextStream(hostname, port);

		LOG.info("WordCountDemo flink job map operator.");
		SingleOutputStreamOperator<WordWithCount> wordWithCountInfos = text
				.flatMap(new FlatMapFunction<String, WordWithCount>() {
					private static final long serialVersionUID = 1L;

					@Override
					public void flatMap(String line, Collector<WordWithCount> collector) throws Exception {
						for (String word : line.split(" ")) {
							LOG.info("WordCountDemo get word: " + word);
							collector.collect(new WordWithCount(word, 1L));
						}
					}
				});

		LOG.info("WordCountDemo flink job keyby and window operator.");
		KeyedStream<WordWithCount, Tuple> keyedInfos = wordWithCountInfos.keyBy("word");
		WindowedStream<WordWithCount, Tuple, TimeWindow> windowedInfo = keyedInfos.timeWindow(Time.seconds(5L),
				Time.seconds(1L));

		LOG.info("WordCountDemo flink job reduce operator.");
		SingleOutputStreamOperator<WordWithCount> windowCounts = windowedInfo
				.reduce(new ReduceFunction<WordWithCount>() {
					private static final long serialVersionUID = 1L;

					@Override
					public WordWithCount reduce(WordWithCount w1, WordWithCount w2) throws Exception {
						return new WordWithCount(w1.getWord(), w1.getCount() + w2.getCount());
					}
				});

		LOG.info("WordCountDemo flink job sink operator.");
//		windowCounts.print().setParallelism(1);
		windowCounts.writeAsText("/tmp/flink_socket_out.txt", FileSystem.WriteMode.OVERWRITE).setParallelism(1);

		env.execute("Socket Window WordCount");
	}

	public static class WordWithCount {
		public String word;
		public Long count;

		public WordWithCount() {
		}

		public WordWithCount(String word, Long count) {
			this.word = word;
			this.count = count;
		}

		public String getWord() {
			return word;
		}

		public void setWord(String word) {
			this.word = word;
		}

		public Long getCount() {
			return count;
		}

		public void setCount(Long count) {
			this.count = count;
		}

		@Override
		public String toString() {
			return word + " : " + count;
		}
	}

}
