package org.nikkii.mumble.net;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

import org.nikkii.mumble.Mumble;

public class MumbleUDPPacketDecoder extends MessageToMessageDecoder<DatagramPacket> {
	
	private Mumble mumble;

	public MumbleUDPPacketDecoder(Mumble mumble) {
		this.mumble = mumble;
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, DatagramPacket msg, List<Object> out) throws Exception {
		ByteBuf content = msg.content();
		
		byte[] contentBytes = new byte[content.readableBytes()];
		content.readBytes(contentBytes);
		
		final byte[] buffer = mumble.cryptState.decrypt(contentBytes, contentBytes.length);

		// Decrypt might return null if the buffer was total
		// garbage.
		if (buffer == null) {
			return;
		}
		
		out.add(new MumbleUDPMessage(Unpooled.copiedBuffer(buffer)));
	}

}
