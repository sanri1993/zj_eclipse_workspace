package zhengjin.flink.app;

import java.util.Calendar;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.flink.streaming.api.functions.source.RichParallelSourceFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SensorSource extends RichParallelSourceFunction<SensorReading> {

	private static final long serialVersionUID = 1L;
	private static Logger LOG = LoggerFactory.getLogger(SensorSource.class);

	// flag indicating whether source is still running
	private boolean running = true;

	/**
	 * run() continuously emits SensorReadings by emitting them through the
	 * SourceContext.
	 */
	@Override
	public void run(SourceContext<SensorReading> srcCtx) throws Exception {
		final int size = 10;
		Random rand = new Random();

		// look up index of this parallel task
		int taskIdx = this.getRuntimeContext().getIndexOfThisSubtask();
		// initialize sensor ids and temperatures
		String[] sensorIds = new String[size];
		double[] curFTemp = new double[size];
		for (int i = 0; i < size; i++) {
			sensorIds[i] = "sensor_" + (taskIdx * 10 + i);
			curFTemp[i] = 65 + (rand.nextGaussian() * 20);
		}

		SensorReading sensor;
		while (this.running) {
			long curTime = Calendar.getInstance().getTimeInMillis();
			// emit SensorReadings
			for (int i = 0; i < size; i++) {
				// update current temperature
				curFTemp[i] += rand.nextGaussian() * 0.5;
				// emit reading
				sensor = new SensorReading(sensorIds[i], curTime, curFTemp[i]);
				LOG.debug("emit sensor: " + sensor.toString());
				srcCtx.collect(sensor);
			}
			TimeUnit.MILLISECONDS.sleep(500L);
		}
	}

	/** Cancels this SourceFunction. */
	@Override
	public void cancel() {
		this.running = false;
	}

}
