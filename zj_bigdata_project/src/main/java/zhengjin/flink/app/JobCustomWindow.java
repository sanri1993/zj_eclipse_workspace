package zhengjin.flink.app;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.common.typeutils.TypeSerializer;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.WindowAssigner;
import org.apache.flink.streaming.api.windowing.triggers.EventTimeTrigger;
import org.apache.flink.streaming.api.windowing.triggers.Trigger;
import org.apache.flink.streaming.api.windowing.triggers.TriggerResult;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.util.Random;

public class JobCustomWindow {

	private static Logger LOG = LoggerFactory.getLogger(JobCustomWindow.class);

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

		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

		// checkpoint every 10 seconds
		env.getCheckpointConfig().setCheckpointInterval(30_000L);
		env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
		env.getConfig().setAutoWatermarkInterval(1000L);

		// ingest sensor stream
		DataStream<SensorReading> sensorData = env.socketTextStream(hostname, port).map(new MySensorReadingMap())
				.assignTimestampsAndWatermarks(new SensorTimeAssigner());

		DataStream<Tuple4<String, Long, Long, Integer>> countsPerThirtySecs = sensorData.keyBy(r -> r.id)
				// a custom window assigner for 30 seconds tumbling windows
				.window(new ThirtySecondsWindows())
				// a custom trigger that fires early (at most) every second
				.trigger(new OneSecondIntervalTrigger())
				// count readings per window
				.process(new CountFunction());

		countsPerThirtySecs.print("WindowStateBySec:");

		LOG.info("Run custom window example");
		env.execute("Custom Window Example");
		// flink run -c com.zjmvn.flink.JobCustomWindow \
		// /tmp/target_jars/zj-mvn-demo.jar --host ncsocket --port 9000

		// input:
		// sensor_1,1579078771000,30.1
		// sensor_1,1579078773000,30.3
		// sensor_1,1579078773500,30.3
		// sensor_1,1579078780000,31.1
		// sensor_1,1579078795000,31.5
	}

	private static class MySensorReadingMap implements MapFunction<String, SensorReading> {

		private static final long serialVersionUID = 1L;

		@Override
		public SensorReading map(String value) throws Exception {
			try {
				String[] fields = value.split(",");
				String id = fields[0];
				long timestamp = Long.parseLong(fields[1]);
				double temperature = Double.parseDouble(fields[2]);
				return new SensorReading(id, timestamp, temperature);
			} catch (ArrayIndexOutOfBoundsException e) {
				LOG.error("invalid source sensor data, ArrayIndexOutOfBoundsException: " + e.getMessage());
				return new SensorReading("sensor_default", 1579078770000L, new Random().nextInt(100));
			}
		}
	}

	/**
	 * A custom window that groups events in to 30 second tumbling windows.
	 */
	private static class ThirtySecondsWindows extends WindowAssigner<Object, TimeWindow> {

		private static final long serialVersionUID = 1L;

		long windowSize = 10_000L;

		@Override
		public Collection<TimeWindow> assignWindows(Object element, long timestamp, WindowAssignerContext context) {
			LOG.info("assignWindows");

			// rounding down by 30 seconds
			long startTime = timestamp - (timestamp % windowSize);
			long endTime = startTime + windowSize;
			// emitting the corresponding time window
			return Collections.singleton(new TimeWindow(startTime, endTime));
		}

		@Override
		public Trigger<Object, TimeWindow> getDefaultTrigger(StreamExecutionEnvironment env) {
			return EventTimeTrigger.create();
		}

		@Override
		public TypeSerializer<TimeWindow> getWindowSerializer(ExecutionConfig executionConfig) {
			return new TimeWindow.Serializer();
		}

		@Override
		public boolean isEventTime() {
			return true;
		}
	}

	/**
	 * A trigger that fires early. The trigger fires at most every second.
	 */
	private static class OneSecondIntervalTrigger extends Trigger<SensorReading, TimeWindow> {

		private static final long serialVersionUID = 1L;
		private static final long interval = 1000L;

		@Override
		public TriggerResult onElement(SensorReading element, long timestamp, TimeWindow window, TriggerContext ctx)
				throws Exception {
			LOG.info("trigger onElement => watermark:[{}], window:[{},{})", ctx.getCurrentWatermark(),
					window.getStart(), window.getEnd());

			// firstSeen will be false if not set yet
			ValueState<Boolean> firstSeen = ctx
					.getPartitionedState(new ValueStateDescriptor<>("firstSeen", Types.BOOLEAN));
			// register initial timer only for first element
			if (firstSeen.value() == null) {
				// compute time for next early firing by rounding watermark to second
				long t = ctx.getCurrentWatermark() + (interval - (ctx.getCurrentWatermark() % interval));
				LOG.info("firstSeen, register event timer:[{}]", t);
				ctx.registerEventTimeTimer(t);
				// register timer for the end of the window
				// NOTE: use window.maxTimestamp() instead of window.getEnd()
				ctx.registerEventTimeTimer(window.maxTimestamp());
				firstSeen.update(true);
			}

			// Continue. Do not evaluate window per element
			return TriggerResult.CONTINUE;
		}

		@Override
		public TriggerResult onProcessingTime(long time, TimeWindow window, TriggerContext ctx) throws Exception {
			// Continue. We don't use processing time timers
			return TriggerResult.CONTINUE;
		}

		@Override
		public TriggerResult onEventTime(long time, TimeWindow window, TriggerContext ctx) throws Exception {
			LOG.info("trigger onEventTime => ts:[{}], watermark:[{}], window:[{},{})", time, ctx.getCurrentWatermark(),
					window.getStart(), window.getEnd());

			if (time == window.maxTimestamp()) {
				// final evaluation and purge window state
				LOG.info("fire and purge window:[{},{})", window.getStart(), window.getEnd());
				return TriggerResult.FIRE_AND_PURGE;
			}

			// register next early firing timer
			long t = ctx.getCurrentWatermark() + (interval - (ctx.getCurrentWatermark() % interval));
			if (t > 0 && t < window.getEnd()) {
				LOG.info("register every second event timer:[{}]", t);
				ctx.registerEventTimeTimer(t);
			}
			// fire trigger to early evaluate window
			return TriggerResult.FIRE;
		}

		@Override
		public void clear(TimeWindow window, TriggerContext ctx) throws Exception {
			// clear trigger state
			ValueState<Boolean> firstSeen = ctx
					.getPartitionedState(new ValueStateDescriptor<>("firstSeen", Types.BOOLEAN));
			firstSeen.clear();
		}
	}

	/**
	 * A window function that counts the readings per sensor and window. The
	 * function emits the sensor id, window end, item of function evaluation, and
	 * count.
	 */
	private static class CountFunction
			extends ProcessWindowFunction<SensorReading, Tuple4<String, Long, Long, Integer>, String, TimeWindow> {

		private static final long serialVersionUID = 1L;

		@Override
		public void process(String id, Context ctx, Iterable<SensorReading> readings,
				Collector<Tuple4<String, Long, Long, Integer>> out) throws Exception {
			// count readings
			int cnt = 0;
			Iterator<SensorReading> iter = readings.iterator();
			while (iter.hasNext()) {
				iter.next();
				cnt++;
			}

			// get current watermark
			long evalTime = ctx.currentWatermark();
			// emit result
			out.collect(Tuple4.of(id, ctx.window().getEnd(), evalTime, cnt));
		}
	}

}
