package org.nikki.mumble.event.user;

import org.nikki.mumble.Mumble;
import org.nikki.mumble.model.MumbleChannel;
import org.nikki.mumble.model.MumbleUser;
import org.nikkii.eventhub.event.Event;

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
