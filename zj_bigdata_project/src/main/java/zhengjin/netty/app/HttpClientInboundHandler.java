package zhengjin.netty.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponse;

public final class HttpClientInboundHandler extends ChannelInboundHandlerAdapter {

	private final static Logger logger = LoggerFactory.getLogger(HttpClientInboundHandler.class);

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		if (msg instanceof HttpResponse) {
			HttpResponse response = (HttpResponse) msg;
			logger.info("Content Type: " + response.headers().get(HttpHeaderNames.CONTENT_TYPE));
		}

		if (msg instanceof HttpContent) {
			HttpContent content = (HttpContent) msg;
			ByteBuf buf = content.content();
			logger.info(buf.toString(io.netty.util.CharsetUtil.UTF_8));
			buf.release();
		}
	}

}
