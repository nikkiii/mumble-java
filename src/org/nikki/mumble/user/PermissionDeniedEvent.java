package org.nikki.mumble.user;

import org.nikki.mumble.model.MumbleChannel;
import org.nikkii.eventhub.event.Event;

import MumbleProto.Mumble.PermissionDenied.DenyType;

public class PermissionDeniedEvent implements Event {

	private int permission;
	private MumbleChannel channel;
	private int session;
	private String reason;
	private DenyType type;

	public PermissionDeniedEvent(int permission, MumbleChannel channel, int session, String reason, DenyType type) {
		this.permission = permission;
		this.channel = channel;
		this.session = session;
		this.reason = reason;
		this.type = type;
	}

	public int getPermission() {
		return permission;
	}

	public MumbleChannel getChannel() {
		return channel;
	}

	public int getSession() {
		return session;
	}

	public String getReason() {
		return reason;
	}

	public DenyType getType() {
		return type;
	}

}
