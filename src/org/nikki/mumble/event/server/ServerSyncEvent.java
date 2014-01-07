package org.nikki.mumble.event.server;

import org.nikki.mumble.Mumble;
import org.nikki.mumble.model.MumbleUser;
import org.nikkii.eventhub.event.Event;

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
