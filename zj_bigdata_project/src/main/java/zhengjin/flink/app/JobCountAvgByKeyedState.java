package zhengjin.flink.app;

import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

public class JobCountAvgByKeyedState {

	public static void main(String[] args) throws Exception {

		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

		DataStream<Tuple2<Long, Long>> ds = env.fromElements(Tuple2.of(1L, 3L), Tuple2.of(2L, 2L), Tuple2.of(1L, 5L),
				Tuple2.of(2L, 12L), Tuple2.of(1L, 7L), Tuple2.of(1L, 4L));

		ds.keyBy(0).flatMap(new CountWindowAverage()).print();

		env.execute("Count Average by Keyed State Example");
		// flink run -c com.zjmvn.flink.JobCountAvgByKeyedState \
		// /tmp/target_jars/zj-mvn-demo.jar

		// output:
		// (1,4)
		// (2,7)
		// (1,5)
	}

	private static class CountWindowAverage extends RichFlatMapFunction<Tuple2<Long, Long>, Tuple2<Long, Long>> {

		private static final long serialVersionUID = 1L;

		/**
		 * The ValueState handle. The first field is the count, the second field a
		 * running sum.
		 */
		private transient ValueState<Tuple2<Long, Long>> sum;

		@Override
		public void flatMap(Tuple2<Long, Long> input, Collector<Tuple2<Long, Long>> out) throws Exception {
			// access the state value
			Tuple2<Long, Long> currentSum = sum.value();
			if (currentSum == null) {
				currentSum = Tuple2.of(0L, 0L);
			}

			currentSum.f0 += 1L;
			currentSum.f1 += input.f1;
			sum.update(currentSum);

			// if the count reaches 2, emit the average and clear the state
			if (currentSum.f0 >= 2) {
				out.collect(new Tuple2<>(input.f0, currentSum.f1 / currentSum.f0));
				sum.clear();
			}
		}

		@Override
		public void open(Configuration config) {
			ValueStateDescriptor<Tuple2<Long, Long>> descriptor = new ValueStateDescriptor<>(
					// the state name
					"average",
					// type information
					TypeInformation.of(new TypeHint<Tuple2<Long, Long>>() {
					}));
			sum = this.getRuntimeContext().getState(descriptor);
		}
	}

}
