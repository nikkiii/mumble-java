package org.nikkii.mumble.net;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioDatagramChannel;

import org.nikkii.mumble.Mumble;

public class MumbleUDPChannelInitializer extends ChannelInitializer<NioDatagramChannel> {

	private Mumble mumble;

	public MumbleUDPChannelInitializer(Mumble mumble) {
		this.mumble = mumble;
	}

	@Override
	protected void initChannel(NioDatagramChannel ch) throws Exception {
		ch.pipeline().addLast(new MumbleUDPPacketDecoder(mumble), new MumbleChannelHandler(mumble));
	}

}
