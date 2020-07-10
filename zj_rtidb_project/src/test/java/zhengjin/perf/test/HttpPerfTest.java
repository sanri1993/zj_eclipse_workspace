package zhengjin.perf.test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import zhengjin.fl.pipeline.http.HttpUtils;
import zhengjin.perf.test.io.NioUtils;

public final class HttpPerfTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(HttpPerfTest.class);
	private static final AtomicInteger WRITE_COUNT = new AtomicInteger(0);
	private static final AtomicInteger COUNT = new AtomicInteger(0);
	private static final AtomicInteger FAILED = new AtomicInteger(0);

	private static boolean isRun = false;
	private static boolean isWait = true;

	private final String host = "127.0.0.1:17891";
	private String path;

	@Before
	public void before() {
		if (isWait) {
			// 当服务端mock等待时间 瓶颈在服务端 则http与nio的吞吐量相当
			// 执行30s,2线程,mock等待时间5-10ms 总吞吐量为3.5k
			path = "/mocktest/one/4?wait=%d&unit=milli";
		} else {
			// 当服务端没有mock等待时间 瓶颈在加压的客户端
			// 执行30s,2线程 http总吞吐量为9W nio总吞吐量为15W
			path = "/demo/2?wait=%d&unit=milli";
		}
	}

	@Test
	public void loggerLevelTest() {
		LOGGER.trace("Sample Trace");
		LOGGER.debug("Sample Debug");
		LOGGER.info("Sample Info");
		LOGGER.warn("Sample Warn");
		LOGGER.error("Sample Error\n");

		org.apache.log4j.Logger logger4j = org.apache.log4j.Logger.getRootLogger();
		logger4j.setLevel(org.apache.log4j.Level.toLevel("error"));

		LOGGER.trace("Sample Trace After setLevel");
		LOGGER.debug("Sample Debug After setLevel");
		LOGGER.info("Sample Info After setLevel");
		LOGGER.warn("Sample Warn After setLevel");
		LOGGER.error("Sample Error After setLevel");
	}

	@Test
	public void httpGetPerfTest() {
		int parallelNum = 2;
		int runTime = 5;

		LOGGER.info("Running ...");
		isRun = true;
		ExecutorService pool = Executors.newFixedThreadPool(parallelNum);
		List<Future<?>> futures = new ArrayList<>();
		for (int i = 0; i < parallelNum; i++) {
			Future<?> future = pool.submit(new httpGetProcess("http://" + this.host + this.path));
			futures.add(future);
		}

		Thread schedule = new Thread(new MySchedule(pool, runTime));
		schedule.start();

		// if error, get exception from threads in pool
		for (Future<?> future : futures) {
			try {
				future.get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
		LOGGER.info("HTTP get perf test finish:");
		LOGGER.info("thoughtput=[{}], failed=[{}].", COUNT.get(), FAILED.get());
	}

	private static class httpGetProcess implements Runnable {

		String url;

		public httpGetProcess(String url) {
			this.url = url;
		}

		@Override
		public void run() {
			while (isRun && (FAILED.get() < 10)) {
				String reqUrl = String.format(this.url, (5 + new Random().nextInt(5)));
				String response = "";
				try {
					response = HttpUtils.get(reqUrl);
				} catch (IOException e) {
					FAILED.addAndGet(1);
					LOGGER.error(e.getMessage());
				}

				if (response.length() > 0) {
					COUNT.addAndGet(1);
				} else {
					FAILED.addAndGet(1);
				}
			}
		}
	}

	private static class MySchedule implements Runnable {

		ExecutorService pool;
		int runTime;

		public MySchedule(ExecutorService pool, int runTime) {
			this.pool = pool;
			this.runTime = runTime;
		}

		@Override
		public void run() {
			for (int i = 1; i <= this.runTime; i++) {
				LOGGER.info("runTime=[{}]sec, thoughtput=[{}], failed=[{}].", i, COUNT.get(), FAILED.get());
				try {
					TimeUnit.SECONDS.sleep(1L);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			LOGGER.info("Ready to shutdown");
			if (pool != null) {
				isRun = false;
				try {
					this.pool.awaitTermination(1L, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				this.pool.shutdown();
			}
		}
	}

	@Test
	public void nioGetPerfTest() throws InterruptedException, IOException {
		int writeParallelNum = 1;
		int readParallelNum = 1;
		int runTime = 5;
		String[] fields = this.host.split(":");

		StringBuffer sb = new StringBuffer();
		sb.append(String.format("GET %s HTTP/1.1\r\n", this.path));
		sb.append(String.format("Host: %s\r\n", this.host));
		sb.append("Accept: */*\r\n\r\n");

		ExecutorService pool = null;
		SocketChannel socketChannel = null;
		Selector selector = null;
		try {
			socketChannel = SocketChannel.open();
			selector = Selector.open();

			socketChannel.configureBlocking(false);
			socketChannel.connect(new InetSocketAddress(fields[0], Integer.valueOf(fields[1])));
			socketChannel.register(selector, SelectionKey.OP_CONNECT);

			LOGGER.info("Running ...");
			pool = Executors.newFixedThreadPool(writeParallelNum + readParallelNum);
			isRun = true;

			NioWriteProcess writer = new NioWriteProcess(selector, pool);
			writer.run(writeParallelNum, sb.toString());

			Thread reader = new Thread((new NioReadProcess(selector, pool)));
			reader.start();

			// TODO: use a scheduled monitor task instead of main process
			for (int i = 0; i < runTime; i++) {
				TimeUnit.SECONDS.sleep(1L);
				LOGGER.info("activeCount=[{}], blockQueueSize=[{}]", ((ThreadPoolExecutor) pool).getActiveCount(),
						((ThreadPoolExecutor) pool).getQueue().size());
				LOGGER.info("runTime=[{}]sec, write=[{}], thoughtput=[{}], failed=[{}].", i, WRITE_COUNT.get(),
						COUNT.get(), FAILED.get());
			}
		} finally {
			isRun = false;
			if (pool != null) {
				pool.awaitTermination(3, TimeUnit.SECONDS);
				pool.shutdown();
			}

			if (socketChannel != null) {
				socketChannel.close();
			}
			if (selector != null) {
				selector.close();
			}
		}

		LOGGER.info("NIO get perf test finish:");
		LOGGER.info("runTime=[{}], thoughtput=[{}], failed=[{}].", runTime, COUNT.get(), FAILED.get());
	}

	private static class NioWriteProcess {

		Selector selector;
		ExecutorService pool;

		public NioWriteProcess(Selector selector, ExecutorService pool) {
			this.selector = selector;
			this.pool = pool;
		}

		public void run(int parallelNum, String body) throws IOException {
			this.selector.select();
			Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
			while (keyIterator.hasNext()) {
				SelectionKey key = keyIterator.next();
				if (key.isConnectable()) {
					LOGGER.info("Connect ready");
					SocketChannel channel = (SocketChannel) key.channel();
					channel.configureBlocking(false);

					if (channel.finishConnect()) {
						for (int i = 0; i < parallelNum; i++) {
							this.pool.submit(new Runnable() {
								@Override
								public void run() {
									String reqBody = "";
									while (isRun && (FAILED.get() < 10)) {
										WRITE_COUNT.addAndGet(1);
										reqBody = String.format(body, (5 + new Random().nextInt(5)));
										try {
											NioUtils.write(channel, reqBody);
										} catch (IOException e) {
											LOGGER.error(e.getMessage());
										}
									}
								}
							});
						}
						channel.register(selector, SelectionKey.OP_READ);
					}
				}
				keyIterator.remove();
			}
		}
	}

	private static class NioReadProcess implements Runnable {

		Selector selector;
		ExecutorService pool;

		public NioReadProcess(Selector selector, ExecutorService pool) {
			this.selector = selector;
			this.pool = pool;
		}

		@Override
		public void run() {
			while (isRun && (FAILED.get() < 10)) {
				try {
					int readyChannels = this.selector.select(500L);
					if (readyChannels == 0) {
						continue;
					}

					Iterator<SelectionKey> keyIterator = this.selector.selectedKeys().iterator();
					while (keyIterator.hasNext()) {
						SelectionKey key = keyIterator.next();
						if (key.isReadable()) {
							SocketChannel channel = (SocketChannel) key.channel();
							channel.configureBlocking(false);

							pool.submit(new Runnable() {
								@Override
								public void run() {
									if (channel.isConnected()) {
										String response = "";
										try {
											response = NioUtils.read(channel);
											if (response.length() > 0) {
												COUNT.addAndGet(1);
											}
										} catch (IOException e) {
											LOGGER.error(e.getMessage());
											FAILED.addAndGet(1);
										}
									}
								}
							});
						}
						keyIterator.remove();
					}
				} catch (IOException e) {
					LOGGER.error(e.getMessage());
				}
			}
		}
	}

}
