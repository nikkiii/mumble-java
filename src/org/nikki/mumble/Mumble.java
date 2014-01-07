package org.nikki.mumble;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;

import org.nikki.mumble.cert.MumbleCertificate;
import org.nikki.mumble.model.MumbleChannel;
import org.nikki.mumble.model.MumbleServerConfig;
import org.nikki.mumble.model.MumbleUser;
import org.nikki.mumble.net.MumbleChannelInitializer;
import org.nikki.mumble.net.MumbleUDPChannelInitializer;
import org.nikki.mumble.ssl.LocalSSLTrustManager;
import org.nikkii.eventhub.event.Event;
import org.nikkii.eventhub.event.EventHub;
import org.nikkii.eventhub.event.EventListener;

import MumbleProto.Mumble.Authenticate;
import MumbleProto.Mumble.UserState;
import MumbleProto.Mumble.Version;

import com.google.protobuf.GeneratedMessage;

public class Mumble {

	public static final int PROTOCOL_VERSION = 66052;
	
	private Map<Integer, MumbleChannel> channels = new HashMap<Integer, MumbleChannel>();

	public CryptState cryptState = new CryptState();
	private MumbleUser currentUser;
	private EventHub hub;
	
	private MumbleCertificate certificate;

	private MumbleProtocol protocol;

	private MumbleServerConfig serverConfig;

	private Map<Integer, MumbleUser> users = new HashMap<Integer, MumbleUser>();

	public long useUdpUntil;

	private boolean usingUdp = false;
	
	private InetAddress host;
	private int port;
	
	private Channel tcpChannel;
	private Channel udpChannel;
	
	private InetSocketAddress socketAddress;
	private ServerHandler serverHandler;

	public Mumble() {
		System.setProperty("jna.library.path", "native");
		hub = new EventHub();
		serverHandler = new ServerHandler(this);
	}

	public void authenticate(String user) {
		authenticate(user, null);
	}
	
	public void authenticate(String user, String password) {
		Authenticate.Builder b = Authenticate.newBuilder();
		b.setUsername(user);
		if(password != null) {
			b.setPassword(password);
		}
		b.setOpus(true);
		
		write(b.build());
	}
	
	public void setCertificate(MumbleCertificate certificate) {
		this.certificate = certificate;
	}
	
	public boolean hasCertificate() {
		return certificate != null;
	}

	public void callEvent(Event event) {
		hub.callEvent(event);
	}

	public void connect(String host, int port) throws IOException, KeyManagementException, NoSuchAlgorithmException, InterruptedException {
		this.host = InetAddress.getByName(host);
		this.port = port;
		this.socketAddress = new InetSocketAddress(host, port);
		
		EventLoopGroup group = new NioEventLoopGroup();
		
		KeyManager[] keyManager = null;
		if(certificate != null) {
			keyManager = new KeyManager[] { certificate.getKeyManager() };
		}
		
		final SSLContext ctx = SSLContext.getInstance("TLS");
		ctx.init(keyManager, new TrustManager[] { new LocalSSLTrustManager() }, null);

		SSLEngine engine = ctx.createSSLEngine();
		engine.setUseClientMode(true);
		engine.setEnabledProtocols(new String[] { "TLSv1" });
		
		Bootstrap b = new Bootstrap();
		b.group(group)
		 .channel(NioSocketChannel.class)
		 .handler(new MumbleChannelInitializer(this, engine));
		
		ChannelFuture connectFuture = b.connect(host, port);
		
		connectFuture.sync();
		
		if(!connectFuture.isSuccess()) {
			throw new IOException("Unable to connect to " + host + ":" + port);
		}
		
		tcpChannel = connectFuture.channel();
		
		// Setup UDP
		Bootstrap udpBootstrap = new Bootstrap();
		udpBootstrap.group(group)
					.channel(NioDatagramChannel.class)
					.handler(new MumbleUDPChannelInitializer(this));
		
		udpChannel = udpBootstrap.connect(host, port).sync().channel();
		
		protocol = new MumbleProtocol(this);
	}
	
