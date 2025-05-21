package cn.game.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 通用的Linux系统磁盘使用率监控工具类
 */
public class DiskUsageMonitor {
	private Logger log = LoggerFactory.getLogger(DiskUsageMonitor.class);

	/**
	 * 获取硬盘使用率（0.0-1.0表示0-100%）
	 * 优先使用iostat命令，失败则回退到/proc/diskstats解析
	 * @return 硬盘使用率（0.0-1.0范围）
	 */
	private double getRawDiskUsage() {
	    try {
	        // 尝试使用iostat（更准确）
	        double ioUsage = getDiskUsageViaIostat();
	        if (ioUsage >= 0) {
	            return ioUsage;
	        }
	    } catch (Exception e) {
	        log.debug("iostat method failed, falling back to /proc/diskstats", e);
	    }
	    
	    // 回退到/proc/diskstats方法
	    try {
	        return getDiskUsageViaProcDiskstats();
	    } catch (Exception e) {
	        log.warn("Failed to get disk usage", e);
	        return 0.0; // 出错时返回0，避免系统误判
	    }
	}

	/**
	 * 通过iostat命令获取磁盘使用率
	 * @return 磁盘使用率，0.0-1.0范围，如果获取失败返回-1
	 */
	private double getDiskUsageViaIostat() {
	    try {
	        // 使用短时间窗口（1秒）以获取较实时的数据
	        Process process = Runtime.getRuntime().exec("iostat -dx 1 2");
	        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
	        String line;
	        double maxUtilization = 0.0;
	        boolean readingSecondSample = false;
	        boolean foundDevice = false;
	        
	        while ((line = reader.readLine()) != null) {
	            // 识别Device行，表示样本开始
	            if (line.contains("Device")) {
	                if (readingSecondSample) {
	                    foundDevice = true;
	                    continue;
	                } else {
	                    readingSecondSample = true;
	                    continue;
	                }
	            }
	            
	            // 只处理第二个样本的磁盘数据
	            if (foundDevice && !line.trim().isEmpty()) {
	                String[] parts = line.trim().split("\\s+");
	                if (parts.length > 0) {
	                    String device = parts[0];
	                    // 只考虑物理磁盘设备
	                    if (device.startsWith("sd") || device.startsWith("hd") || 
	                        device.startsWith("nvme") || device.startsWith("vd")) {
	                        // %util列通常是最后一列
	                        try {
	                            double utilization = Double.parseDouble(parts[parts.length - 1]) / 100.0;
	                            maxUtilization = Math.max(maxUtilization, utilization);
	                        } catch (NumberFormatException e) {
	                            // 忽略解析错误
	                        }
	                    }
	                }
	            }
	        }
	        
	        // 等待进程完成
	        int exitCode = process.waitFor();
	        if (exitCode != 0) {
	            return -1;
	        }
	        
	        return maxUtilization;
	    } catch (Exception e) {
	        log.debug("iostat execution failed", e);
	        return -1;
	    }
	}

	/**
	 * 通过/proc/diskstats获取磁盘使用率
	 * @return 磁盘使用率，0.0-1.0范围
	 */
	private double getDiskUsageViaProcDiskstats() throws IOException, InterruptedException {
	    // 获取第一个样本
	    Map<String, long[]> firstSample = readDiskStats();
	    long firstSampleTime = System.currentTimeMillis();
	    
	    // 短暂等待以获取差异
	    Thread.sleep(1000);
	    
	    // 获取第二个样本
	    Map<String, long[]> secondSample = readDiskStats();
	    long secondSampleTime = System.currentTimeMillis();
	    
	    // 计算最大使用率
	    double maxUtilization = 0.0;
	    long timeElapsedMs = secondSampleTime - firstSampleTime;
	    
	    for (Map.Entry<String, long[]> entry : secondSample.entrySet()) {
	        String device = entry.getKey();
	        long[] second = entry.getValue();
	        long[] first = firstSample.get(device);
	        
	        if (first != null && second.length >= 10 && first.length >= 10) {
	            // io_ticks是第10个元素（索引9）
	            long ticksDiff = second[9] - first[9];
	            // 计算使用率：ticksDiff毫秒中有多少毫秒在I/O上
	            double utilization = (double) ticksDiff / timeElapsedMs;
	            // 确保在0-1范围内
	            utilization = Math.min(1.0, Math.max(0.0, utilization));
	            maxUtilization = Math.max(maxUtilization, utilization);
	        }
	    }
	    
	    return maxUtilization;
	}

	/**
	 * 读取/proc/diskstats文件
	 * @return 磁盘统计数据映射
	 */
	private Map<String, long[]> readDiskStats() throws IOException {
	    Map<String, long[]> result = new HashMap<>();
	    List<String> lines = Files.readAllLines(Paths.get("/proc/diskstats"));
	    
	    for (String line : lines) {
	        String[] parts = line.trim().split("\\s+");
	        if (parts.length >= 14) {
	            String deviceName = parts[2];
	            // 只收集物理磁盘，排除分区
	            if ((deviceName.startsWith("sd") || deviceName.startsWith("hd") || 
	                 deviceName.startsWith("nvme") || deviceName.startsWith("vd")) && 
	                !deviceName.matches(".*\\d+$")) {
	                long[] stats = new long[parts.length - 3];
	                for (int i = 0; i < stats.length; i++) {
	                    stats[i] = Long.parseLong(parts[i + 3]);
	                }
	                result.put(deviceName, stats);
	            }
	        }
	    }
	    
	    return result;
	}
}