package org.nikki.mumble.event.user;

import org.nikki.mumble.Mumble;
import org.nikki.mumble.model.MumbleUser;
import org.nikkii.eventhub.event.Event;

public class UserUpdateEvent implements Event {

	private Mumble mumble;
	private MumbleUser user;

	public UserUpdateEvent(Mumble mumble, MumbleUser user) {
		this.mumble = mumble;
		this.user = user;
	}

	public Mumble getMumble() {
		return mumble;
	}

	public MumbleUser getUser() {
		return user;
	}
}
