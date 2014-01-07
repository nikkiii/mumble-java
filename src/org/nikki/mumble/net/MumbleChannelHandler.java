package org.nikki.mumble.net;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.IOException;

import org.nikki.mumble.Mumble;

public class MumbleChannelHandler extends ChannelInboundHandlerAdapter {
	
	private Mumble mumble;

	public MumbleChannelHandler(Mumble mumble) {
		this.mumble = mumble;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) {
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) {
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws IOException {
		if(msg instanceof MumbleUDPMessage) {
			mumble.getProtocol().processUdp((MumbleUDPMessage) msg);
		} else if(msg instanceof MumbleMessage) {
			mumble.getProtocol().processTcp((MumbleMessage) msg);
		}
	}

}
