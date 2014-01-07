package org.nikkii.mumble;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringEscapeUtils;
import org.nikkii.mumble.event.channel.ChannelRegisteredEvent;
import org.nikkii.mumble.event.channel.ChannelRemoveEvent;
import org.nikkii.mumble.event.channel.ChannelUpdateEvent;
import org.nikkii.mumble.event.chat.ChannelTextMessageEvent;
import org.nikkii.mumble.event.chat.TextMessageEvent;
import org.nikkii.mumble.event.chat.UserTextMessageEvent;
import org.nikkii.mumble.event.server.ServerConfigEvent;
import org.nikkii.mumble.event.server.ServerRejectEvent;
import org.nikkii.mumble.event.server.ServerSyncEvent;
import org.nikkii.mumble.event.user.UserJoinChannelEvent;
import org.nikkii.mumble.event.user.UserJoinEvent;
import org.nikkii.mumble.event.user.UserRemovedEvent;
import org.nikkii.mumble.event.user.UserUpdateEvent;
import org.nikkii.mumble.model.MumbleChannel;
import org.nikkii.mumble.model.MumbleServerConfig;
import org.nikkii.mumble.model.MumbleUser;
import org.nikkii.mumble.net.MumbleMessage;
import org.nikkii.mumble.net.MumbleUDPMessage;
import org.nikkii.mumble.user.PermissionDeniedEvent;
import org.nikkii.mumble.util.ByteBufPDS;

import MumbleProto.Mumble.ChannelRemove;
import MumbleProto.Mumble.ChannelState;
import MumbleProto.Mumble.CodecVersion;
import MumbleProto.Mumble.CryptSetup;
import MumbleProto.Mumble.PermissionDenied;
import MumbleProto.Mumble.PermissionQuery;
import MumbleProto.Mumble.Reject;
import MumbleProto.Mumble.ServerSync;
import MumbleProto.Mumble.TextMessage;
import MumbleProto.Mumble.UserRemove;
import MumbleProto.Mumble.UserState;

import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessage;

public class MumbleProtocol {

	public static final int UDPMESSAGETYPE_UDPVOICECELTALPHA = 0;
	public static final int UDPMESSAGETYPE_UDPPING = 1;
	public static final int UDPMESSAGETYPE_UDPVOICESPEEX = 2;
	public static final int UDPMESSAGETYPE_UDPVOICECELTBETA = 3;
	public static final int UDPMESSAGETYPE_UDPVOICEOPUS = 4;

	public static final int CODEC_NOCODEC = -1;
	public static final int CODEC_ALPHA = UDPMESSAGETYPE_UDPVOICECELTALPHA;
	public static final int CODEC_BETA = UDPMESSAGETYPE_UDPVOICECELTBETA;
	public static final int CODEC_OPUS = UDPMESSAGETYPE_UDPVOICEOPUS;

	public static final int SAMPLE_RATE = 48000;
	public static final int FRAME_SIZE = SAMPLE_RATE / 100;
	

	/**
	 * The time window during which the last successful UDP ping must have been
	 * transmitted. If the time since the last successful UDP ping is greater
	 * than this treshold the connection falls back on TCP tunneling.
	 *
	 * NOTE: This is the time when the last successfully received ping was SENT
	 * by the client.
	 *
	 * 6000 gives 1 second reply-time as the ping interval is 5000 seconds
	 * currently.
	 */
	public static final int UDP_PING_TRESHOLD = 6000;

	private Mumble mumble;
	
	private long lastPing = 0;
	
	private long lastLatency = 0;
	
	private int codec = CODEC_NOCODEC;
	private Channel channel;
	
	private AtomicInteger tcpPackets = new AtomicInteger(0);
	private AtomicInteger udpPackets = new AtomicInteger(0);

	public MumbleProtocol(Mumble mumble) {
		this.mumble = mumble;
		this.channel = mumble.getTCPChannel();
	}
	
	public void incrementTcpPackets() {
		tcpPackets.incrementAndGet();
	}
	
