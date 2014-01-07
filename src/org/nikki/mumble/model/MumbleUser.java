package org.nikki.mumble.model;


public class MumbleUser {
	
	public static final int USERSTATE_NONE = 0;
	public static final int USERSTATE_MUTED = 1;
	public static final int USERSTATE_DEAFENED = 2;

	private int session;
	private String name;
	private String comment;
	private int talkingState;
	private int userState;
	private boolean isCurrent;

	private boolean muted;
	private boolean deafened;

	private MumbleChannel channel;
	
	public MumbleUser() {
	}
	
	public void setSession(int session) {
		this.session = session;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setTalkingState(int talkingState) {
		this.talkingState = talkingState;
	}

	public void setUserState(int userState) {
		this.userState = userState;
	}

	public void setCurrent(boolean isCurrent) {
		this.isCurrent = isCurrent;
	}

	public void setMuted(boolean muted) {
		this.muted = muted;
	}

	public void setDeafened(boolean deafened) {
		this.deafened = deafened;
	}

	public void setChannel(MumbleChannel channel) {
		if (this.channel != null) {
			this.channel.removeUser(this);
		}

		this.channel = channel;
		this.channel.addUser(this);
	}

	public static int getUserstateNone() {
		return USERSTATE_NONE;
	}

	public static int getUserstateMuted() {
		return USERSTATE_MUTED;
	}

	public static int getUserstateDeafened() {
		return USERSTATE_DEAFENED;
	}

	public int getSession() {
		return session;
	}

	public String getName() {
		return name;
	}

	public int getTalkingState() {
		return talkingState;
	}

	public int getUserState() {
		return userState;
	}

	public boolean isCurrent() {
		return isCurrent;
	}

	public boolean isMuted() {
		return muted;
	}

	public boolean isDeafened() {
		return deafened;
	}

	public MumbleChannel getChannel() {
		return channel;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public String getComment() {
		return comment;
	}

}
