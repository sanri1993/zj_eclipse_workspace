package zhengjin.fl.pipeline.http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

public class NioHttpTest {

	@Test
	public void syncNioHttpTest() {
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

			StringBuffer sb = new StringBuffer();
			sb.append("GET / HTTP/1.1\r\n");
			sb.append("Host: 127.0.0.1\r\n");
			sb.append("\r\n");

			// 写入数据
			ByteBuffer writeBuffer = ByteBuffer.wrap(sb.toString().getBytes());
			while (writeBuffer.hasRemaining()) {
				socketChannel.write(writeBuffer);
			}
			writeBuffer.clear();

			// 读取返回结果
			StringBuffer response = new StringBuffer();
			ByteBuffer readBuffer = ByteBuffer.allocate(1024);

			int read = 0;
			// 当socketChannel没有数据时 线程阻塞
			while ((read = socketChannel.read(readBuffer)) > 0) {
				response.append(new String(readBuffer.array(), 0, read, "UTF-8"));
				readBuffer.clear();
			}
			System.out.println("HTTP response: " + response);
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
	public void asyncNioHttpTest() throws IOException, InterruptedException {
		SocketChannel socketChannel = null;
		Selector selector = null;

		try {
			socketChannel = SocketChannel.open();
			// 非阻塞通道
			socketChannel.configureBlocking(false);
			socketChannel.connect(new InetSocketAddress("127.0.0.1", 17891));

			selector = Selector.open();

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
				// selector.select(long timeout); 没有就绪通道，超时后立即返回0
				int readyChannels = selector.select();
				if (readyChannels == 0) {
					TimeUnit.SECONDS.sleep(1L);
					continue;
				}

				Set<SelectionKey> selectedKeys = selector.selectedKeys();
				Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
				while (keyIterator.hasNext()) {
					SelectionKey key = keyIterator.next();
					if (key.isConnectable()) {
						System.out.println("Connect ready");
						SocketChannel channel = (SocketChannel) key.channel();
						channel.configureBlocking(false);

						if (channel.finishConnect()) {
							this.write(channel);
							channel.register(selector, SelectionKey.OP_READ);
						}
					} else if (key.isReadable()) {
						System.out.println("Read ready");
						SocketChannel channel = (SocketChannel) key.channel();
						channel.configureBlocking(false);

						if (channel.isConnected()) {
							this.read(channel);
//							TimeUnit.SECONDS.sleep(1L);
//							this.write(channel);
						}
						// 完成一次读写操作 退出循环
						flag = false;
					}
					keyIterator.remove();
				}
			}
		} finally {
			if (socketChannel != null) {
				socketChannel.close();
			}
			if (selector != null) {
				selector.close();
			}
		}
	}

	/**
	 * 向通道内写入数据
	 * 
	 * @param socketChannel
	 * @throws IOException
	 */
	private void write(SocketChannel socketChannel) throws IOException {
		if (!socketChannel.isConnected()) {
			return;
		}

		StringBuffer sb = new StringBuffer();
		sb.append("GET / HTTP/1.1\r\n");
		sb.append("Host: 127.0.0.1\r\n");
		sb.append("\r\n");

		ByteBuffer writeBuffer = ByteBuffer.wrap(sb.toString().getBytes());
		System.out.println("Write to channel: " + sb.toString());
		while (writeBuffer.hasRemaining()) {
			socketChannel.write(writeBuffer);
		}
	}

	/**
	 * 从通道内读取数据
	 * 
	 * @param socketChannel
	 * @throws IOException
	 */
	private void read(SocketChannel socketChannel) throws IOException {
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
		System.out.println("Read from channel: " + result.toString());
	}

}
