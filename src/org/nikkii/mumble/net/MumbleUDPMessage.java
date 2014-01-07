package org.nikkii.mumble.net;

import io.netty.buffer.ByteBuf;

public class MumbleUDPMessage {

	private ByteBuf message;

	public MumbleUDPMessage(ByteBuf message) {
		this.message = message;
	}
	
	public ByteBuf getMessage() {
		return message;
	}

}
