package zhengjin.flink.app;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.WindowedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.watermark.Watermark;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.triggers.Trigger;
import org.apache.flink.streaming.api.windowing.triggers.TriggerResult;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.util.Random;

public class JobWindowTrigger {

	private static Logger LOG = LoggerFactory.getLogger(JobWindowTrigger.class);

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

		env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
		env.getConfig().setAutoWatermarkInterval(1000L);

		DataStreamSource<String> text = env.socketTextStream(hostname, port);

		// build source
		DataStream<SensorReading> sensorData = text.map(new MapFunction<String, SensorReading>() {

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
		}).assignTimestampsAndWatermarks(new MyTimestampsAndWatermarks());

		// window process
		WindowedStream<SensorReading, String, TimeWindow> windowed = sensorData.keyBy(r -> r.id)
				.window(TumblingEventTimeWindows.of(Time.seconds(5L))).allowedLateness(Time.seconds(5L))
				.trigger(new MyEventTimeTrigger());

		// test
		DataStream<String> test = windowed.apply(new MyWindowFunction());

		test.print("WindowState:").setParallelism(1);

		env.execute("Window TimeEvent Trigger Example");
		// flink run -c com.zjmvn.flink.JobWindowTrigger \
		// /tmp/target_jars/zj-mvn-demo.jar --host ncsocket --port 9000
	}

	private static class MyTimestampsAndWatermarks implements AssignerWithPeriodicWatermarks<SensorReading> {

		private static final long serialVersionUID = 1L;

		private final long maxOutofOrderness = 5000L;
		private long currentMaxTimestamp;

		@Override
		public long extractTimestamp(SensorReading element, long previousElementTimestamp) {
			long timestamp = element.timestamp;
			this.currentMaxTimestamp = Math.max(timestamp, this.currentMaxTimestamp);
			return timestamp;
		}

		@Override
		public Watermark getCurrentWatermark() {
			return new Watermark(this.currentMaxTimestamp - this.maxOutofOrderness);
		}
	}

	private static class MyEventTimeTrigger extends Trigger<SensorReading, TimeWindow> {

		private static final long serialVersionUID = 1L;

		@Override
		public TriggerResult onElement(SensorReading element, long timestamp, TimeWindow window, TriggerContext ctx)
				throws Exception {
			LOG.info("trigger onElement, element ts:[{}], window maxTimestamp:[{}], last watermark:[{}]",
					element.timestamp, window.maxTimestamp(), ctx.getCurrentWatermark());

			if (window.maxTimestamp() <= ctx.getCurrentWatermark()) {
				// for lateness event
				LOG.info("trigger onElement: FIRE");
				return TriggerResult.FIRE;
			} else {
				// When the current watermark passes the specified time
				// Trigger.onEventTime() is called with the time specified here.
				ctx.registerEventTimeTimer(window.maxTimestamp());
				return TriggerResult.CONTINUE;
			}
		}

		@Override
		public TriggerResult onProcessingTime(long time, TimeWindow window, TriggerContext ctx) throws Exception {
			return TriggerResult.CONTINUE;
		}

		@Override
		public TriggerResult onEventTime(long time, TimeWindow window, TriggerContext ctx) throws Exception {
			LOG.info("trigger onEventTime: ts:[{}], window:[{},{})", time, window.getStart(), window.getEnd());
			if (time == window.maxTimestamp()) {
				LOG.info("trigger onEventTime: FIRE");
				return TriggerResult.FIRE;
			}
			return TriggerResult.CONTINUE;
		}

		@Override
		public void clear(TimeWindow window, TriggerContext ctx) throws Exception {
			LOG.info("trigger clear, window:[{},{})", window.getStart(), window.getEnd());
			ctx.deleteEventTimeTimer(window.maxTimestamp());
		}

		@Override
		public boolean canMerge() {
			return true;
		}

		@Override
		public void onMerge(TimeWindow window, OnMergeContext ctx) {
			LOG.info("trigger onMerge");
			long windowMaxTimestamp = window.maxTimestamp();
			if (windowMaxTimestamp > ctx.getCurrentWatermark()) {
				ctx.registerEventTimeTimer(windowMaxTimestamp);
			}
		}

		@Override
		public String toString() {
			return "MyEventTimeTrigger()";
		}
	}

	private static class MyWindowFunction implements WindowFunction<SensorReading, String, String, TimeWindow> {

		private static final long serialVersionUID = 1L;

		@Override
		public void apply(String key, TimeWindow window, Iterable<SensorReading> input, Collector<String> out)
				throws Exception {
			List<SensorReading> list = new ArrayList<>();
			Iterator<SensorReading> iter = input.iterator();
			while (iter.hasNext()) {
				list.add(iter.next());
			}

			Collections.sort(list, new Comparator<SensorReading>() {
				@Override
				public int compare(SensorReading s1, SensorReading s2) {
					return (int) (s1.timestamp - s2.timestamp);
				}
			});

			for (SensorReading r : list) {
				LOG.debug("window function process, sensor:" + r);
			}

			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String firstEventTs = format.format(list.get(0).timestamp);
			String lastEventTs = format.format(list.get(list.size() - 1).timestamp);
			String windowStart = format.format(window.getStart());
			String windowEnd = format.format(window.getEnd());
			out.collect(String.format(
					"key:[%s], window size:[%d], first event ts:[%s], last event ts:[%s], window start ts:[%s], window end ts:[%s]",
					key, list.size(), firstEventTs, lastEventTs, windowStart, windowEnd));
		}
	}

}
