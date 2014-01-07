package org.nikki.mumble.event.chat;

import org.nikki.mumble.Mumble;
import org.nikkii.eventhub.event.Event;

public class TextMessageEvent implements Event {
	protected Mumble mumble;
	private String message;
	
	public TextMessageEvent(Mumble mumble, String message) {
		this.mumble = mumble;
		this.message = message;
	}


	public Mumble getMumble() {
		return mumble;
	}

	public String getMessage() {
		return message;
	}
}
