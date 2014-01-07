package org.nikki.mumble;

public class ACL {
	public static class Perm {
		public static int None = 0x0;
		
		public static int Write = 0x1;
		
		public static int Traverse = 0x2;
		
		public static int Enter = 0x4;
		
		public static int Speak = 0x8;
		
		public static int MuteDeafen = 0x10;
		
		public static int Move = 0x20;
		
		public static int MakeChannel = 0x40;
		
		public static int LinkChannel = 0x80;
		
		public static int Whisper = 0x100;
		
		public static int TextMessage = 0x200;
		
		public static int MakeTempChannel = 0x400;
		
		public static int Kick = 0x10000;
		
		public static int Ban = 0x20000;
		
		public static int Register = 0x40000;
		
		public static int SelfRegister = 0x80000;
		
		public static int Cached = 0x8000000;
		
		public static int All = 0xf07ff;
	}
}
