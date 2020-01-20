package zhengjin.flink.app;

/**
 * POJO representing an alert.
 */
public class Alert {

	public String message;
	public long timestamp;

	/**
	 * Empty default constructor to comply with Flink's POJO requirements.
	 */
	public Alert() {
	}

	public Alert(String message, long timestamp) {
		this.message = message;
		this.timestamp = timestamp;
	}

	@Override
	public String toString() {
		return "(" + message + ", " + timestamp + ")";
	}

}