	public void registerCurrentUser() {
		UserState.Builder b = UserState.newBuilder();
		b.setSession(currentUser.getSession());
		b.setUserId(0);
		write(b.build());
	}

	public MumbleUser getUserById(int id) {
		return users.get(id);
	}

	public MumbleChannel getChannelById(int channelId) {
		return channels.get(channelId);
	}

	public MumbleChannel getChannelByName(String name) {
		for (MumbleChannel channel : channels.values()) {
			if (channel.getName().equals(name))
				return channel;
		}
		return null;
	}

	public Collection<MumbleChannel> getChannels() {
		return channels.values();
	}

	public MumbleUser getCurrentUser() {
		return currentUser;
	}

	public void joinChannel(MumbleChannel channel) {
		final UserState.Builder us = userSettingBuilder();
		us.setChannelId(channel.getId());
		write(us.build());
	}

	public void refreshUdpLimit(final long limit) {
		useUdpUntil = limit;
	}

	public void registerChannel(MumbleChannel channel) {
		channels.put(channel.getId(), channel);
	}

	public void registerListener(EventListener listener) {
		hub.registerListener(listener);
	}

	public void registerUser(MumbleUser user) {
		users.put(user.getSession(), user);
	}

	public MumbleUser removeUser(int sessionId) {
		return users.remove(sessionId);
	}

	public void sendVersion() {
		Version.Builder m = Version.newBuilder();
		
		m.setVersion(PROTOCOL_VERSION);
		m.setRelease("MumbleJava 1.0");
		m.setOs(System.getProperty("java.runtime.name"));
		m.setOsVersion(System.getProperty("java.version"));

		write(m.build());
	}

	public void setComment(String comment) {
		write(userSettingBuilder().setComment(comment).build());
	}

	private UserState.Builder userSettingBuilder() {
		UserState.Builder b = UserState.newBuilder();
		if(currentUser != null)
			b.setSession(currentUser.getSession());
		return b;
	}

	public void setCurrentUser(MumbleUser currentUser) {
		this.currentUser = currentUser;
	}

	public void setServerConfig(MumbleServerConfig serverConfig) {
		this.serverConfig = serverConfig;
	}

	public MumbleServerConfig getServerConfig() {
		return serverConfig;
	}

	public void setState(boolean deafened, boolean muted) {
		UserState.Builder b = userSettingBuilder();
		b.setSelfDeaf(deafened);
		b.setSelfMute(muted);
		write(b.build());
	}

	public MumbleChannel unregisterChannel(int channelId) {
		return channels.remove(channelId);
	}

	public void write(GeneratedMessage m) {
		protocol.write(m);
	}

	public void writeUdp(byte[] buffer, int length, boolean forceUdp) {
		if (forceUdp || useUdpUntil > System.currentTimeMillis()) {
			if (!usingUdp && !forceUdp) {
				usingUdp = true;
			}

			final byte[] encryptedBuffer = cryptState.encrypt(buffer, length);
			
			udpChannel.writeAndFlush(new DatagramPacket(Unpooled.wrappedBuffer(encryptedBuffer), socketAddress));
		} else {
			if (usingUdp) {
				usingUdp = false;
			}
			
			ByteBuf buf = Unpooled.buffer(6 + length);
			buf.writeShort(MessageTypes.UDP_TUNNEL);
			buf.writeInt(length);
			buf.writeBytes(buffer);
			
			tcpChannel.writeAndFlush(buf);
		}
	}
	
	public MumbleProtocol getProtocol() {
		return protocol;
	}

	public Channel getTCPChannel() {
		return tcpChannel;
	}
	
	public Channel getUDPChannel() {
		return udpChannel;
	}

	public Map<Integer, MumbleUser> getUsers() {
		return users;
	}
	
	public ServerHandler getServerHandler() {
		return serverHandler;
	}

	public void close() {
		tcpChannel.close();
		udpChannel.close();
	}
}
