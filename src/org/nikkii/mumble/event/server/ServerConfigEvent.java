package org.nikkii.mumble.event.server;

import org.nikkii.eventhub.event.Event;
import org.nikkii.mumble.Mumble;
import org.nikkii.mumble.model.MumbleServerConfig;

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
