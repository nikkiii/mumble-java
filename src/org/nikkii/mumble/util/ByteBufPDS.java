package org.nikkii.mumble.util;

import io.netty.buffer.ByteBuf;

public class ByteBufPDS {
	public static long read(ByteBuf buf) {
		long i = 0;
		final long v = buf.readByte();

		if ((v & 0x80) == 0x00) {
				i = v & 0x7F;
		} else if ((v & 0xC0) == 0x80) {
				i = (v & 0x3F) << 8 | buf.readByte();
		} else if ((v & 0xF0) == 0xF0) {
				final int tmp = (int) (v & 0xFC);
				switch (tmp) {
				case 0xF0:
						i = buf.readByte() << 24 | buf.readByte() << 16 | buf.readByte() << 8 | buf.readByte();
						break;
				case 0xF4:
						i = buf.readByte() << 56 | buf.readByte() << 48 | buf.readByte() << 40 | buf.readByte() << 32 |
								buf.readByte() << 24 | buf.readByte() << 16 | buf.readByte() << 8 | buf.readByte();
						break;
				case 0xF8:
						i = read(buf);
						i = ~i;
						break;
				case 0xFC:
						i = v & 0x03;
						i = ~i;
						break;
				default:
						i = 0;
						break;
				}
		} else if ((v & 0xF0) == 0xE0) {
				i = (v & 0x0F) << 24 | buf.readByte() << 16 | buf.readByte() << 8 | buf.readByte();
		} else if ((v & 0xE0) == 0xC0) {
				i = (v & 0x1F) << 16 | buf.readByte() << 8 | buf.readByte();
		}
		return i;
	}
	
	public static void write(ByteBuf buf, long value) {
		long i = value;
	
		if (((i & 0x8000000000000000L) > 0) && (~i < 0x100000000L)) {
			// Signed number.
			i = ~i;
			if (i <= 0x3) {
					// Shortcase for -1 to -4
					buf.writeByte((int) (0xFC | i));
					return;
			} else {
				buf.writeByte(0xF8);
			}
		}

		if (i < 0x80) {
			// Need top bit clear
			buf.writeByte((int) i);
		} else if (i < 0x4000) {
			// Need top two bits clear
			buf.writeByte((int) ((i >> 8) | 0x80));
			buf.writeByte((int) (i & 0xFF));
		} else if (i < 0x200000) {
			// Need top three bits clear
			buf.writeByte((int) ((i >> 16) | 0xC0));
			buf.writeByte((int) ((i >> 8) & 0xFF));
			buf.writeByte((int) (i & 0xFF));
		} else if (i < 0x10000000) {
			// Need top four bits clear
			buf.writeByte((int) ((i >> 24) | 0xE0));
			buf.writeByte((int) ((i >> 16) & 0xFF));
			buf.writeByte((int) ((i >> 8) & 0xFF));
			buf.writeByte((int) (i & 0xFF));
		} else if (i < 0x100000000L) {
			// It's a full 32-bit integer.
			buf.writeByte(0xF0);
			buf.writeInt((int) i);
		} else {
			// It's a 64-bit value.
			buf.writeByte(0xF4);
			buf.writeLong(i);
		}
	}
}
