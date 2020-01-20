package zhengjin.flink.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple5;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.collector.selector.OutputSelector;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.IterativeStream;
import org.apache.flink.streaming.api.datastream.SplitStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("deprecation")
public class JobIterateFibNumber {

	private static Logger LOG = LoggerFactory.getLogger(JobIterateFibNumber.class);
	private static final int BOUND = 100;

	public static void main(String[] args) throws Exception {

		// checking input parameters
		final ParameterTool params = ParameterTool.fromArgs(args);

		// obtain execution environment and set setBufferTimeout to 1 to enable
		// continuous flushing of the output buffers (lowest latency)
		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment().setBufferTimeout(1L);

		// make parameters available in the web interface
		env.getConfig().setGlobalJobParameters(params);

		// create input stream of integer pairs
		DataStream<Tuple2<Integer, Integer>> inputStream;
		if (params.has("input")) {
			inputStream = env.readTextFile(params.get("input")).map(new FibonacciInputMap());
		} else {
			LOG.info("Executing Iterate example with default input data set.");
			LOG.info("Use --input to specify file input.");
			inputStream = env.addSource(new RandomFibonacciSource());
		}

		// create an iterative data stream from the input with 5 second timeout
		IterativeStream<Tuple5<Integer, Integer, Integer, Integer, Integer>> it = inputStream.map(new InputMap())
				.iterate(5000L);

		// apply the step function to get the next Fibonacci number
		// increment the counter and split the output with the output selector
		SplitStream<Tuple5<Integer, Integer, Integer, Integer, Integer>> step = it.map(new Step())
				.split(new MySelector());

		// close the iteration by selecting the tuples that were directed to the
		// 'iterate' channel in the output selector
		it.closeWith(step.select("iterate"));

		// to produce the final output select the tuples directed to the
		// 'output' channel then get the input pairs that have the greatest iteration
		// counter on a 1 second sliding window
		DataStream<Tuple2<Tuple2<Integer, Integer>, Integer>> numbers = step.select("output").map(new OutputMap());

		// emit results
		if (params.has("output")) {
			numbers.writeAsText(params.get("output"));
		} else {
			LOG.info("Printing result to stdout. Use --output to specify output path.");
			numbers.print("FibResults:");
		}

		env.execute("Streaming Iteration Fib Number Example");
		// flink run -c com.zjmvn.flink.JobIterateFibNumber \
		// /tmp/target_jars/zj-mvn-demo.jar

		// output:
		// (7,14,5)
		// (18,37,3)
		// (3,46,3)
		// (23,32,3)
	}

	/**
	 * Generate BOUND number of random integer pairs from the range from 1 to
	 * BOUND/2.
	 */
	private static class RandomFibonacciSource implements SourceFunction<Tuple2<Integer, Integer>> {

		private static final long serialVersionUID = 1L;

		private Random rnd = new Random();
		private volatile boolean isRunning = true;
		private int counter = 0;

		@Override
		public void run(SourceContext<Tuple2<Integer, Integer>> ctx) throws Exception {

			while (isRunning && counter < BOUND) {
				int first = rnd.nextInt(BOUND / 2 - 1) + 1;
				int second = rnd.nextInt(BOUND / 2 - 1) + 1;

				LOG.debug("create source:({},{})", first, second);
				ctx.collect(new Tuple2<>(first, second));
				counter++;
				TimeUnit.SECONDS.sleep(1L);
			}
		}

		@Override
		public void cancel() {
			isRunning = false;
		}
	}

	/**
	 * Generate random integer pairs from the range from 0 to BOUND/2.
	 */
	private static class FibonacciInputMap implements MapFunction<String, Tuple2<Integer, Integer>> {

		private static final long serialVersionUID = 1L;

		@Override
		public Tuple2<Integer, Integer> map(String value) throws Exception {
			String record = value.substring(1, value.length() - 1);
			String[] splitted = record.split(",");
			return new Tuple2<>(Integer.parseInt(splitted[0]), Integer.parseInt(splitted[1]));
		}
	}

	/**
	 * Map the inputs so that the next Fibonacci numbers can be calculated while
	 * preserving the original input tuple. A counter is attached to the tuple and
	 * incremented in every iteration step.
	 */
	private static class InputMap
			implements MapFunction<Tuple2<Integer, Integer>, Tuple5<Integer, Integer, Integer, Integer, Integer>> {

		private static final long serialVersionUID = 1L;

		@Override
		public Tuple5<Integer, Integer, Integer, Integer, Integer> map(Tuple2<Integer, Integer> value)
				throws Exception {
			return new Tuple5<>(value.f0, value.f1, value.f0, value.f1, 0);
		}
	}

	/**
	 * Iteration step function that calculates the next Fibonacci number.
	 */
	private static class Step implements
			MapFunction<Tuple5<Integer, Integer, Integer, Integer, Integer>, Tuple5<Integer, Integer, Integer, Integer, Integer>> {

		private static final long serialVersionUID = 1L;

		@Override
		public Tuple5<Integer, Integer, Integer, Integer, Integer> map(
				Tuple5<Integer, Integer, Integer, Integer, Integer> value) throws Exception {
			return new Tuple5<>(value.f0, value.f1, value.f3, value.f2 + value.f3, ++value.f4);
		}
	}

	/**
	 * OutputSelector testing which tuple needs to be iterated again.
	 */
	private static class MySelector implements OutputSelector<Tuple5<Integer, Integer, Integer, Integer, Integer>> {

		private static final long serialVersionUID = 1L;

		@Override
		public Iterable<String> select(Tuple5<Integer, Integer, Integer, Integer, Integer> value) {
			List<String> output = new ArrayList<>();
			if (value.f2 < BOUND && value.f3 < BOUND) {
				output.add("iterate");
			} else {
				output.add("output");
			}
			return output;
		}
	}

	/**
	 * Giving back the input pair and the counter.
	 */
	private static class OutputMap implements
			MapFunction<Tuple5<Integer, Integer, Integer, Integer, Integer>, Tuple2<Tuple2<Integer, Integer>, Integer>> {

		private static final long serialVersionUID = 1L;

		@Override
		public Tuple2<Tuple2<Integer, Integer>, Integer> map(Tuple5<Integer, Integer, Integer, Integer, Integer> value)
				throws Exception {
			return new Tuple2<>(new Tuple2<>(value.f0, value.f1), value.f4);
		}
	}

}
