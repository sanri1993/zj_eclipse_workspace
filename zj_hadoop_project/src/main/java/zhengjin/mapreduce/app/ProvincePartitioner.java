package zhengjin.mapreduce.app;

import java.util.HashMap;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Partitioner;

public class ProvincePartitioner extends Partitioner<Text, FlowBean> {

	public static HashMap<String, Integer> provinceDict = new HashMap<String, Integer>(10);

	static {
		provinceDict.put("137", 0);
		provinceDict.put("133", 1);
		provinceDict.put("138", 2);
		provinceDict.put("135", 3);
	}

	public int getPartition(Text key, FlowBean value, int numPartitions) {
		String prefix = key.toString().substring(0, 3);
		return provinceDict.getOrDefault(prefix, 4);
	}

}
