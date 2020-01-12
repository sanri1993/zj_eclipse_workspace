package com.zjmvn.flink;

import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.WindowedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;

/**
 * Example program to demonstrate keyed transformation functions: keyBy, reduce.
 */
public class MainKeyedTransformations {

	public static void main(String[] args) throws Exception {

		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

		env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
		env.getConfig().setAutoWatermarkInterval(1000L);

		DataStream<SensorReading> readings = env.addSource(new SensorSource())
				.assignTimestampsAndWatermarks(new SensorTimeAssigner());

		KeyedStream<SensorReading, String> keyed = readings.keyBy(r -> r.id);
		WindowedStream<SensorReading, String, TimeWindow> windowed = keyed.timeWindow(Time.seconds(1L));

		// a rolling reduce that computes the highest temperature of each sensor and the
		// corresponding timestamp
		DataStream<SensorReading> maxTempPerSensor = windowed.reduce((r1, r2) -> {
			if (r1.temperature > r2.temperature) {
				return r1;
			} else {
				return r2;
			}
		});

		maxTempPerSensor.print();

		env.execute("Keyed Transformations Example");
	}

}
