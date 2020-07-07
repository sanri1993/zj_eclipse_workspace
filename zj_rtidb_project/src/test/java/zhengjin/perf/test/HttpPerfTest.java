package zhengjin.perf.test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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

	@Test
	public void HttpGetPerfTest() throws InterruptedException {
		String url = "";
		if (isWait) {
			// 当服务端mock等待时间 瓶颈在服务端 则http与nio的吞吐量相当
			// 执行30s,2线程,mock等待时间5-10ms 总吞吐量为3.5k
			url = "http://127.0.0.1:17891/mocktest/one/4";
		} else {
			// 当服务端没有mock等待时间 瓶颈在加压的客户端
			// 执行30s,2线程 http总吞吐量为9W nio总吞吐量为15W
			url = "http://127.0.0.1:17891/demo/2";
		}

		int parallelNum = 2;
		int runTime = 30;

		LOGGER.info("Running ...");
		ExecutorService pool = null;
		try {
			pool = Executors.newFixedThreadPool(parallelNum);
			for (int i = 0; i < parallelNum; i++) {
				pool.submit(new httpGetProcess(url));
			}

			for (int i = 0; i < runTime; i++) {
				TimeUnit.SECONDS.sleep(1L);
				LOGGER.info("runTime=[{}]sec, thoughtput=[{}], failed=[{}].", i, COUNT.get(), FAILED.get());
			}
		} finally {
			if (pool != null) {
				pool.shutdown();
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
			Map<String, String> params = new HashMap<>();
			params.put("unit", "milli");

			while (FAILED.get() < 10) {
				try {
					params.put("wait", String.valueOf(5 + new Random().nextInt(5)));
					String response = HttpUtils.get(this.url, params);
					if (response.length() > 0) {
						COUNT.addAndGet(1);
					} else {
						FAILED.addAndGet(1);
					}
				} catch (IOException e) {
					FAILED.addAndGet(1);
					LOGGER.error(e.getMessage());
				}
			}
		}
	}

	@Test
	public void NioGetPerfTest() throws InterruptedException, IOException {
		String host = "127.0.0.1:17891";
		String path = "";
		if (isWait) {
			path = "/mocktest/one/4?wait=%d&unit=milli";
		} else {
			path = "/demo/2?wait=%d&unit=milli";
		}

		int writeParallelNum = 1;
		int readParallelNum = 1;
		int runTime = 30;

		StringBuffer sb = new StringBuffer();
		sb.append(String.format("GET %s HTTP/1.1\r\n", path));
		sb.append(String.format("Host: %s\r\n", host));
		sb.append("Accept: */*\r\n\r\n");

		ExecutorService pool = null;
		SocketChannel socketChannel = null;
		Selector selector = null;
		try {
			socketChannel = SocketChannel.open();
			selector = Selector.open();

			String[] fields = host.split(":");
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
					System.out.println("Connect ready");
					SocketChannel channel = (SocketChannel) key.channel();
					channel.configureBlocking(false);

					if (channel.finishConnect()) {
						for (int i = 0; i < parallelNum; i++) {
							this.pool.submit(new Runnable() {
								@Override
								public void run() {
									String reqBody = "";
									while (isRun) {
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
			while (isRun) {
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
