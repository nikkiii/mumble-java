package org.nikkii.mumble.event.server;

import org.nikkii.eventhub.event.Event;
import org.nikkii.mumble.Mumble;
import org.nikkii.mumble.model.MumbleUser;

public class ServerSyncEvent implements Event {

	private Mumble mumble;
	private MumbleUser currentUser;

	public ServerSyncEvent(Mumble mumble, MumbleUser currentUser) {
		this.mumble = mumble;
		this.currentUser = currentUser;
	}

	public Mumble getMumble() {
		return mumble;
	}

	public MumbleUser getCurrentUser() {
		return currentUser;
	}

}
