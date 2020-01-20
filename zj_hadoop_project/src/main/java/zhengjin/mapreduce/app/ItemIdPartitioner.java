package zhengjin.mapreduce.app;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Partitioner;

public class ItemIdPartitioner extends Partitioner<OrderBean, NullWritable> {

	@Override
	public int getPartition(OrderBean bean, NullWritable value, int numReduceTasks) {
		// 相同item_id发送到相同的partition, 并且分区数与reduce task数保持一致
		return (bean.getItemid().hashCode() & Integer.MAX_VALUE) % numReduceTasks;
	}

}
