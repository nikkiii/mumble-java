package org.nikki.mumble.event.chat;

import org.nikki.mumble.Mumble;
import org.nikki.mumble.model.MumbleChannel;
import org.nikki.mumble.model.MumbleUser;

public class ChannelTextMessageEvent extends UserTextMessageEvent {

	private MumbleChannel channel;

	public ChannelTextMessageEvent(Mumble mumble, MumbleChannel channel, MumbleUser user, String message) {
		super(mumble, user, message);
		this.channel = channel;
	}

	public MumbleChannel getChannel() {
		return channel;
	}

	@Override
	public void reply(String string) {
		mumble.getServerHandler().sendText(user.getChannel(), string);
	}
	
	@Override
	public void reply(String fmt, Object... args) {
		mumble.getServerHandler().sendText(user.getChannel(), String.format(fmt, args));
	}

}