	public void incrementUdpPackets() {
		udpPackets.incrementAndGet();
	}
	
	public void processTcp(MumbleMessage message) {
		incrementTcpPackets();
		
		switch(message.getType()) {
		case MessageTypes.PING:
			// Check response time
			lastLatency = System.currentTimeMillis() - lastPing;
			break;
		case MessageTypes.VERSION:
			break;
		case MessageTypes.REJECT:
			processReject((Reject) message.getMessage());
			break;
		case MessageTypes.SERVER_SYNC:
			processServerSync((ServerSync) message.getMessage());
			break;
		case MessageTypes.CHANNEL_REMOVE:
			processChannelRemove((ChannelRemove) message.getMessage());
			break;
		case MessageTypes.CHANNEL_STATE:
			processChannelState((ChannelState) message.getMessage());
			break;
		case MessageTypes.USER_STATE:
			processUserState((UserState) message.getMessage());
			break;
		case MessageTypes.USER_REMOVE:
			processUserRemove((UserRemove) message.getMessage());
			break;
		case MessageTypes.TEXT_MESSAGE:
			processTextMessage((TextMessage) message.getMessage());
			break;
		case MessageTypes.PERMISSION_DENIED:
			processPermissionDenied((PermissionDenied) message.getMessage());
			break;
		case MessageTypes.CODEC_VERSION:
			processCodecVersion((CodecVersion) message.getMessage());
			break;
		case MessageTypes.SERVER_CONFIG:
			processServerConfig((MumbleProto.Mumble.ServerConfig) message.getMessage());
			break;
		case MessageTypes.PERMISSION_QUERY:
			PermissionQuery query = (PermissionQuery) message.getMessage();
			MumbleChannel channel = mumble.getChannelById(query.getChannelId());
			System.out.println("Query - Channel: " + channel + ", data: " + query);
			break;
		case MessageTypes.CRYPT_SETUP:
			processCryptSetup((CryptSetup) message.getMessage());
			break;
		default:
			Logger.getLogger(MumbleProtocol.class.getName()).warning("Unhandled protocol : " + message.getType());
			break;
		}
	}

	public void processUdp(MumbleUDPMessage message) {
		incrementUdpPackets();
		
		ByteBuf buffer = message.getMessage();
		int data = buffer.readByte();
		int type = data >> 5 & 0x7;
		
		switch(type) {
		case Globals.UDPVoiceCELTAlpha:
		case Globals.UDPVoiceCELTBeta:
		case Globals.UDPVoiceSpeex:
		case Globals.UDPVoiceOpus:
			processVoicePacket(data, type, buffer);
			break;
		case Globals.UDPPing:
			long timestamp = buffer.readLong();

			mumble.refreshUdpLimit(timestamp + UDP_PING_TRESHOLD);
			break;
		}
	}
	
	private void processCryptSetup(CryptSetup message) {
		if (message.hasKey() && message.hasClientNonce() &&
				message.hasServerNonce()) {
			// Full key setup
			mumble.cryptState.setKeys(
					message.getKey().toByteArray(),
					message.getClientNonce().toByteArray(),
					message.getServerNonce().toByteArray());
		} else if (message.hasServerNonce()) {
			// Server syncing its nonce to us.
			mumble.cryptState.setServerNonce(message.getServerNonce().toByteArray());
		} else {
			// Server wants our nonce.
			final CryptSetup.Builder nonceBuilder = CryptSetup.newBuilder();
			nonceBuilder.setClientNonce(ByteString.copyFrom(mumble.cryptState.getClientNonce()));
			write(nonceBuilder.build());
		}
	}

