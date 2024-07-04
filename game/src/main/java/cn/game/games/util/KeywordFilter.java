package cn.game.games.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class KeywordFilter {

	private static String filterFileName = "chatfilter.txt";
	private static char replaceChar = '*';
	private static KeywordFilter instace;

	public static KeywordFilter getInstance() {
		if (instace == null) {
			try {
				instace = KeywordFilter.createFromFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return instace;
	}

    private static class TrieNode {
		Map<Character, TrieNode> children;
        boolean isEnd;

		TrieNode() {
			this.children = new HashMap<>();
		}
    }

	private final TrieNode root;

	private KeywordFilter(Set<String> keywords) {
		this.root = new TrieNode();
        for (String keyword : keywords) {
            insert(keyword.toLowerCase());
        }
		makeImmutable(root);
    }

    private void insert(String keyword) {
        TrieNode node = root;
        for (char c : keyword.toCharArray()) {
            node = node.children.computeIfAbsent(c, k -> new TrieNode());
        }
        node.isEnd = true;
    }

	private void makeImmutable(TrieNode node) {
		node.children = Collections.unmodifiableMap(node.children);
		for (TrieNode child : node.children.values()) {
			makeImmutable(child);
		}
	}

    public String filter(String text) {
        char[] chars = text.toCharArray();
        int n = chars.length;
        StringBuilder result = null;
        boolean modified = false;

        for (int i = 0; i < n; i++) {
            TrieNode node = root;
            int matchEnd = -1;
            for (int j = i; j < n; j++) {
                char c = Character.toLowerCase(chars[j]);
                if (!node.children.containsKey(c)) break;
                node = node.children.get(c);
                if (node.isEnd) {
                    matchEnd = j;
                }
            }
            
            if (matchEnd != -1) {
                if (!modified) {
                    result = new StringBuilder(text);
                    modified = true;
                }
                for (int k = i; k <= matchEnd; k++) {
					result.setCharAt(k, replaceChar);
                }
				i = matchEnd;
            }
        }

        return modified ? result.toString() : text;
    }

	public static KeywordFilter createFromFile() throws IOException {
		Set<String> keywords = new HashSet<>();
		try (InputStream is = KeywordFilter.class.getClassLoader().getResourceAsStream(filterFileName);
				BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (!line.isEmpty()) {
					keywords.add(line);
				}
			}
		} catch (NullPointerException e) {
			throw new IOException("File not found: " + filterFileName, e);
		}
		return new KeywordFilter(keywords);
	}

    public static void main(String[] args) {
		String text1 = "这是一段包含敏感词1和敏感词2的文本";
		System.out.println(KeywordFilter.getInstance().filter(text1));

		String text2 = "这是一段正常的文本，不包含敏感词";
		System.out.println(KeywordFilter.getInstance().filter(text2));
    }
}