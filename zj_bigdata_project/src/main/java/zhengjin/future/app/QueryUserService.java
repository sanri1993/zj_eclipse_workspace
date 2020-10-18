package zhengjin.future.app;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class QueryUserService {

	private Supplier<QueryUtils> queryUtilsSupplier = QueryUtils::new;

	// FutureTask
	public UserInfo converUserInfoV1(ExecutorService pool, UserInfo userInfo) {
		Future<String> homeFuture = pool.submit(new Callable<String>() {
			@Override
			public String call() throws Exception {
				return queryUtilsSupplier.get().queryHome(userInfo.getHomeId());
			}
		});

		Future<String> carFuture = pool.submit(new Callable<String>() {
			@Override
			public String call() throws Exception {
				return queryUtilsSupplier.get().queryCar(userInfo.getCarId());
			}
		});

		Future<String> jobFuture = pool.submit(new Callable<String>() {
			@Override
			public String call() throws Exception {
				return queryUtilsSupplier.get().queryCar(userInfo.getJobId());
			}
		});

		try {
			userInfo.setHomeDesc(homeFuture.get());
			userInfo.setCarDesc(carFuture.get());
			userInfo.setJobDesc(jobFuture.get());
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}

		System.out.printf("[%d] query userinfo done\n", Thread.currentThread().getId());
		return userInfo;
	}

	// CompletableFuture
	public UserInfo converUserInfoV2(UserInfo userInfo) {
		QuerySuppiler querySuppilerCar = new QuerySuppiler(userInfo.getCarId(), "car", queryUtilsSupplier.get());
		CompletableFuture<String> getCarDesc = CompletableFuture.supplyAsync(querySuppilerCar);
		getCarDesc.thenAccept(new Consumer<String>() {
			@Override
			public void accept(String carDesc) {
				userInfo.setCarDesc(carDesc);
			}
		});

		QuerySuppiler querySuppilerHome = new QuerySuppiler(userInfo.getHomeId(), "home", queryUtilsSupplier.get());
		CompletableFuture<String> getHomeDesc = CompletableFuture.supplyAsync(querySuppilerHome);
		getHomeDesc.thenAccept(new Consumer<String>() {
			@Override
			public void accept(String homeDesc) {
				userInfo.setHomeDesc(homeDesc);
			}
		});

		QuerySuppiler querySuppilerJob = new QuerySuppiler(userInfo.getJobId(), "job", queryUtilsSupplier.get());
		CompletableFuture<String> getJobDesc = CompletableFuture.supplyAsync(querySuppilerJob);
		getJobDesc.thenAccept(new Consumer<String>() {
			@Override
			public void accept(String jobDesc) {
				userInfo.setJobDesc(jobDesc);
			}
		});

		CompletableFuture<Void> getUserInfo = CompletableFuture.allOf(getCarDesc, getHomeDesc, getJobDesc);
		getUserInfo.thenAccept(new Consumer<Void>() {
			@Override
			public void accept(Void result) {
				System.out.printf("[%d] query userinfo done\n", Thread.currentThread().getId());
			}
		});
		getUserInfo.join();

		return userInfo;
	}

}
