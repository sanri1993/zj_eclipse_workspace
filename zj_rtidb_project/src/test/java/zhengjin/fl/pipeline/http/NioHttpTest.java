package zhengjin.fl.pipeline.http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public final class NioHttpTest {

	private static ExecutorService pool;

	private final String host = "127.0.0.1:17891";
	private final String path = "/demo/2?userid=xxx&username=xxx";

	@BeforeClass
	public static void before() {
		pool = Executors.newCachedThreadPool();
	}

	@AfterClass
	public static void after() {
		if (pool != null) {
			pool.shutdown();
		}
	}

	@Test
	public void syncNioHttpPostTest() throws InterruptedException {
		SocketChannel socketChannel = null;
		try {
			// 建立通道 此时并未连接网络
			socketChannel = SocketChannel.open();
			// 设置SocketChannel的阻塞模式 这里是阻塞模式
			// socketChannel默认阻塞 可以不设置
			socketChannel.configureBlocking(true);
			// 建立连接 此时才真正的连上网络
			socketChannel.connect(new InetSocketAddress("127.0.0.1", 17891));
			Assert.assertTrue(socketChannel.isConnected());

			// json
			JSONObject serverInfo = new JSONObject();
			serverInfo.put("server_name", "svr_a_002");
			serverInfo.put("server_ip", "127.0.1.2");

			JSONArray serverInfos = new JSONArray();
			serverInfos.add(serverInfo);

			JSONObject json = new JSONObject();
			json.put("server_group_id", "svr_grp_001");
			json.put("server_list", serverInfos);
			String jsonStr = JSONObject.toJSONString(json);

			// write
			StringBuffer sb = new StringBuffer();
			sb.append("POST /demo/3 HTTP/1.1\r\n");
			sb.append("Host: 127.0.0.1:17891\r\n");
			sb.append("Content-Type: application/json;charset=utf-8\r\n");
			sb.append(String.format("Content-Length: %d", jsonStr.length()));
			sb.append("\r\n\r\n");
			sb.append(jsonStr);
			sb.append("\r\n\r\n");

			ByteBuffer writeBuffer = ByteBuffer.wrap(sb.toString().getBytes());
			while (writeBuffer.hasRemaining()) {
				socketChannel.write(writeBuffer);
			}
			writeBuffer.clear();

			// read
			ByteBuffer readBuffer = ByteBuffer.allocate(1024);
			int read = 0;
			// 当socketChannel没有数据时 线程阻塞
			while ((read = socketChannel.read(readBuffer)) > 0) {
				System.out.println(new String(readBuffer.array(), 0, read, "UTF-8"));
				readBuffer.clear();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (socketChannel != null) {
				try {
					socketChannel.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Test
	public void asyncNioHttpGetTest() {
		String[] fields = this.host.split(":");

		try (SocketChannel socketChannel = SocketChannel.open(); Selector selector = Selector.open();) {
			// 非阻塞通道
			socketChannel.configureBlocking(false);
			socketChannel.connect(new InetSocketAddress(fields[0], Integer.valueOf(fields[1])));

			// SelectionKey.OP_CONNECT 连接就绪
			// SelectionKey.OP_READ 读就绪
			// SelectionKey.OP_WRITE 写就绪
			// SelectionKey.OP_ACCEPT 接受就绪
			socketChannel.register(selector, SelectionKey.OP_CONNECT);

			Boolean flag = true;
			while (flag) {
				// selector选择就绪的通道
				// selector.select(); 没有就绪通道时会阻塞
				// selector.selectNow(); 没有就绪通道直接立即返回0
				// selector.select(long timeout); 没有就绪通道 超时后立即返回0
				int readyChannels = selector.select(300L);
				if (readyChannels == 0) {
					continue;
				}

				Set<SelectionKey> selectedKeys = selector.selectedKeys();
				Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
				while (keyIterator.hasNext()) {
					SelectionKey selectionKey = keyIterator.next();
					if (selectionKey.isConnectable()) {
						System.out.println("Connect ready");
						SocketChannel channel = (SocketChannel) selectionKey.channel();
						channel.configureBlocking(false);

						if (channel.finishConnect()) {
							write(channel, this.path, this.host);
							channel.register(selector, SelectionKey.OP_READ);
						}
					} else if (selectionKey.isReadable()) {
						System.out.println("Read ready");
						SocketChannel channel = (SocketChannel) selectionKey.channel();
						channel.configureBlocking(false);

						if (channel.isConnected()) {
							read(channel);
						}
						// 完成一次读写操作 退出循环
						flag = false;
					}
					keyIterator.remove();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void asyncNioHttpGetTest02() {
		String[] fields = this.host.split(":");

		try (SocketChannel socketChannel = SocketChannel.open(); Selector selector = Selector.open();) {
			socketChannel.configureBlocking(false);
			socketChannel.connect(new InetSocketAddress(fields[0], Integer.valueOf(fields[1])));
			socketChannel.register(selector, SelectionKey.OP_CONNECT);

			// write
			selector.select(); // blocked
			Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
			System.out.println("Write loop ...");
			while (keyIterator.hasNext()) {
				SelectionKey selectionKey = keyIterator.next();
				if (selectionKey.isConnectable()) {
					System.out.println("Connect ready");
					SocketChannel channel = (SocketChannel) selectionKey.channel();
					channel.configureBlocking(false);

					if (channel.finishConnect()) { // do connect
						for (int i = 0; i < 3; i++) {
							write(channel, this.path, this.host);
						}
						channel.register(selector, SelectionKey.OP_READ);
						break;
					}
					keyIterator.remove();
				}
			}

			// read
			selector.select(); // blocked
			keyIterator = selector.selectedKeys().iterator();
			while (keyIterator.hasNext()) {
				SelectionKey selectionKey = keyIterator.next();
				if (selectionKey.isReadable()) {
					System.out.println("Read ready");
					SocketChannel channel = (SocketChannel) selectionKey.channel();
					channel.configureBlocking(false);

					if (channel.isConnected()) {
						read(channel);
					}
					keyIterator.remove();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void asyncNioHttpGetTest03() {
		// NIO without event register
		String[] fields = this.host.split(":");

		try (SocketChannel socketChannel = SocketChannel.open(); Selector selector = Selector.open();) {
			socketChannel.configureBlocking(false);
			socketChannel.connect(new InetSocketAddress(fields[0], Integer.valueOf(fields[1])));

			// write
			if (socketChannel.finishConnect()) { // do connect
				for (int i = 0; i < 3; i++) {
					write(socketChannel, this.path, this.host);
				}
			}

			// read
			if (socketChannel.isConnected()) {
				read(socketChannel);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void asyncNioMultiHttpGetTest() throws InterruptedException {
		final String host = "127.0.0.1:17891";
		final String path = "/mocktest/one/4?wait=200&unit=milli";
		String[] fields = host.split(":");

		try (SocketChannel socketChannel = SocketChannel.open(); Selector selector = Selector.open();) {
			socketChannel.configureBlocking(false);
			socketChannel.connect(new InetSocketAddress(fields[0], Integer.valueOf(fields[1])));
			socketChannel.register(selector, SelectionKey.OP_CONNECT); // => key.isConnectable()

			HttpHandler handler = new HttpHandler(host, path);
			while (true) {
				System.out.println("Loop ...");
				int readyChannels = selector.select(500L); // => selector.selectedKeys()
				if (readyChannels == 0) {
					continue;
				}

				Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
				while (keyIterator.hasNext()) {
					System.out.println("SelectedKeys loop ...");
					SelectionKey key = keyIterator.next();
					if (key.isConnectable()) {
						handler.handleConnect(key);
					} else if (key.isReadable()) {
						handler.handleRead(key);
					}
					keyIterator.remove();
				}
				System.out.println("Active threads count: " + ((ThreadPoolExecutor) pool).getActiveCount());

				TimeUnit.MILLISECONDS.sleep(50L);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static class HttpHandler {

		String host;
		String path;

		public HttpHandler(String host, String path) {
			this.host = host;
			this.path = path;
		}

		public void handleConnect(SelectionKey key) throws IOException {
			System.out.println("Connect ready");
			SocketChannel channel = (SocketChannel) key.channel();
			channel.configureBlocking(false);

			final int parallelNum = 2;
			final int loopNum = 5;
			if (channel.finishConnect()) {
				for (int i = 0; i < parallelNum; i++) {
					pool.submit(new Runnable() {
						@Override
						public void run() {
							try {
								for (int i = 0; i < loopNum; i++) {
									write(channel, path, host);
									TimeUnit.MILLISECONDS.sleep(100L);
								}
							} catch (IOException | InterruptedException e) {
								e.printStackTrace();
							}
						}
					});
				}
				channel.register(key.selector(), SelectionKey.OP_READ); // => key.isReadable()
			}
		}

		public void handleRead(SelectionKey key) throws IOException {
			System.out.println("Read ready");
			SocketChannel channel = (SocketChannel) key.channel();
			channel.configureBlocking(false);

			if (channel.isConnected()) {
				pool.submit(new Runnable() {
					@Override
					public void run() {
						try {
							read(channel);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});
			}
		}
	}

	/**
	 * 向通道内写入数据（client to service）
	 * 
	 * @param socketChannel
	 * @throws IOException
	 */
	private static void write(SocketChannel socketChannel, String path, String host) throws IOException {
		if (!socketChannel.isConnected()) {
			return;
		}

		StringBuffer sb = new StringBuffer();
		sb.append(String.format("GET %s HTTP/1.1\r\n", path));
		sb.append(String.format("Host: %s\r\n", host));
		sb.append("Accept: */*\r\n");
		sb.append("\r\n");

		String tag = String.format("[%s]: ", Thread.currentThread().getId());
		System.out.println(tag + "Write to channel: " + sb.toString());

		ByteBuffer writeBuffer = ByteBuffer.wrap(sb.toString().getBytes());
		while (writeBuffer.hasRemaining()) {
			socketChannel.write(writeBuffer);
		}
	}

	/**
	 * 从通道内读取数据（server to client）
	 * 
	 * @param socketChannel
	 * @throws IOException
	 */
	private static void read(SocketChannel socketChannel) throws IOException {
		if (!socketChannel.isConnected()) {
			return;
		}

		ByteBuffer readBuffer = ByteBuffer.allocate(1024);
		StringBuffer result = new StringBuffer();

		int read = 0;
		while ((read = socketChannel.read(readBuffer)) > 0) {
			result.append(new String(readBuffer.array(), 0, read, "UTF-8"));
			readBuffer.clear();
		}

		String tag = String.format("[%s]: ", Thread.currentThread().getId());
		System.out.println(tag + "Read from channel: " + result.toString());
	}

}
