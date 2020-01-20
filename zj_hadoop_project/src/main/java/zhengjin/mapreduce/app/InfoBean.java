package zhengjin.mapreduce.app;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;

public class InfoBean implements Writable {

	// order info
	private int order_id;
	private String dateString;
	private String p_id;
	private int amount;
	// product info
	private String pname;
	private int category_id;
	private float price;

	// flag=0, order info bean
	// flag=1, product info bean
	private String flag;

	public InfoBean() {
	}

	public void set(int order_id, String dateString, String p_id, int amount, String pname, int category_id,
			float price, String flag) {
		this.order_id = order_id;
		this.dateString = dateString;
		this.p_id = p_id;
		this.amount = amount;
		this.pname = pname;
		this.category_id = category_id;
		this.price = price;
		this.flag = flag;
	}

	public int getOrder_id() {
		return this.order_id;
	}

	public void setOrder_id(int order_id) {
		this.order_id = order_id;
	}

	public String getDateString() {
		return dateString;
	}

	public void setDateString(String dateString) {
		this.dateString = dateString;
	}

	public String getP_id() {
		return this.p_id;
	}

	public void setP_id(String p_id) {
		this.p_id = p_id;
	}

	public int getAmount() {
		return this.amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public String getPName() {
		return this.pname;
	}

	public void setPName(String pname) {
		this.pname = pname;
	}

	public int getCategory_id() {
		return this.category_id;
	}

	public void setCategory_id(int category_id) {
		this.category_id = category_id;
	}

	public float getPrice() {
		return this.price;
	}

	public void setPrice(float price) {
		this.price = price;
	}

	public String getFlag() {
		return this.flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeInt(this.order_id);
		out.writeUTF(this.dateString);
		out.writeUTF(this.p_id);
		out.writeInt(this.amount);
		out.writeUTF(this.pname);
		out.writeInt(this.category_id);
		out.writeFloat(this.price);
		out.writeUTF(this.flag);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		this.order_id = in.readInt();
		this.dateString = in.readUTF();
		this.p_id = in.readUTF();
		this.amount = in.readInt();
		this.pname = in.readUTF();
		this.category_id = in.readInt();
		this.price = in.readFloat();
		this.flag = in.readUTF();
	}

	@Override
	public String toString() {
		return String.format("order_id=%d,dataString=%s,p_id=%s,amount=%d,pname=%s,category_id=%d,price=%.2f",
				this.order_id, this.dateString, this.p_id, this.amount, this.pname, this.category_id, this.price);
	}

}
