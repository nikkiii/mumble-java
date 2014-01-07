package org.nikkii.mumble.event.chat;

import org.nikkii.eventhub.event.Event;
import org.nikkii.mumble.Mumble;

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
