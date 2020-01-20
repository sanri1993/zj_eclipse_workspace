package zhengjin.flink.app;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

/**
 * Example program to demonstrate simple transformation functions: filter, map,
 * and flatMap.
 */
public class JobBasicTransformations {

	public static void main(String[] args) throws Exception {

		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

		// the timeCharacteristic was set to ProcessingTime by default
		env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
		env.getConfig().setAutoWatermarkInterval(1000L);

		DataStream<SensorReading> readings = env.addSource(new SensorSource())
				.assignTimestampsAndWatermarks(new SensorTimeAssigner());

		DataStream<SensorReading> filteredReadings = readings.filter(r -> r.temperature >= 70);
		DataStream<String> sensorIds = filteredReadings.map(r -> r.id);

		DataStream<String> splitIds = sensorIds.flatMap((FlatMapFunction<String, String>) (id, out) -> {
			for (String s : id.split("_")) {
				out.collect(s);
			}
		}).returns(Types.STRING);
		// DataStream<String> splitIds = sensorIds.flatMap(new IdSplitter());

		splitIds.print().setParallelism(1);

		env.execute("Basic Transformations Example");
		// flink run -c com.zjmvn.flink.JobBasicTransformations \
		// /tmp/target_jars/zj-mvn-demo.jar
	}

	/**
	 * User-defined FlatMapFunction that splits a sensor's id String into a prefix
	 * and a number.
	 */
	@SuppressWarnings("unused")
	private static class IdSplitter implements FlatMapFunction<String, String> {

		private static final long serialVersionUID = 1L;

		@Override
		public void flatMap(String value, Collector<String> out) throws Exception {
			String[] splits = value.split("_");
			for (String split : splits) {
				out.collect(split);
			}
		}
	}

}
