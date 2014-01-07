package org.nikkii.mumble.event.channel;

import org.nikkii.eventhub.event.Event;
import org.nikkii.mumble.Mumble;
import org.nikkii.mumble.model.MumbleChannel;

public class ChannelUpdateEvent implements Event {

	private Mumble mumble;
	private MumbleChannel channel;

	public ChannelUpdateEvent(Mumble mumble, MumbleChannel channel) {
		this.mumble = mumble;
		this.channel = channel;
	}

	public Mumble getMumble() {
		return mumble;
	}

	public MumbleChannel getChannel() {
		return channel;
	}

}
