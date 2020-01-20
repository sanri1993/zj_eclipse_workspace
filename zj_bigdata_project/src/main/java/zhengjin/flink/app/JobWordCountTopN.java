package zhengjin.flink.app;

import java.util.Comparator;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessAllWindowFunction;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.SlidingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 下面的代码实现了按照首字母分组，取每组元素count最高的TopN方法。
 * 嵌套TopN全局topN的缺陷是，由于windowall是一个全局并发为1的操作，所有的数据只能汇集到一个节点进行 TopN 的计算，
 * 那么计算能力就会受限于单台机器，容易产生数据热点问题。解决思路就是使用嵌套 TopN, 或者说两层 TopN. 在原先的 TopN 前面，再加一层
 * TopN, 用于分散热点。例如可以先加一层分组 TopN, 第一层会计算出每一组的 TopN, 而后在第二层中进行合并汇总，得到最终的全网TopN.
 * 第二层虽然仍是单点，但是大量的计算量由第一层分担了，而第一层是可以水平扩展的。
 */
public class JobWordCountTopN {

	private static Logger LOG = LoggerFactory.getLogger(JobWordCountTopN.class);

	public static void main(String[] args) throws Exception {

		boolean isGroupBy = false;
		try {
			final ParameterTool params = ParameterTool.fromArgs(args);
			isGroupBy = params.getBoolean("groupby");
		} catch (Exception e) {
			LOG.warn("No groupby is set, use default 'false'.");
		}

		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

		env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime); // 以processtime作为时间语义

		DataStream<String> text = env.socketTextStream("hostname", 1024);

		// 将输入语句split成一个一个单词并初始化count值为1的Tuple2<String, Integer>类型
		DataStream<Tuple2<String, Integer>> ds = text.flatMap(new LineSplitter());

		DataStream<Tuple2<String, Integer>> wcount = ds
				// 按照Tuple2<String, Integer>的第一个元素为key, 也就是单词
				.keyBy(0)
				// key之后的元素进入一个总时间长度为600s, 每20s向后滑动一次的滑动窗口
				.window(SlidingProcessingTimeWindows.of(Time.seconds(10L), Time.seconds(3L)))
				// 将相同的key的元素第二个count值相加
				.sum(1);

		DataStream<Tuple2<String, Integer>> ret;

		final int topN = 5;
		if (!isGroupBy) {
			ret = wcount
					// windowAll是一个全局并发为1的特殊操作，也就是所有元素都会进入到一个窗口内进行计算
					// 所有key元素进入一个20s长的窗口（选20s是因为上游窗口每20s计算一轮数据，topN窗口一次计算只统计一个窗口时间内的变化）
					.windowAll(TumblingProcessingTimeWindows.of(Time.seconds(3L)))
					// 计算该窗口TopN
					.process(new TopNAllFunction(topN));
		} else { // 分组TopN
			ret = wcount.keyBy(new TupleKeySelectorByStart())
					// 20s窗口统计上游数据
					.window(TumblingProcessingTimeWindows.of(Time.seconds(3L)))
					// 分组TopN统计
					.process(new TopNFunction(topN));
		}

		ret.print("TopNResults:").setParallelism(1);

		env.execute("TopN Example");
	}

	private static class LineSplitter implements FlatMapFunction<String, Tuple2<String, Integer>> {

		private static final long serialVersionUID = 1L;

		@Override
		public void flatMap(String value, Collector<Tuple2<String, Integer>> out) throws Exception {
			String[] tokens = value.toLowerCase().split("//W+");
			for (String token : tokens) {
				if (token.length() > 0) {
					out.collect(new Tuple2<String, Integer>(token, 1));
				}
			}
		}
	}

	private static class TopNAllFunction
			extends ProcessAllWindowFunction<Tuple2<String, Integer>, Tuple2<String, Integer>, TimeWindow> {

		private static final long serialVersionUID = 1L;

		private int topSize = 10;

		public TopNAllFunction(int topSize) {
			this.topSize = topSize;
		}

		@Override
		public void process(
				ProcessAllWindowFunction<Tuple2<String, Integer>, Tuple2<String, Integer>, TimeWindow>.Context ctx,
				Iterable<Tuple2<String, Integer>> input, Collector<Tuple2<String, Integer>> out) throws Exception {
			// treemap按照key降序排列，相同count值不覆盖
			TreeMap<Integer, Tuple2<String, Integer>> treemap = new TreeMap<Integer, Tuple2<String, Integer>>(
					new Comparator<Integer>() {
						@Override
						public int compare(Integer x, Integer y) {
							return (x > y) ? 1 : -1;
						}
					});

			for (Tuple2<String, Integer> element : input) {
				treemap.put(element.f1, element);
				if (treemap.size() > this.topSize) {
					treemap.pollLastEntry();
				}
			}

			for (Entry<Integer, Tuple2<String, Integer>> entry : treemap.entrySet()) {
				out.collect(entry.getValue());
			}
		}
	}

	private static class TupleKeySelectorByStart implements KeySelector<Tuple2<String, Integer>, String> {

		private static final long serialVersionUID = 1L;

		@Override
		public String getKey(Tuple2<String, Integer> value) throws Exception {
			return value.f0.substring(0, 1); // 取首字母做key
		}
	}

	private static class TopNFunction
			extends ProcessWindowFunction<Tuple2<String, Integer>, Tuple2<String, Integer>, String, TimeWindow> {

		private static final long serialVersionUID = 1L;

		private int topSize = 10;

		public TopNFunction(int topSize) {
			this.topSize = topSize;
		}

		@Override
		public void process(String key,
				ProcessWindowFunction<Tuple2<String, Integer>, Tuple2<String, Integer>, String, TimeWindow>.Context ctx,
				Iterable<Tuple2<String, Integer>> input, Collector<Tuple2<String, Integer>> out) throws Exception {
			TreeMap<Integer, Tuple2<String, Integer>> treemap = new TreeMap<Integer, Tuple2<String, Integer>>(
					new Comparator<Integer>() {
						@Override
						public int compare(Integer x, Integer y) {
							return (x > y) ? -1 : 1;
						}
					});

			for (Tuple2<String, Integer> element : input) {
				treemap.put(element.f1, element);
				if (treemap.size() > topSize) {
					treemap.pollLastEntry();
				}
			}

			for (Entry<Integer, Tuple2<String, Integer>> entry : treemap.entrySet()) {
				out.collect(entry.getValue());
			}
		}
	}

}
