package zhengjin.future.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CompletableFutureApp {

	private static ExecutorService pool;

	public static void main(String[] args) {

		String runFlag = "v2";
		QueryUserService queryUserService = new QueryUserService();

		List<UserInfo> userInfoList = Collections.synchronizedList(new ArrayList<>());
		for (int i = 0; i < 20; i++) {
			UserInfo userInfo = new UserInfo();
			userInfo.setId(i);
			userInfo.setName("username" + i);
			userInfo.setCarId(i);
			userInfo.setJobId(i);
			userInfo.setHomeId(i);
			userInfoList.add(userInfo);
		}

		long start = System.currentTimeMillis();
		Stream<UserInfo> s;
		if ("v1".equals(runFlag)) {
			pool = Executors.newFixedThreadPool(3);
			s = userInfoList.stream().map(userInfo -> {
				return queryUserService.converUserInfoV1(pool, userInfo);
			});
		} else {
			s = userInfoList.stream().map(userInfo -> {
				return queryUserService.converUserInfoV2(userInfo);
			});
		}
		s.collect(Collectors.toList());

		System.out.printf("\nExec time: %d ms\n", (System.currentTimeMillis() - start));
		System.out.println("Users info:");
		userInfoList.forEach(userInfo -> {
			System.out.println(userInfo);
		});

		if ("v1".equals(runFlag)) {
			if (pool != null) {
				pool.shutdown();
			}
		}
		System.out.println("Future demo done");
	}

}
