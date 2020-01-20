package zhengjin.flink.app;

import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.CoProcessFunction;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This example shows how to use a CoProcessFunction and Timers.
 */
public class JobCoProcessFunctionTimers {

	private static Logger LOG = LoggerFactory.getLogger(JobCoProcessFunctionTimers.class);

	public static void main(String[] args) throws Exception {

		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

		env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime);

		// switch messages disable filtering of sensor readings for a specific amount of
		// time
		DataStream<Tuple2<String, Long>> filterSwitches = env.fromElements(
				// forward readings of sensor_2 for 10 seconds
				Tuple2.of("sensor_2", 3_000L),
				// forward readings of sensor_7 for 1 minute
				Tuple2.of("sensor_7", 10_000L),
				// update forward readings of sensor_2
				Tuple2.of("sensor_2", 5_000L));

		// ingest sensor stream
		DataStream<SensorReading> readings = env.addSource(new SensorSource());

		DataStream<SensorReading> forwardedReadings = readings
				// connect readings and switches
				.connect(filterSwitches)
				// key by sensor ids
				.keyBy(r -> r.id, s -> s.f0)
				// apply filtering CoProcessFunction
				.process(new ReadingFilter());

		forwardedReadings.print();

		env.execute("Filter sensor readings");
		// flink run -c com.zjmvn.flink.JobCoProcessFunctionTimers \
		// /tmp/target_jars/zj-mvn-demo.jar

		// output:
		// (sensor_2, 1578929799882, 120.19745490123705)
		// (sensor_7, 1578929799882, 60.46768340488465)
		// (sensor_2, 1578929800388, 120.83026453956776)
		// (sensor_7, 1578929800388, 60.69115460017668)
		// ...
		// (sensor_7, 1578929808903, 57.7631544864944)
		// (sensor_7, 1578929809404, 57.835212750396835)
	}

	private static class ReadingFilter extends CoProcessFunction<SensorReading, Tuple2<String, Long>, SensorReading> {

		private static final long serialVersionUID = 1L;

		// switch to enable forwarding
		private ValueState<Boolean> forwardingEnabled;
		// timestamp to disable the currently active timer
		private ValueState<Long> disableTimer;

		@Override
		public void open(Configuration parameters) throws Exception {
			this.forwardingEnabled = this.getRuntimeContext()
					.getState(new ValueStateDescriptor<>("filterSwitch", Types.BOOLEAN));
			this.disableTimer = this.getRuntimeContext().getState(new ValueStateDescriptor<>("timer", Types.LONG));
		}

		@Override
		public void processElement1(SensorReading r, Context ctx, Collector<SensorReading> out) throws Exception {
			// check if we need to forward the reading
			Boolean forward = this.forwardingEnabled.value();
			if (forward != null && forward) {
				out.collect(r);
			}
		}

		@Override
		public void processElement2(Tuple2<String, Long> s, Context ctx, Collector<SensorReading> out)
				throws Exception {
			// enable forwarding of readings
			this.forwardingEnabled.update(true);
			// set timer to disable switch
			long timerTimestamp = ctx.timerService().currentProcessingTime() + s.f1;

			Long curTimerTimestamp = disableTimer.value();
			if (curTimerTimestamp == null || timerTimestamp > curTimerTimestamp) {
				if (curTimerTimestamp != null) {
					LOG.info("key:{}, remove current timer: {}", s.f0, curTimerTimestamp);
					ctx.timerService().deleteProcessingTimeTimer(curTimerTimestamp);
				}
				LOG.info("key:{}, register new timer: {}", s.f0, timerTimestamp);
				ctx.timerService().registerProcessingTimeTimer(timerTimestamp); // callback onTimer()
				this.disableTimer.update(timerTimestamp);
			}
		}

		@Override
		public void onTimer(long ts, OnTimerContext ctx, Collector<SensorReading> out) throws Exception {
			LOG.info("remove all state of timer: {}", ts);
			this.forwardingEnabled.clear();
			this.disableTimer.clear();
		}
	}

}