	private void processServerConfig(MumbleProto.Mumble.ServerConfig message) {
		if(mumble.getCurrentUser() == null) {
			return;
		}
		
		MumbleServerConfig cfg = mumble.getServerConfig();
		if(cfg == null) {
			cfg = new MumbleServerConfig();
		}
		
		if(message.hasMaxBandwidth()) {
			cfg.setMaxBandwidth(message.getMaxBandwidth());
		}
		
		if(message.hasWelcomeText()) {
			cfg.setWelcomeText(message.getWelcomeText());
		}
		
		if(message.hasAllowHtml()) {
			cfg.setAllowHtml(message.getAllowHtml());
		}
		
		if(message.hasMessageLength()) {
			cfg.setMessageLength(message.getMessageLength());
		}
		
		if(message.hasImageMessageLength()) {
			cfg.setImageMessageLength(message.getImageMessageLength());
		}
		
		mumble.setServerConfig(cfg);
		
		mumble.callEvent(new ServerConfigEvent(mumble, cfg));
	}

	private void processCodecVersion(CodecVersion message) {
		codec = CODEC_NOCODEC;
		if (message.hasOpus() && message.getOpus()) {
			codec = CODEC_OPUS;
			System.out.println("Using Opus!");
		} else if (message.hasAlpha() &&
				message.getAlpha() == Globals.CELT_VERSION) {
			codec = CODEC_ALPHA;
		} else if (message.hasBeta() &&
				message.getBeta() == Globals.CELT_VERSION) {
			codec = CODEC_BETA;
		}
	}

	private void processTextMessage(TextMessage message) {
		String text = message.getMessage();
		text = StringEscapeUtils.unescapeHtml4(text);
		if(message.hasActor()) {
			MumbleUser user = mumble.getUserById(message.getActor());
			
			if (user == null) {
				return;
			}
			
			if (message.getChannelIdCount() > 0) {
				// Channel event
				MumbleChannel channel = mumble.getChannelById(message.getChannelId(0));
				if(channel != null) {
					mumble.callEvent(new ChannelTextMessageEvent(mumble, channel, user, text));
				}
			} else {
				mumble.callEvent(new UserTextMessageEvent(mumble, user, text));
			}
		} else {
			mumble.callEvent(new TextMessageEvent(mumble, text));
		}
	}

	private void processUserRemove(UserRemove message) {
		MumbleUser user = mumble.removeUser(message.getSession());
		
		if(user != null) {
			// Remove the user from the channel as well.
			user.getChannel().removeUser(user);
			
			mumble.callEvent(new UserRemovedEvent(mumble, user));
		}
	}

	private void processPermissionDenied(PermissionDenied message) {
		mumble.callEvent(new PermissionDeniedEvent(message.getPermission(), mumble.getChannelById(message.getChannelId()), message.getSession(), message.getReason(), message.getType()));
	}

	private void processUserState(UserState message) {
		MumbleUser user = mumble.getUserById(message.getSession());
		
		boolean added = false, channelUpdated = false;
		
		if(user == null) {
			user = new MumbleUser();
			user.setSession(message.getSession());
			mumble.registerUser(user);
			
			added = true;
		}
		
		if(message.hasName()) {
			user.setName(message.getName());
		}
		
		if (message.hasComment()) {
			user.setComment(message.getComment());
		}
		
		if (message.hasSelfDeaf() || message.hasSelfMute()) {
			if (message.getSelfDeaf()) {
				user.setUserState(MumbleUser.USERSTATE_DEAFENED);
			} else if (message.getSelfMute()) {
				user.setUserState(MumbleUser.USERSTATE_MUTED);
			} else {
				user.setUserState(MumbleUser.USERSTATE_NONE);
			}
		}

		if (message.hasMute()) {
			user.setMuted(message.getMute());
			user.setUserState(user.isMuted() ? MumbleUser.USERSTATE_MUTED
				: MumbleUser.USERSTATE_NONE);
		}

		if (message.hasDeaf()) {
			user.setDeafened(message.getDeaf());
			user.setMuted(message.getMute() | message.getDeaf());
			user.setUserState(user.isDeafened() ? MumbleUser.USERSTATE_DEAFENED : (user.isMuted() ? MumbleUser.USERSTATE_MUTED : MumbleUser.USERSTATE_NONE));
		}

		if (message.hasSuppress()) {
			user.setUserState(message.getSuppress() ? MumbleUser.USERSTATE_MUTED
				: MumbleUser.USERSTATE_NONE);
		}

		if (added || message.hasChannelId()) {
			MumbleChannel channel = mumble.getChannelById(message.getChannelId());
			if(channel == null) {
				throw new RuntimeException("Unknown channel!");
			}
			user.setChannel(channel);
			channelUpdated = true;
		}
		
		if(added) {
			mumble.callEvent(new UserJoinEvent(mumble, user));
		} else {
			mumble.callEvent(new UserUpdateEvent(mumble, user));
		}
		
		if(channelUpdated) {
			mumble.callEvent(new UserJoinChannelEvent(mumble, user, user.getChannel()));
		}
	}

