package org.nikkii.mumble.net;

import com.google.protobuf.GeneratedMessage;

public class MumbleMessage {
	private int type;
	
	private GeneratedMessage message;
	
	public MumbleMessage(int type, GeneratedMessage message) {
		this.type = type;
		this.message = message;
	}

	public int getType() {
		return type;
	}
	
	public GeneratedMessage getMessage() {
		return message;
	}
}
