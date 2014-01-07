package org.nikkii.mumble.net;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nikkii.mumble.MessageTypes;

import MumbleProto.Mumble.ChannelRemove;
import MumbleProto.Mumble.ChannelState;
import MumbleProto.Mumble.CodecVersion;
import MumbleProto.Mumble.CryptSetup;
import MumbleProto.Mumble.PermissionDenied;
import MumbleProto.Mumble.PermissionQuery;
import MumbleProto.Mumble.Reject;
import MumbleProto.Mumble.ServerConfig;
import MumbleProto.Mumble.ServerSync;
import MumbleProto.Mumble.TextMessage;
import MumbleProto.Mumble.UserRemove;
import MumbleProto.Mumble.UserState;
import MumbleProto.Mumble.Version;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.Parser;

public class MumbleProtobufDecoder extends ByteToMessageDecoder {
	
	@SuppressWarnings({ "rawtypes", "serial" })
	private static final Map<Integer, Parser> parsers = new HashMap<Integer, Parser>() {{
		put(MessageTypes.VERSION, Version.PARSER);
		put(MessageTypes.REJECT, Reject.PARSER);
		put(MessageTypes.SERVER_SYNC, ServerSync.PARSER);
		put(MessageTypes.CHANNEL_REMOVE, ChannelRemove.PARSER);
		put(MessageTypes.CHANNEL_STATE, ChannelState.PARSER);
		put(MessageTypes.USER_STATE, UserState.PARSER);
		put(MessageTypes.USER_REMOVE, UserRemove.PARSER);
		put(MessageTypes.TEXT_MESSAGE, TextMessage.PARSER);
		put(MessageTypes.PERMISSION_DENIED, PermissionDenied.PARSER);
		put(MessageTypes.CODEC_VERSION, CodecVersion.PARSER);
		put(MessageTypes.SERVER_CONFIG, ServerConfig.PARSER);
		put(MessageTypes.PERMISSION_QUERY, PermissionQuery.PARSER);
		put(MessageTypes.CRYPT_SETUP, CryptSetup.PARSER);
	}};

	private int type = -1;
	private int length = -1;

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) throws Exception {
		if(type == -1 && length == -1) {
			if(buf.readableBytes() < 6)
				return;
			
			type = buf.readShort();
			length = buf.readInt();
		} else {
			if(buf.readableBytes() < length)
				return;
			
			ByteBuf message = buf.readBytes(length);
			switch(type) {
			case MessageTypes.UDP_TUNNEL:
				out.add(new MumbleUDPMessage(message));
				break;
			default:
				Parser parser = parsers.get(type);
				if(parser != null) {
					GeneratedMessage msg = (GeneratedMessage) parser.parseFrom(new ByteBufInputStream(message,  length));
					out.add(new MumbleMessage(type, msg));
				}
				break;
			}
			type = -1;
			length = -1;
		}
	}

}
