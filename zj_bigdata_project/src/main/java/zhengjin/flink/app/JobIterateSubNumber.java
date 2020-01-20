package zhengjin.flink.app;

import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.IterativeStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class JobIterateSubNumber {

	public static void main(String[] args) throws Exception {

		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

		DataStream<Long> someIntegers = env.generateSequence(1L, 100L);

		IterativeStream<Long> iteration = someIntegers.iterate();

		DataStream<Long> minusOne = iteration.map(new MapFunction<Long, Long>() {
			private static final long serialVersionUID = 1L;

			@Override
			public Long map(Long value) throws Exception {
				return value - 2;
			}
		});

		DataStream<Long> stillGreaterThanZero = minusOne.filter(new FilterFunction<Long>() {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean filter(Long value) throws Exception {
				return value > 0;
			}
		});

		// continuously subtracts 1 from a series of integers until they reach zero
		iteration.closeWith(stillGreaterThanZero);

		DataStream<Long> lessThanZero = minusOne.filter(new FilterFunction<Long>() {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean filter(Long value) throws Exception {
				return value <= 0;
			}
		});

		lessThanZero.print("IteratorValue:");

		env.execute("Streaming Iteration Sub Number Example");
		// flink run -c com.zjmvn.flink.JobIterateSubNumber \
		// /tmp/target_jars/zj-mvn-demo.jar
	}

}
