package zhengjin.netty.app;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.codec.http.HttpVersion;

public final class HttpClient {

	public void connect(String host, int port)
			throws InterruptedException, URISyntaxException, UnsupportedEncodingException {
		EventLoopGroup workerGroup = new NioEventLoopGroup();

		try {
			Bootstrap b = new Bootstrap();
			b.group(workerGroup);
			b.channel(NioSocketChannel.class);
			b.option(ChannelOption.SO_KEEPALIVE, true);

			b.handler(new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
					// 客户端接收到的是httpResponse响应, 所以要使用HttpResponseDecoder进行解码
					ch.pipeline().addLast(new HttpResponseDecoder());
					// 客户端发送的是httprequest, 所以要使用HttpRequestEncoder进行编码
					ch.pipeline().addLast(new HttpRequestEncoder());
					ch.pipeline().addLast(new HttpClientInboundHandler());
				}
			});

			// 构建http请求
			URI uri = new URI(String.format("http://%s:%d", host, port));
			String msg = "Hello from Netty Client";
			DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET,
					uri.toASCIIString(), Unpooled.wrappedBuffer(msg.getBytes("UTF-8")));

			request.headers().add(HttpHeaderNames.HOST, host);
			request.headers().add(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
			request.headers().add(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes());

			// Start the client
			ChannelFuture f = b.connect(host, port).sync();
			f.channel().write(request);
			f.channel().flush();
			f.channel().closeFuture().sync();
		} finally {
			workerGroup.shutdownGracefully();
		}
	}

	public static void main(String[] args) throws Exception {
		HttpClient client = new HttpClient();
		client.connect("127.0.0.1", 8884);
	}

}
