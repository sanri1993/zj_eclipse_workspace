package zhengjin.flink.app;

import org.apache.flink.streaming.api.functions.timestamps.BoundedOutOfOrdernessTimestampExtractor;
import org.apache.flink.streaming.api.windowing.time.Time;

/**
 * Assigns timestamps to SensorReadings based on their internal timestamp and
 * emits watermarks with 5 seconds slack.
 */
public class SensorTimeAssigner extends BoundedOutOfOrdernessTimestampExtractor<SensorReading> {

	private static final long serialVersionUID = 1L;

	/**
	 * Configures the extractor with 5 seconds out-of-order interval.
	 */
	public SensorTimeAssigner() {
		super(Time.seconds(5L));
	}

	/**
	 * Extracts timestamp from SensorReading.
	 *
	 * @param r sensor reading
	 * @return the timestamp of the sensor reading.
	 */
	@Override
	public long extractTimestamp(SensorReading element) {
		return element.timestamp;
	}

}
