package org.nikki.mumble.event.server;

import org.nikki.mumble.Mumble;
import org.nikki.mumble.model.MumbleServerConfig;
import org.nikkii.eventhub.event.Event;

public class ServerConfigEvent implements Event {

	private Mumble mumble;
	private MumbleServerConfig config;

	public ServerConfigEvent(Mumble mumble, MumbleServerConfig config) {
		this.mumble = mumble;
		this.config = config;
	}

	public Mumble getMumble() {
		return mumble;
	}

	public MumbleServerConfig getConfig() {
		return config;
	}

}