	private void processChannelState(ChannelState message) {
		MumbleChannel channel = mumble.getChannelById(message.getChannelId());
		if (channel != null) {
			if (message.hasName()) {
				channel.setName(message.getName());
			}
			mumble.callEvent(new ChannelUpdateEvent(mumble, channel));
			return;
		}

		// New channel
		channel = new MumbleChannel(message.getChannelId(), message.getName());
		
		if(message.hasParent()) {
			MumbleChannel parent = mumble.getChannelById(message.getParent());
			parent.addSubChannel(channel);
			channel.setParent(parent);
		}
		
		mumble.registerChannel(channel);
		
		mumble.callEvent(new ChannelRegisteredEvent(mumble, channel));
	}

	private void processChannelRemove(ChannelRemove message) {
		MumbleChannel channel = mumble.unregisterChannel(message.getChannelId());
		if(channel.hasParent()) {
			channel.getParent().removeSubChannel(channel);
		}
		if(channel != null) {
			mumble.callEvent(new ChannelRemoveEvent(mumble, channel));
		}
	}

	private void processServerSync(ServerSync message) {
		MumbleServerConfig config = mumble.getServerConfig();
		if(config == null) {
			config = new MumbleServerConfig();
		}
		
		boolean changed = false;
		
		if(message.hasMaxBandwidth()) {
			config.setMaxBandwidth(message.getMaxBandwidth());
			changed = true;
		}
		
		if(message.hasWelcomeText()) {
			config.setWelcomeText(message.getWelcomeText());
			changed = true;
		}
		
		if(changed) {
			mumble.setServerConfig(config);
			mumble.callEvent(new ServerConfigEvent(mumble, config));
		}
		
		if(mumble.getCurrentUser() != null) {
			return;
		}
		
		new Thread(new MumblePingRunnable(mumble)).start();
		
		MumbleUser currentUser = mumble.getUserById(message.getSession());
		
		mumble.setCurrentUser(currentUser);
		
		mumble.callEvent(new ServerSyncEvent(mumble, currentUser));
	}

	private void processReject(Reject reject) {
		mumble.callEvent(new ServerRejectEvent(reject.getReason(), reject.getType()));
		mumble.close();
	}

	@SuppressWarnings("unused")
	private void processVoicePacket(int data, int type, ByteBuf buffer) {
		// Unused, but contains the type (0 normal, 1 whisper to channel, 2-30 direct whisper - 2 for direct incoming, 31 server loopback)
		final int flags = data & 0x1f;

		// Don't try to decode the unsupported codec version.
		if (type != codec) {
			return;
		}
		
		// This is Mumble's varint data type
		final long session = ByteBufPDS.read(buffer);

		final MumbleUser u = mumble.getUserById((int) session);
		if (u == null) {
			return;
		}

		/*
		// Produce an audio event
		mumble.callEvent(new AudioChatEvent(mumble, u, type, flags, pds));*/
	}
	
	public void write(GeneratedMessage m) {
		channel.writeAndFlush(m);
	}
	
	public void updateLastPing() {
		lastPing = System.currentTimeMillis();
	}
	
	public long getLastLatency() {
		return lastLatency;
	}

	public int getTcpPackets() {
		return tcpPackets.get();
	}
	
	public int getUdpPackets() {
		return udpPackets.get();
	}
}
