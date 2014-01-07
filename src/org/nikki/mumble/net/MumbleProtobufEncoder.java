package org.nikki.mumble.net;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.nikki.mumble.MessageTypes;

import MumbleProto.Mumble.Authenticate;
import MumbleProto.Mumble.ChannelState;
import MumbleProto.Mumble.CryptSetup;
import MumbleProto.Mumble.Ping;
import MumbleProto.Mumble.TextMessage;
import MumbleProto.Mumble.UserState;
import MumbleProto.Mumble.Version;

import com.google.protobuf.GeneratedMessage;

public class MumbleProtobufEncoder extends MessageToByteEncoder<GeneratedMessage> {
	
	private static final Map<Class<?>, Integer> types = new HashMap<Class<?>, Integer>();
	
	static {
		types.put(Version.class, MessageTypes.VERSION);
		types.put(Authenticate.class, MessageTypes.AUTHENTICATE);
		types.put(Ping.class, MessageTypes.PING);
		types.put(UserState.class, MessageTypes.USER_STATE);
		types.put(TextMessage.class, MessageTypes.TEXT_MESSAGE);
		types.put(CryptSetup.class, MessageTypes.CRYPT_SETUP);
		types.put(ChannelState.class, MessageTypes.CHANNEL_STATE);
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, GeneratedMessage msg, ByteBuf out) throws Exception {
		if(types.containsKey(msg.getClass())) {
			out.writeShort(types.get(msg.getClass()));
			out.writeInt(msg.getSerializedSize());
			msg.writeTo(new ByteBufOutputStream(out));
		} else {
			throw new IOException("Unable to find outgoing message type");
		}
	}

}
