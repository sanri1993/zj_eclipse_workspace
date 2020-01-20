package zhengjin.flink.app;

import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.CoFlatMapFunction;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple application that outputs an alert whenever there is a high risk of
 * fire. The application receives the stream of temperature sensor readings and
 * a stream of smoke level measurements. When the temperature is over a given
 * threshold and the smoke level is high, we emit a fire alert.
 */
public class JobMultiStreamTransformations {

	private static Logger LOG = LoggerFactory.getLogger(JobMultiStreamTransformations.class);

	public static void main(String[] args) throws Exception {

		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

		env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
		env.getConfig().setAutoWatermarkInterval(1000L);

		DataStream<SensorReading> tempReadings = env.addSource(new SensorSource())
				.assignTimestampsAndWatermarks(new SensorTimeAssigner());
		DataStream<SmokeLevel> smokeReadings = env.addSource(new SmokeLevelSource()).setParallelism(1);

		KeyedStream<SensorReading, String> keyedTempReadings = tempReadings.keyBy(r -> r.id);

		DataStream<Alert> alerts = keyedTempReadings.connect(smokeReadings.broadcast())
				.flatMap(new RaiseAlertFlatMap());

		alerts.print().setParallelism(1);

		env.execute("Multi-Stream Transformations Example");
		// flink run -c com.zjmvn.flink.JobMultiStreamTransformations \
		// /tmp/target_jars/zj-mvn-demo.jar
	}

	/**
	 * A CoFlatMapFunction that processes a stream of temperature readings and a
	 * control stream of smoke level events. The control stream updates a shared
	 * variable with the current smoke level. For every event in the sensor stream,
	 * if the temperature reading is above 100 degrees and the smoke level is high,
	 * a "Risk of fire" alert is generated.
	 */
	public static class RaiseAlertFlatMap implements CoFlatMapFunction<SensorReading, SmokeLevel, Alert> {

		private static final long serialVersionUID = 1L;

		private SmokeLevel smokeLevel = SmokeLevel.LOW;

		@Override
		public void flatMap1(SensorReading tempReading, Collector<Alert> out) throws Exception {
			// high chance of fire => true
			LOG.debug(String.format("smoke level:%s, temperature:%s", this.smokeLevel, tempReading.temperature));
			if (this.smokeLevel == SmokeLevel.HIGH && tempReading.temperature > 80) {
				out.collect(new Alert("Risk of fire! " + tempReading, tempReading.timestamp));
			}
		}

		@Override
		public void flatMap2(SmokeLevel smokeLevel, Collector<Alert> out) throws Exception {
			// update smoke level
			this.smokeLevel = smokeLevel;
		}
	}

}
