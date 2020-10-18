package zhengjin.future.app;

public class UserInfo {

	private Integer id;
	private String name;
	private Integer jobId;
	private String jobDesc;
	private Integer carId;
	private String carDesc;
	private Integer homeId;
	private String homeDesc;

	public Integer getId() {
		return id;
	}

	@Override
	public String toString() {
		return String.format("id=%d, jobId=%d, jobDesc=%s", this.id, this.jobId, this.jobDesc);
	}

	public UserInfo setId(Integer id) {
		this.id = id;
		return this;
	}

	public String getName() {
		return name;
	}

	public UserInfo setName(String name) {
		this.name = name;
		return this;
	}

	public Integer getJobId() {
		return jobId;
	}

	public UserInfo setJobId(Integer jobId) {
		this.jobId = jobId;
		return this;
	}

	public String getJobDesc() {
		return jobDesc;
	}

	public UserInfo setJobDesc(String jobDesc) {
		this.jobDesc = jobDesc;
		return this;
	}

	public Integer getCarId() {
		return carId;
	}

	public UserInfo setCarId(Integer carId) {
		this.carId = carId;
		return this;
	}

	public String getCarDesc() {
		return carDesc;
	}

	public UserInfo setCarDesc(String carDesc) {
		this.carDesc = carDesc;
		return this;
	}

	public Integer getHomeId() {
		return homeId;
	}

	public UserInfo setHomeId(Integer homeId) {
		this.homeId = homeId;
		return this;
	}

	public String getHomeDesc() {
		return homeDesc;
	}

	public UserInfo setHomeDesc(String homeDesc) {
		this.homeDesc = homeDesc;
		return this;
	}

}
