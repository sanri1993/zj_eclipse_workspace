package com.zjmvn.flink;

import java.io.IOException;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
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
		DataStreamSource<String> text = env.socketTextStream(hostname, port);

		DataStream<Tuple2<String, String>> input = text.map(new MapFunction<String, Tuple2<String, String>>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Tuple2<String, String> map(String value) throws Exception {
				String[] fields = value.split(",");
				return new Tuple2<>(fields[0], fields[1]);
			}
		});

		DataStream<Tuple2<String, Long>> result = input.keyBy(0).process(new CountWithTimeoutFunction());

		result.print("Results:").setParallelism(1);

		env.execute("Keyed Process Function Example");
		// flink run -c com.zjmvn.flink.JobKeyedProcessFunctionCount \
		// /tmp/target_jars/zj-mvn-demo.jar
	}

	/**
	 * The implementation of the ProcessFunction that maintains the count and
	 * timeouts
	 */
	private static class CountWithTimeoutFunction
			extends KeyedProcessFunction<Tuple, Tuple2<String, String>, Tuple2<String, Long>> {

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
		public void processElement(Tuple2<String, String> value,
				KeyedProcessFunction<Tuple, Tuple2<String, String>, Tuple2<String, Long>>.Context ctx,
				Collector<Tuple2<String, Long>> out) throws Exception {
			CountWithTimestamp current = this.state.value();
			if (current == null) {
				LOG.info("Set state for key:{}", value.f0);
				current = new CountWithTimestamp();
				current.key = value.f0;
			}
			current.count++;
			// set the state's timestamp to the record's assigned event time timestamp
			current.lastModified = ctx.timestamp();
			this.state.update(current);

			// schedule the next timer 60 seconds from the current event time
			ctx.timerService().registerEventTimeTimer(current.lastModified + interval);
		}

		@Override
		public void onTimer(long timestamp, OnTimerContext ctx, Collector<Tuple2<String, Long>> out)
				throws IOException {
			LOG.info("onTimer callback for ts:{}", timestamp);

			CountWithTimestamp result = this.state.value();
			if (timestamp == result.lastModified + interval) {
				LOG.info("onTimer callback, ontput for ts:{}", timestamp);
				out.collect(new Tuple2<String, Long>(result.key, result.count));
			}
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
