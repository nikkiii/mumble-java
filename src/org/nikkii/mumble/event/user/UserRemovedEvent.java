package org.nikkii.mumble.event.user;

import org.nikkii.eventhub.event.Event;
import org.nikkii.mumble.Mumble;
import org.nikkii.mumble.model.MumbleUser;

public class UserRemovedEvent implements Event {

	private Mumble mumble;
	private MumbleUser user;

	public UserRemovedEvent(Mumble mumble, MumbleUser user) {
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
