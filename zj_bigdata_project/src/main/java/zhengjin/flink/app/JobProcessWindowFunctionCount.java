package zhengjin.flink.app;

import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobProcessWindowFunctionCount {

	private static Logger LOG = LoggerFactory.getLogger(JobProcessWindowFunctionCount.class);

	public static void main(String[] args) throws Exception {

		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

		DataStream<Tuple2<String, Long>> input = env.addSource(new MyTupleSource());

		DataStream<String> result = input.keyBy(t -> t.f0).timeWindow(Time.seconds(3L))
				.process(new MyProcessWindowFunction());

		result.print("WindowState:");

		env.execute("Process Window Function Example");
		// flink run -c com.zjmvn.flink.JobProcessWindowFunction \
		// /tmp/target_jars/zj-mvn-demo.jar

		// output:
		// WindowState:> key:key1, window:[9000:12000), count:4
		// WindowState:> key:key2, window:[9000:12000), count:2
		// WindowState:> key:key1, window:[12000:15000), count:4
		// WindowState:> key:key2, window:[12000:15000), count:3
	}

	private static class MyProcessWindowFunction
			extends ProcessWindowFunction<Tuple2<String, Long>, String, String, TimeWindow> {

		private static final long serialVersionUID = 1L;

		@Override
		public void process(String key,
				ProcessWindowFunction<Tuple2<String, Long>, String, String, TimeWindow>.Context ctx,
				Iterable<Tuple2<String, Long>> input, Collector<String> out) throws Exception {
			LOG.debug("key:{}, window process:{}", key, ctx.window());

			long count = 0;
			Iterator<Tuple2<String, Long>> iter = input.iterator();
			while (iter.hasNext()) {
				iter.next();
				count++;
			}
			out.collect(String.format("key:%s, window:%s, count:%d", key, ctx.window(), count));
		}
	}

	private static class MyTupleSource implements SourceFunction<Tuple2<String, Long>> {

		private static final long serialVersionUID = 1L;
		private boolean running = true;

		@Override
		public void run(SourceContext<Tuple2<String, Long>> ctx) throws Exception {
			Random rand = new Random();

			while (this.running) {
				long number = rand.nextInt(10000);
				String key = number % 2 == 0 ? "key1" : "key2";
				Tuple2<String, Long> t = new Tuple2<String, Long>(key, number);
				ctx.collect(t);
				LOG.debug("create source: {}", t);
				TimeUnit.MILLISECONDS.sleep(300 + rand.nextInt(500));
			}
		}

		@Override
		public void cancel() {
			this.running = false;
		}
	}

}
