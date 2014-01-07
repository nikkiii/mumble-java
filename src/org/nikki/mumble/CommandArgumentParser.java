package org.nikki.mumble;

import java.util.ArrayList;
import java.util.List;

public class CommandArgumentParser {
	public static String[] parse(String command) {
		List<String> args = new ArrayList<>();
		StringBuilder buf = new StringBuilder();
		boolean quoted = false, escaped = false;

		for (int i = 0; i < command.length(); i++) {
			char c = command.charAt(i);
			if (c == ' ' && !quoted) {
				args.add(buf.toString());
				buf.setLength(0);
			} else if (c == '"' && !escaped) {
				quoted = !quoted;
			} else if (c == '\\') {
				escaped = true;
			} else {
				escaped = false;
				buf.append(c);
			}
		}

		if (buf.length() > 0)
			args.add(buf.toString());

		return args.toArray(new String[args.size()]);
	}
}
