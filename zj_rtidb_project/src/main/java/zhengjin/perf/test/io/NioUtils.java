package zhengjin.perf.test.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public final class NioUtils {

	/**
	 * 向通道内写入数据（client to service）
	 */
	public static void write(SocketChannel socketChannel, String body) throws IOException {
		if (!socketChannel.isConnected()) {
			throw new IOException("Channel is not connected!");
		}

		ByteBuffer writeBuffer = ByteBuffer.wrap(body.getBytes());
		while (writeBuffer.hasRemaining()) {
			socketChannel.write(writeBuffer);
		}
	}

	public static String read(SocketChannel socketChannel) throws IOException {
		return read(socketChannel, 1024);
	}

	/**
	 * 从通道内读取数据（server to client）
	 */
	public static String read(SocketChannel socketChannel, int bufferSize) throws IOException {
		if (!socketChannel.isConnected()) {
			throw new IOException("Channel is not connected!");
		}

		ByteBuffer readBuffer = ByteBuffer.allocate(bufferSize);
		StringBuffer sb = new StringBuffer();
		int read = 0;
		while ((read = socketChannel.read(readBuffer)) > 0) {
			sb.append(new String(readBuffer.array(), 0, read, "UTF-8"));
			readBuffer.clear();
		}
		return sb.toString();
	}

}
