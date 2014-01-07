package org.nikkii.mumble.event.server;

import org.nikkii.eventhub.event.Event;

import MumbleProto.Mumble.Reject.RejectType;

public class ServerRejectEvent implements Event {

	private String reason;
	private RejectType type;

	public ServerRejectEvent(String reason, RejectType type) {
		this.reason = reason;
		this.type = type;
	}

	public String getReason() {
		return reason;
	}

	public RejectType getType() {
		return type;
	}

}
