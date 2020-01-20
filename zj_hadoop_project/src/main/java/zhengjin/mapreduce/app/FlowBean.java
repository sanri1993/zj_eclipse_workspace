package zhengjin.mapreduce.app;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;

public class FlowBean implements Writable {

	private long upFlow;
	private long dFlow;
	private long sumFlow;

	public FlowBean() {
	}

	public FlowBean(long upFlow, long dFlow) {
		this.upFlow = upFlow;
		this.dFlow = dFlow;
		this.sumFlow = upFlow + dFlow;
	}

	public long getUpFlow() {
		return this.upFlow;
	}

	public void setUpFlow(long upFlow) {
		this.upFlow = upFlow;
	}

	public long getdFlow() {
		return this.dFlow;
	}

	public void setdFlow(long dFlow) {
		this.dFlow = dFlow;
	}

	public long getSumFlow() {
		return this.sumFlow;
	}

	public void setSumFlow(long sumFlow) {
		this.sumFlow = sumFlow;
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeLong(this.upFlow);
		out.writeLong(this.dFlow);
		out.writeLong(this.sumFlow);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		this.upFlow = in.readLong();
		this.dFlow = in.readLong();
		this.sumFlow = in.readLong();
	}

	@Override
	public String toString() {
		// for hdfs output print
		return this.upFlow + "\t" + this.dFlow + "\t" + this.sumFlow;
	}

}
