package org.nikkii.mumble;

import org.nikkii.mumble.model.MumbleChannel;
import org.nikkii.mumble.model.MumbleUser;

import MumbleProto.Mumble.ChannelState;
import MumbleProto.Mumble.TextMessage;
import MumbleProto.Mumble.UserState;

public class ServerHandler {
	private Mumble mumble;

	public ServerHandler(Mumble mumble) {
		this.mumble = mumble;
	}
	
	public void createChannel(MumbleChannel parent, String name, String description, int position, boolean temporary) {
		ChannelState.Builder bldr = ChannelState.newBuilder();
		bldr.setParent(parent.getId())
			.setName(name)
			.setDescription(description)
			.setPosition(position)
			.setTemporary(temporary);
		mumble.write(bldr.build());
	}
	
	public void move(MumbleUser user, MumbleChannel to) {
		UserState.Builder bldr = UserState.newBuilder();
		bldr.setSession(user.getSession());
		bldr.setChannelId(to.getId());
		mumble.write(bldr.build());
	}

	public void sendText(MumbleChannel channel, String text) {
		TextMessage.Builder b = TextMessage.newBuilder();
		b.addChannelId(channel.getId());
		b.setMessage(text);
		mumble.write(b.build());
	}
	
	public void sendText(MumbleUser user, String text) {
		TextMessage.Builder b = TextMessage.newBuilder();
		b.addSession(user.getSession());
		b.setMessage(text);
		mumble.write(b.build());
	}
}
