package cn.game.simulation.log;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LogAnalysis {

	public static void main(String args[]) throws Exception{
		List<String> endString = new ArrayList<>() ; 
		File file  = new File("D:\\greensoft\\crt\\download") ; 
		File slow  = new File("D:\\greensoft\\crt\\download\\slow.txt") ; 
		int total = 0  ; 
		int ms30 = 0 ; 
		int ms100 = 0 ; 
		int ms500 = 0 ; 
		int ms1000 = 0 ; 
		if (slow.exists()) {
			slow.delete() ; 
		}
		if (file.isDirectory()) {
			File[] listFiles = file.listFiles(); 
			for (File f : listFiles) {
				try {
					List<String> readLines = FileUtils.readLines(f);
					for (String string : readLines) {
						int r = string.indexOf("RunTime"); 
						int ms = string.lastIndexOf("ms") ; 
						if (r<0||ms<0) {
							continue ; 
						}
						total ++ ; 
						String end = string.substring(r+"RunTime".length()+1, ms-1) ; 
						int time = Integer.parseInt(end) ; 
						if (time>30) {
							endString.add(string.substring(0, ms)) ; 
							if (time>=30&&time<100) {
								ms30++ ; 
							}else if (time>=100&&time<500) {
								ms100++  ; 
							}else if(time>=500&&time<1000){
								
								ms500++ ; 
							}else if(time>=1000){
								System.out.println(string);
								ms1000++ ; 
							}
						}
						
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
				} 
			}
			FileUtils.writeLines(slow, endString) ; 
			System.out.println("total message : "+total);
			System.out.println("over message : "+endString.size());
			System.out.println("over 30ms : "+ms30);
			System.out.println("over 100ms : "+ms100);
			System.out.println("over 500ms : "+ms500);
			System.out.println("over 1000ms : "+ms1000);
			
		}
	}
}
