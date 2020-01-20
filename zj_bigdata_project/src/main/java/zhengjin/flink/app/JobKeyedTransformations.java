package zhengjin.flink.app;

import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.WindowedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example program to demonstrate keyed transformation functions: keyBy, reduce.
 */
public class JobKeyedTransformations {

	private static Logger LOG = LoggerFactory.getLogger(JobKeyedTransformations.class);

	public static void main(String[] args) throws Exception {

		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

		env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
		env.getConfig().setAutoWatermarkInterval(1000L);

		DataStream<SensorReading> readings = env.addSource(new SensorSource())
				.assignTimestampsAndWatermarks(new SensorTimeAssigner());

		KeyedStream<SensorReading, String> keyed = readings.keyBy(r -> r.id);
		// if no timeWindow, handle by each event
		WindowedStream<SensorReading, String, TimeWindow> windowed = keyed.timeWindow(Time.seconds(3L));

		// a rolling reduce that computes the highest temperature of each sensor and the
		// corresponding timestamp
		DataStream<SensorReading> maxTempPerSensor = windowed.reduce((r1, r2) -> {
			LOG.debug("reducer, r1 ts:{}, r2 ts:{}", r1.timestamp, r2.timestamp);
			if (r1.temperature > r2.temperature) {
				return r1;
			} else {
				return r2;
			}
		});

		maxTempPerSensor.print().setParallelism(1);

		env.execute("Keyed Transformations Example");
		// flink run -c com.zjmvn.flink.JobKeyedTransformations \
		// /tmp/target_jars/zj-mvn-demo.jar
	}

}
