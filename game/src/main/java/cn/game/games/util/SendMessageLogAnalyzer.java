package cn.game.games.util;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**    
 * 2025-11-13 10:38:40.597 [vt-53] INFO  Stdout - opType[sendWithLength]PlayerId[240420837]errorCode[0]msgId[0x01000002]msgLength[66993]seq[1]
	对日志格式进行分析，统计消息数量以及长度是否有增长
 * 
 */
public class SendMessageLogAnalyzer {
    
    static class MinuteStats {
        String minute;
        int totalCount = 0;
        long totalLength = 0;
        Map<String, Integer> msgIdCount = new HashMap<>();
        Map<String, Long> msgIdLength = new HashMap<>();
        
        public MinuteStats(String minute) {
            this.minute = minute;
        }
        
        public void addMessage(String msgId, int length) {
            totalCount++;
            totalLength += length;
            msgIdCount.put(msgId, msgIdCount.getOrDefault(msgId, 0) + 1);
            msgIdLength.put(msgId, msgIdLength.getOrDefault(msgId, 0L) + length);
        }
    }
    
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("使用方法: java LogAnalyzer <日志文件路径>");
            return;
        }
        
        String logFilePath = args[0];
        analyzeLog(logFilePath);
    }
    
    public static void analyzeLog(String logFilePath) {
        // 日志格式正则表达式
        Pattern pattern = Pattern.compile(
            "(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}):\\d{2}\\.\\d+ .*?" +
            "opType\\[sendWithLength\\].*?" +
            "msgId\\[(0x[0-9A-Fa-f]+)\\].*?" +
            "msgLength\\[(\\d+)\\]"
        );
        
        // 存储每分钟的统计数据
        Map<String, MinuteStats> statsMap = new TreeMap<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(logFilePath))) {
            String line;
            int lineNumber = 0;
            int parsedCount = 0;
            
            while ((line = br.readLine()) != null) {
                lineNumber++;
                Matcher matcher = pattern.matcher(line);
                
                if (matcher.find()) {
                    String minute = matcher.group(1); // 精确到分钟
                    String msgId = matcher.group(2);
                    int msgLength = Integer.parseInt(matcher.group(3));
                    
                    MinuteStats stats = statsMap.computeIfAbsent(minute, MinuteStats::new);
                    stats.addMessage(msgId, msgLength);
                    parsedCount++;
                } else if (line.contains("opType[sendWithLength]")) {
                    System.err.println("警告: 第 " + lineNumber + " 行无法解析: " + line);
                }
            }
            
            System.out.println("总共读取 " + lineNumber + " 行，成功解析 " + parsedCount + " 条消息");
            System.out.println("=".repeat(100));
            
            // 输出统计结果
            printSummary(statsMap);
            System.out.println("\n" + "=".repeat(100));
            printDetailedStats(statsMap);
            
        } catch (IOException e) {
            System.err.println("读取文件错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void printSummary(Map<String, MinuteStats> statsMap) {
        System.out.println("\n每分钟统计摘要:");
        System.out.println("-".repeat(100));
        System.out.printf("%-20s | %10s | %15s | %15s%n", 
            "时间(分钟)", "消息数量", "总长度(字节)", "平均长度(字节)");
        System.out.println("-".repeat(100));
        
        for (MinuteStats stats : statsMap.values()) {
            double avgLength = stats.totalCount > 0 ? 
                (double) stats.totalLength / stats.totalCount : 0;
            System.out.printf("%-20s | %,10d | %,15d | %,15.2f%n",
                stats.minute, stats.totalCount, stats.totalLength, avgLength);
        }
    }
    
    private static void printDetailedStats(Map<String, MinuteStats> statsMap) {
        System.out.println("\n每分钟详细消息ID统计:");
        
        // 收集所有出现过的消息ID
        Set<String> allMsgIds = new TreeSet<>();
        for (MinuteStats stats : statsMap.values()) {
            allMsgIds.addAll(stats.msgIdCount.keySet());
        }
        
        for (MinuteStats stats : statsMap.values()) {
            System.out.println("\n" + "-".repeat(100));
            System.out.println("时间: " + stats.minute);
            System.out.printf("总消息数: %,d | 总长度: %,d 字节%n", 
                stats.totalCount, stats.totalLength);
            System.out.println("-".repeat(100));
            System.out.printf("%-15s | %10s | %12s | %15s | %15s%n",
                "消息ID", "数量", "占比(%)", "总长度(字节)", "平均长度(字节)");
            System.out.println("-".repeat(100));
            
            for (String msgId : allMsgIds) {
                int count = stats.msgIdCount.getOrDefault(msgId, 0);
                long length = stats.msgIdLength.getOrDefault(msgId, 0L);
                
                if (count > 0) {
                    double percentage = (double) count / stats.totalCount * 100;
                    double avgLength = (double) length / count;
                    System.out.printf("%-15s | %,10d | %11.2f%% | %,15d | %,15.2f%n",
                        msgId, count, percentage, length, avgLength);
                }
            }
        }
    }
    
    // 用于代码测试的方法
    public static void generateTestLog(String outputPath) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputPath))) {
            Random random = new Random();
            String[] msgIds = {"0x01000002", "0x01000003", "0x01000004"};
            
            for (int minute = 0; minute < 5; minute++) {
                for (int i = 0; i < 100 + minute * 20; i++) { // 每分钟消息数递增
                    String msgId = msgIds[random.nextInt(msgIds.length)];
                    int length = 50000 + random.nextInt(20000) + minute * 5000; // 长度也递增
                    
                    writer.printf("2025-11-13 10:%02d:%02d.597 [vt-53] INFO  Stdout - " +
                        "opType[sendWithLength]PlayerId[240420837]errorCode[0]" +
                        "msgId[%s]msgLength[%d]seq[1]%n",
                        minute, random.nextInt(60), msgId, length);
                }
            }
        }
    }
}