package org.nikki.mumble.event.channel;

import org.nikki.mumble.Mumble;
import org.nikki.mumble.model.MumbleChannel;
import org.nikkii.eventhub.event.Event;

public class ChannelRemoveEvent implements Event {

	private Mumble mumble;
	private MumbleChannel channel;

	public ChannelRemoveEvent(Mumble mumble, MumbleChannel channel) {
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