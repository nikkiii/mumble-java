package org.nikkii.mumble.event.chat;

import org.nikkii.mumble.Mumble;
import org.nikkii.mumble.model.MumbleUser;

public class UserTextMessageEvent extends TextMessageEvent {

	protected MumbleUser user;

	public UserTextMessageEvent(Mumble mumble, MumbleUser user, String message) {
		super(mumble, message);
		this.user = user;
	}

	public MumbleUser getUser() {
		return user;
	}

	public void reply(String string) {
		mumble.getServerHandler().sendText(user, string);
	}
	
	public void reply(String fmt, Object... args) {
		mumble.getServerHandler().sendText(user, String.format(fmt, args));
	}

}
