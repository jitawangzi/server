package cn.game.games.util;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class KeywordFilter {
    private static class TrieNode {
		Map<Character, TrieNode> children;
        boolean isEnd;

		TrieNode() {
			this.children = new HashMap<>();
		}
    }

	private final TrieNode root;
	private static final AtomicReference<KeywordFilter> INSTANCE = new AtomicReference<>();
	private static String filterFileName = "chatfilter.txt";
//	private static char replaceChar = '*';

	private KeywordFilter(Set<String> keywords) {
		this.root = new TrieNode();
        for (String keyword : keywords) {
            insert(keyword.toLowerCase());
        }
    }

    private void insert(String keyword) {
        TrieNode node = root;
        for (char c : keyword.toCharArray()) {
            node = node.children.computeIfAbsent(c, k -> new TrieNode());
        }
        node.isEnd = true;
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
					result.setCharAt(k, '*');
                }
				i = matchEnd;
            }
        }

        return modified ? result.toString() : text;
    }

	public static KeywordFilter getInstance() {
		return INSTANCE.get();
	}

	public static void initializeFromKeywords(Set<String> newKeywords) {
		KeywordFilter newFilter = new KeywordFilter(newKeywords);
		INSTANCE.set(newFilter);
	}

	public static void initializeFromFile() throws IOException {
		Set<String> keywords = readKeyWordsFromFile(filterFileName);
		initializeFromKeywords(keywords);
	}

	private static Set<String> readKeyWordsFromFile(String filename) throws IOException {
		Set<String> keywords = new HashSet<>();
		try (InputStream is = KeywordFilter.class.getClassLoader().getResourceAsStream(filename);
				BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (!line.isEmpty()) {
					keywords.add(line);
				}
			}
		} catch (NullPointerException e) {
			throw new IOException("File not found: " + filename, e);
		}
		return keywords;
	}

    public static void main(String[] args) {
		try {
			KeywordFilter.initializeFromFile();

			KeywordFilter filter = KeywordFilter.getInstance();

			String text1 = "这是一段包含敏感词1和敏感词2的文本";
			System.out.println(filter.filter(text1));

			String text2 = "这是一段正常的文本，不包含敏感词";
			System.out.println(filter.filter(text2));

			// 动态更新关键词
			Set<String> keyWordsFromFile = readKeyWordsFromFile(filterFileName);
			Set<String> newKeywords = new HashSet<>(Arrays.asList("敏感词1", "敏感词2", "新敏感词"));
			keyWordsFromFile.addAll(newKeywords);
			KeywordFilter.initializeFromKeywords(keyWordsFromFile);

			filter = KeywordFilter.getInstance(); // 获取更新后的实例

			String text3 = "这是一段包含新敏感词的文本";
			System.out.println(filter.filter(text3));
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}