package zhengjin.future.app;

import java.util.concurrent.TimeUnit;

public class QueryUtils {

	public String queryCar(Integer carId) {
		try {
			TimeUnit.SECONDS.sleep(1);
			System.out.printf("[%d] query car desc done\n", Thread.currentThread().getId());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return "car_desc";
	}

	public String queryJob(Integer jobId) {
		try {
			TimeUnit.SECONDS.sleep(1);
			System.out.printf("[%d] query job desc done\n", Thread.currentThread().getId());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return "job_desc";
	}

	public String queryHome(Integer homeId) {
		try {
			TimeUnit.SECONDS.sleep(1);
			System.out.printf("[%d] query home desc done\n", Thread.currentThread().getId());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return "home_desc";
	}

}
