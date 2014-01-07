package org.nikki.mumble.net;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslHandler;

import javax.net.ssl.SSLEngine;

import org.nikki.mumble.Mumble;

public class MumbleChannelInitializer extends ChannelInitializer<SocketChannel> {

	private SSLEngine engine;
	private Mumble mumble;

	public MumbleChannelInitializer(Mumble mumble, SSLEngine engine) {
		this.mumble = mumble;
		this.engine = engine;
	}

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ch.pipeline().addLast(new SslHandler(engine), new MumbleProtobufDecoder(), new MumbleProtobufEncoder(), new MumbleChannelHandler(mumble));
	}

}
