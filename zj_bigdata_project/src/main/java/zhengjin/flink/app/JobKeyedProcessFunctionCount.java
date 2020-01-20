package zhengjin.flink.app;

import java.io.IOException;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.watermark.Watermark;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobKeyedProcessFunctionCount {

	private static Logger LOG = LoggerFactory.getLogger(JobKeyedProcessFunctionCount.class);

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

		env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
		env.getConfig().setAutoWatermarkInterval(1000L);

		DataStreamSource<String> text = env.socketTextStream(hostname, port);

		DataStream<MyValue> input = text.map(new MapFunction<String, MyValue>() {

			private static final long serialVersionUID = 1L;

			@Override
			public MyValue map(String value) {
				try {
					String[] fields = value.split(",");
					return new MyValue(fields[0], Long.parseLong(fields[1]));
				} catch (ArrayIndexOutOfBoundsException e) {
					LOG.error("build MyValue failed, ArrayIndexOutOfBoundsException: " + e.getMessage());
					return new MyValue("test1", 1579078750000L);
				}
			}
		}).assignTimestampsAndWatermarks(new MyTimestampsAndWatermarks());

		DataStream<Tuple2<String, Long>> result = input.keyBy(r -> r.value).process(new CountWithTimeoutFunction());

		result.print("CountResults:").setParallelism(1);

		env.execute("Keyed Process Function Example");
		// flink run -c com.zjmvn.flink.JobKeyedProcessFunctionCount \
		// /tmp/target_jars/zj-mvn-demo.jar --host ncsocket --port 9000

		// input:
		// test1,1579078760000
		// test1,1579078762000
		// test1,1579078767000
		// test2,1579078772000
		// test3,1579078780000
	}

	private static class MyTimestampsAndWatermarks implements AssignerWithPeriodicWatermarks<MyValue> {

		private static final long serialVersionUID = 1L;

		private final long maxOutofOrderness = 0L;
		private long currentMaxTimestamp;

		@Override
		public long extractTimestamp(MyValue element, long previousElementTimestamp) {
			long timestamp = element.timestamp;
			this.currentMaxTimestamp = Math.max(timestamp, this.currentMaxTimestamp);
			return timestamp;
		}

		@Override
		public Watermark getCurrentWatermark() {
			return new Watermark(this.currentMaxTimestamp - this.maxOutofOrderness);
		}
	}

	/**
	 * The implementation of the ProcessFunction that maintains the count and
	 * timeouts
	 */
	private static class CountWithTimeoutFunction extends KeyedProcessFunction<String, MyValue, Tuple2<String, Long>> {

		private static final long serialVersionUID = 1L;
		private static final long interval = 5_000L;

		/** The state that is maintained by this process function */
		private ValueState<CountWithTimestamp> state;

		@Override
		public void open(Configuration parameters) throws Exception {
			this.state = this.getRuntimeContext()
					.getState(new ValueStateDescriptor<>("myState", CountWithTimestamp.class));
		}

		@Override
		public void processElement(MyValue val, KeyedProcessFunction<String, MyValue, Tuple2<String, Long>>.Context ctx,
				Collector<Tuple2<String, Long>> out) throws Exception {
			LOG.info("processElement: " + val);

			CountWithTimestamp current = this.state.value();
			if (current == null) {
				LOG.info("processElement, key:{} init state", val.value);
				current = new CountWithTimestamp();
				current.key = val.value;
			}

			current.count++;
			// set the state's timestamp to the record's assigned event time timestamp
			current.lastModified = val.timestamp;
			this.state.update(current);

			// schedule the next timer 60 seconds from the current event time
			ctx.timerService().registerEventTimeTimer(current.lastModified + interval);
		}

		@Override
		public void onTimer(long timestamp, OnTimerContext ctx, Collector<Tuple2<String, Long>> out)
				throws IOException {
			CountWithTimestamp result = this.state.value();
			LOG.info("onTimer callback, key:{}, ts:{}", result.key, timestamp);
			if (timestamp == result.lastModified + interval) {
				LOG.info("onTimer callback, output");
				out.collect(new Tuple2<String, Long>(result.key, result.count));
			}
		}
	}

	public static class MyValue {

		public String value;
		public long timestamp;

		public MyValue() {
		}

		public MyValue(String val, long ts) {
			this.value = val;
			this.timestamp = ts;
		}

		@Override
		public String toString() {
			return String.format("(%s,%d)", this.value, this.timestamp);
		}
	}

	/**
	 * The data type stored in the state
	 */
	private static class CountWithTimestamp {

		public String key;
		public long count;
		public long lastModified;
	}

}
