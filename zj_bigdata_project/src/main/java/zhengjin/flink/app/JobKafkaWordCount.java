package zhengjin.flink.app;

import java.util.Properties;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.datastream.WindowedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobKafkaWordCount {

	private static Logger LOG = LoggerFactory.getLogger(JobKafkaWordCount.class);
	private static final String prefix = "KafkaWordCountDemo: ";

	public static void main(String[] args) throws Exception {

		LOG.info(prefix + "get stream exec env.");
		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

		LOG.info(prefix + "flink job source operator by kafka.");
		Properties props = new Properties();
		props.setProperty("bootstrap.servers", "kafka:9092");
		props.setProperty("group.id", "flink-group");
		FlinkKafkaConsumer<String> consumer = new FlinkKafkaConsumer<>("FlinkTopic01", new SimpleStringSchema(), props);
		DataStreamSource<String> stringDataStreamSource = env.addSource(consumer);

		LOG.info(prefix + "flink job map operator.");
		SingleOutputStreamOperator<String> flatMap = stringDataStreamSource
				.flatMap(new FlatMapFunction<String, String>() {
					private static final long serialVersionUID = 1L;

					@Override
					public void flatMap(String s, Collector<String> outCollector) throws Exception {
						String[] split = s.split(" ");
						for (String currentOne : split) {
							outCollector.collect(currentOne);
						}
					}
				});

		SingleOutputStreamOperator<Tuple2<String, Integer>> map = flatMap
				.map(new MapFunction<String, Tuple2<String, Integer>>() {
					private static final long serialVersionUID = 1L;

					@Override
					public Tuple2<String, Integer> map(String word) throws Exception {
						return new Tuple2<>(word, 1);
					}
				});

		LOG.info(prefix + "flink job keyby and window operator.");
		KeyedStream<Tuple2<String, Integer>, Tuple> keyByResult = map.keyBy(0);
		WindowedStream<Tuple2<String, Integer>, Tuple, TimeWindow> windowResult = keyByResult
				.timeWindow(Time.seconds(5L));

		LOG.info(prefix + "flink job sum operator.");
		SingleOutputStreamOperator<Tuple2<String, Integer>> endResult = windowResult.sum(1);

		LOG.info(prefix + "flink job sink operator to kafka.");
		@SuppressWarnings("deprecation")
		FlinkKafkaProducer<String> producer = new FlinkKafkaProducer<String>("FlinkResult01", new SimpleStringSchema(),
				props);

		endResult.map(new MapFunction<Tuple2<String, Integer>, String>() {
			private static final long serialVersionUID = 1L;

			@Override
			public String map(Tuple2<String, Integer> tp2) throws Exception {
				return tp2.f0 + "-" + tp2.f1;
			}
		}).addSink(producer).setParallelism(1);

		endResult.print().setParallelism(1);
//		endResult.writeAsText("/tmp/flink_kafka_out.txt", FileSystem.WriteMode.OVERWRITE).setParallelism(1);

		env.execute("Kafka WordCount");
	}

}
