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

import org.junit.Assert;
import org.junit.Test;

public final class NioHttpTest {

	private static ExecutorService pool = Executors.newCachedThreadPool();

	@Test
	public void syncNioHttpGetTest() throws InterruptedException {
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

			// write
			StringBuffer sb = new StringBuffer();
			sb.append("GET / HTTP/1.1\r\n");
			sb.append("Host: 127.0.0.1\r\n");
			sb.append("\r\n");

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
		final String path = "/demo/2?userid=xxx&username=xxx";
		final String host = "127.0.0.1:17891";

		try (SocketChannel socketChannel = SocketChannel.open(); Selector selector = Selector.open();) {
			// 非阻塞通道
			socketChannel.configureBlocking(false);
			String[] fields = host.split(":");
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
							write(channel, path, host);
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
	public void asyncNioMultiHttpGetTest() {
		final String host = "127.0.0.1:17891";
		final String path = "/mocktest/one/4?wait=200&unit=milli";

		try (SocketChannel socketChannel = SocketChannel.open(); Selector selector = Selector.open()) {
			String[] fields = host.split(":");
			socketChannel.configureBlocking(false);
			socketChannel.connect(new InetSocketAddress(fields[0], Integer.valueOf(fields[1])));
			socketChannel.register(selector, SelectionKey.OP_CONNECT);

			HttpHandler handler;
			while (true) {
				System.out.println("Loop ...");
				// if readyChannels > 0; select keys (return from selectedKeys()) of ready
				// channels
				int readyChannels = selector.select(500L);
				if (readyChannels == 0) {
					continue;
				}

				Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
				while (keyIterator.hasNext()) {
					System.out.println("SelectedKeys loop ...");
					SelectionKey key = keyIterator.next();
					handler = new HttpHandler(key, host, path);
					if (key.isConnectable()) {
						handler.handleConnect();
					} else if (key.isReadable()) {
						handler.handleRead();
					}
					keyIterator.remove();
				}

				try {
					TimeUnit.MILLISECONDS.sleep(50L);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.println("Active threads count: " + ((ThreadPoolExecutor) pool).getActiveCount());
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			pool.shutdown();
		}
	}

	private static class HttpHandler {

		private SelectionKey key;
		String host;
		String path;

		public HttpHandler(SelectionKey key, String host, String path) {
			this.key = key;
			this.host = host;
			this.path = path;
		}

		public void handleConnect() throws IOException {
			System.out.println("Connect ready");
			SocketChannel channel = (SocketChannel) key.channel();
			channel.configureBlocking(false);

			if (channel.finishConnect()) {
				final int parallelNum = 3;
				for (int i = 0; i < parallelNum; i++) {
					pool.submit(new Runnable() {
						@Override
						public void run() {
							try {
								int loopNum = 5;
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
				channel.register(this.key.selector(), SelectionKey.OP_READ);
			}
		}

		public void handleRead() throws IOException {
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
	 * 向通道内写入数据（from client to service）
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

		ByteBuffer writeBuffer = ByteBuffer.wrap(sb.toString().getBytes());
		String tag = String.format("[%s]: ", Thread.currentThread().getId());
		System.out.println(tag + "Write to channel: " + sb.toString());
		while (writeBuffer.hasRemaining()) {
			socketChannel.write(writeBuffer);
		}
	}

	/**
	 * 从通道内读取数据（from server to client）
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
