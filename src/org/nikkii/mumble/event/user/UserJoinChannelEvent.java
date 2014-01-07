package org.nikkii.mumble.event.user;

import org.nikkii.eventhub.event.Event;
import org.nikkii.mumble.Mumble;
import org.nikkii.mumble.model.MumbleChannel;
import org.nikkii.mumble.model.MumbleUser;

public class UserJoinChannelEvent implements Event {

	private Mumble mumble;
	private MumbleUser user;
	private MumbleChannel channel;

	public UserJoinChannelEvent(Mumble mumble, MumbleUser user, MumbleChannel channel) {
		this.mumble = mumble;
		this.user = user;
		this.channel = channel;
	}

	public Mumble getMumble() {
		return mumble;
	}

	public MumbleUser getUser() {
		return user;
	}

	public MumbleChannel getChannel() {
		return channel;
	}

}
