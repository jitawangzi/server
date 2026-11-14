package cn.game.games.net.game.gm;

import java.util.ArrayList;
import java.util.List;

/**
 * GM文本命令行分词器，支持单双引号、反斜杠转义与空引号保留
 *
 * @author shuyongqiang
 */
public final class GmCommandTokenizer {

	private GmCommandTokenizer() {
	}

	/**
	 * 将原始文本命令解析为参数列表
	 *
	 * @param rawInput 原始命令行文本
	 * @return 解析后的词法单元列表
	 */
	public static List<String> tokenize(String rawInput) {
		List<String> tokens = new ArrayList<>();
		if (rawInput == null) {
			return tokens;
		}
		StringBuilder current = new StringBuilder();
		boolean inToken = false;
		char quoteChar = 0;
		boolean escaping = false;

		for (int i = 0; i < rawInput.length(); i++) {
			char c = rawInput.charAt(i);
			if (escaping) {
				current.append(unescapeChar(c));
				escaping = false;
				inToken = true;
				continue;
			}
			if (c == '\\') {
				escaping = true;
				inToken = true;
				continue;
			}
			if (quoteChar != 0) {
				if (c == quoteChar) {
					quoteChar = 0;
				} else {
					current.append(c);
				}
				inToken = true;
				continue;
			}
			if (c == '"' || c == '\'') {
				quoteChar = c;
				inToken = true;
				continue;
			}
			if (Character.isWhitespace(c)) {
				if (inToken) {
					tokens.add(current.toString());
					current.setLength(0);
					inToken = false;
				}
				continue;
			}
			current.append(c);
			inToken = true;
		}

		if (escaping) {
			throw new IllegalArgumentException("GM命令尾部存在未完成的转义符");
		}
		if (quoteChar != 0) {
			throw new IllegalArgumentException("GM命令存在未闭合的引号: " + quoteChar);
		}
		if (inToken) {
			tokens.add(current.toString());
		}
		return tokens;
	}

	private static char unescapeChar(char c) {
		return switch (c) {
			case 'n' -> '\n';
			case 't' -> '\t';
			case 'r' -> '\r';
			default -> c;
		};
	}
}