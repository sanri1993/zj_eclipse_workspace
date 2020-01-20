package zhengjin.flink.app;

import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * Program demonstrating a rolling sum.
 */
public class JobRollingSum {

	public static void main(String[] args) throws Exception {

		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

		DataStream<Tuple3<Integer, Integer, Integer>> inputStream = env.fromElements(Tuple3.of(1, 2, 2),
				Tuple3.of(2, 3, 1), Tuple3.of(2, 2, 4), Tuple3.of(1, 5, 3));

		DataStream<Tuple3<Integer, Integer, Integer>> resultStream = inputStream
				// key on first field of tuples
				.keyBy(0)
				// sum the second field of the tuple
				.sum(1);

		resultStream.print().setParallelism(1);

		env.execute("Rolling Sum Example");
		// flink run -c zhengjin.flink.app.JobRollingSum \
		// /tmp/flink_test/zj-bigdata-app.jar

		// output:
		// (1,2,2)
		// (2,3,1)
		// (2,5,1)
		// (1,7,2)
	}

}
