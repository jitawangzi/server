package cn.game.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Deprecated
public class TreeWordFilter {

	private static Map<Character, WordTree> wordTrees;
	
	public static void init(String wordFilename){
		List<String> words = loadWordLibs(wordFilename);
		
		wordTrees = new HashMap<>();
		char[] chs;
		WordTree tree;
		WordNode node = null;
		for(String w : words){
			if(w == null || w.length() == 0)
				continue;
			
			chs = w.toCharArray();
			tree = wordTrees.get(chs[0]);
			if(tree == null){
				tree = new WordTree();
				wordTrees.put(chs[0], tree);
				node = tree.insertRoot(chs[0]);
			}else{
				node = tree.getRoot();
			}
			
			for(int i=1, len=chs.length; i<len; i++){
				node = tree.insertChild(node, chs[i]);
			}
			
			node.isWordEnd = true;
		}
		
		//FileUtil.writeTextFile("c:/a.txt", wordTrees.toString());
//		System.out.println("init complete");
	}
	
	public static List<String> loadWordLibs(String filename) {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		if(loader == null)
			loader = TreeWordFilter.class.getClassLoader();
		
		InputStream is = null;
		try{
			is = loader.getResourceAsStream(filename);			
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"));
			
			List<String> tmpLines = new ArrayList<String>(100000);
			String line = null;
			while((line = reader.readLine()) != null){
				tmpLines.add(line);
			}
			
			return tmpLines;
		}catch(Exception e){
			throw new RuntimeException("加载敏感词库失败, filename=" + filename);
		}finally{
			if(is != null){
				try {
					is.close();
				} catch (IOException e1) {					
					e1.printStackTrace();
				}
			}
		}
	}
	
	
	public static boolean check(String str){
		String s;
		for(int i=0, len=str.length(); i<len; i++){
			for(int j=i, len2=str.length(); j<len2; j++){
				s = str.substring(i, j+1);
				if(!checkWord(s))
					return false;
			}
		}
		return true;
	}
	
	private static boolean checkWord(String word){
		char[] chs = word.toCharArray();
		WordTree tree = wordTrees.get(chs[0]);
		if(tree == null)
			return true;
		
		WordNode node = tree.getRoot();
		for(int i=1, len=chs.length; i<len; i++){
			node = node.findChild(chs[i]);
			if(node == null)
				return true;
		}
				
		return !node.isWordEnd;
	}
	
	static class WordNode{
		private char ch;
		private boolean isWordEnd;
		private List<WordNode> childs;
		
		protected void sortChilds(){
			Collections.sort(childs, new Comparator<WordNode>(){
				@Override
				public int compare(WordNode o1, WordNode o2) {
					return o1.ch - o2.ch;
				}				
			});
		}
		
		protected WordNode findChild(char c){
			
			if(childs == null)
				return null;
			
			int start = 0, end = childs.size()-1;
			
			while(start <= end){
				int mid = start + (end - start) / 2;
				WordNode midNode = childs.get(mid);
				if(c == midNode.ch)
					return midNode;
				else if(c < midNode.ch){
					end = mid - 1;
				}else{
					start = mid + 1;
				}
			}
			return null;
		}
		
		protected void insertChild(WordNode node){
			if(childs == null)
				childs = new ArrayList<>();
			childs.add(node);
			sortChilds();
		}
		
		@Override
		public String toString(){
			JSONObject json = new JSONObject();
			json.put("value", ch);
			
			if(isWordEnd)
				json.put("end", "y");
			
			if(childs != null){
				JSONArray arr = new JSONArray();
				for(WordNode n : childs){
					arr.add(n.toString());
				}
				json.put("childs", arr);
			}
			
			return json.toString();
		}
	}
	
	static class WordTree{
		
		private WordNode root;
		
		protected WordNode getRoot(){
			return root;
		}
		
		/**
		 * 创建值未ch的根节点
		 * @param ch
		 * @return
		 */
		protected WordNode insertRoot(char ch){
			root = new WordNode();
			root.ch = ch;
			return root;
		}
		
		/**
		 * 插入子节点，如果相同值的节点不存在, 如果相同值的节点已经存在就直接返回
		 * 
		 * @param parentNode
		 * @param ch
		 * @return
		 */
		protected WordNode insertChild(WordNode parentNode, char ch){
			WordNode n = parentNode.findChild(ch);
			if(n == null){
				WordNode newNode = new WordNode();
				newNode.ch = ch;				
				parentNode.insertChild(newNode);
				return newNode;
			}else{
				return n;
			}
		}
		
		@Override
		public String toString(){
			return root.toString();
		}
	}
	
	public static void main(String[] args){
		String s = "毛  32泽 啥223东";
//		char[] chs = s.toCharArray();
//		for(int i=0, len=s.length(); i<len; i++){
//			System.out.println((int)chs[i]);
//		}
		
		TreeWordFilter f = new TreeWordFilter();
		f.init("filterWord.txt");
		
		System.out.println(f.check(s));
	}
}
