package cn.game.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermission;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SystemTimeShift {

    private static final Path STATE_DIR = Paths.get("/var/lib/java-time-shift");
    private static final Path STATE_FILE = STATE_DIR.resolve("state.properties");
    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ZoneId ZONE = ZoneId.systemDefault();

    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                printHelp();
                return;
            }
            String first = args[0].trim();

            if ("help".equalsIgnoreCase(first) || "--help".equalsIgnoreCase(first) || "-h".equalsIgnoreCase(first)) {
                printHelp();
                return;
            }
            if ("status".equalsIgnoreCase(first)) {
                status();
                return;
            }
            if ("restore".equalsIgnoreCase(first)) {
                restore();
                return;
            }
            if ("set".equalsIgnoreCase(first)) {
                if (args.length < 2) {
                    System.err.println("缺少时间参数，例如：set \"2025-01-01 00:00:00\"");
                    System.exit(2);
                }
                String target = joinArgs(Arrays.copyOfRange(args, 1, args.length));
                setAbsoluteTime(target);
                return;
            }
            if ("add".equalsIgnoreCase(first) || "sub".equalsIgnoreCase(first)) {
                if (args.length < 2) {
                    System.err.println("缺少偏移参数，例如：add 1h30m 或 sub 2d");
                    System.exit(2);
                }
                String offset = joinArgs(Arrays.copyOfRange(args, 1, args.length));
                boolean negative = "sub".equalsIgnoreCase(first);
                shiftByOffset(offset, negative);
                return;
            }
            // +offset / -offset
            if (first.startsWith("+") || first.startsWith("-")) {
                String offset = joinArgs(args);
                shiftByOffset(offset, false);
                return;
            }
            // 无符号偏移，默认增加
            String candidate = joinArgs(args);
            try {
                parseShift(candidate);
                shiftByOffset(candidate, false);
                return;
            } catch (IllegalArgumentException ignored) {}

            System.err.println("无法识别的命令/参数。");
            printHelp();
            System.exit(2);

        } catch (NeedsRootException e) {
            System.err.println("需要 root 权限或 CAP_SYS_TIME 能力，请使用 sudo 运行。详情：" + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println("执行失败：" + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void printHelp() {
        System.out.println("用法：");
        System.out.println("  sudo java cn.game.util.CentosTimeShift 1d");
        System.out.println("  sudo java cn.game.util.CentosTimeShift 2h30m");
        System.out.println("  sudo java cn.game.util.CentosTimeShift +2h30m");
        System.out.println("  sudo java cn.game.util.CentosTimeShift -1d");
        System.out.println("  sudo java cn.game.util.CentosTimeShift add 1w2d3h");
        System.out.println("  sudo java cn.game.util.CentosTimeShift set \"2025-01-01 00:00:00\"");
        System.out.println("  sudo java cn.game.util.CentosTimeShift restore   恢复到当前网络时间（chrony > ntpdate > SNTP）");
        System.out.println("  sudo java cn.game.util.CentosTimeShift status");
        System.out.println("单位：s 秒, m 分钟, h 小时, d 天, w 周, mo 月（m=分钟，mo=月份）");
        System.out.println("环境变量/属性：NTP_SERVERS 或 -Dntp.servers=\"server1 server2\"");
        System.out.println("默认NTP：pool.ntp.org time.cloudflare.com ntp.aliyun.com ntp.tencent.com");
    }

    // ---- Core operations ----

    private static void status() throws Exception {
        Tool tool = detectTool();
        System.out.println("工具检测：");
        System.out.println("  timedatectl 可用: " + tool.hasTimedatectl);
        System.out.println("  date 可用       : " + tool.hasDate);
        System.out.println("  chronyc 可用    : " + tool.hasChronyc + (tool.hasChronyc ? ("，可通信: " + chronyResponsive()) : ""));
        System.out.println("  ntpdate 可用    : " + tool.hasNtpdate);
        System.out.println("将用于设置系统时间: " + (tool.preferred == ToolKind.TIMEDATECTL ? "timedatectl" :
                tool.preferred == ToolKind.DATE ? "date -s" : "无"));

        String now = ZonedDateTime.now(ZONE).format(DT);
        System.out.println("当前系统时间（本地）: " + now);

        if (tool.hasTimedatectl) {
            NtpState ns = getNtpState();
            System.out.println("timedatectl NTP 启用: " + ns.enabled + ", 已同步: " + ns.synchronizedNow);
        }

        if (Files.exists(STATE_FILE)) {
            Properties p = loadProps(STATE_FILE);
            System.out.println("已保存状态：");
            System.out.println("  savedAt        : " + p.getProperty("savedAt", "未知"));
            System.out.println("  ntpOriginally  : " + p.getProperty("ntpOriginally", "未知"));
            System.out.println("  toolWhenSaved  : " + p.getProperty("toolWhenSaved", "未知"));
        } else {
            System.out.println("未发现已保存的原始状态。");
        }

        System.out.println("NTP 服务器: " + String.join(", ", getNtpServers()));
    }

    private static void setAbsoluteTime(String dateTime) throws Exception {
        ensureRoot();
        String normalized = dateTime.trim().replace('T', ' ');
        LocalDateTime ldt;
        try {
            ldt = LocalDateTime.parse(normalized, DT);
        } catch (Exception e) {
            try {
                ldt = LocalDateTime.parse(normalized, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm[:ss]"));
            } catch (Exception ex) {
                throw new IllegalArgumentException("无法解析时间，期望格式：YYYY-MM-DD HH:MM:SS，例如：2025-01-01 00:00:00");
            }
        }
        maybeSaveOriginalState();

        Tool tool = detectTool();
        if (tool.preferred == ToolKind.NONE) {
            throw new IllegalStateException("系统未检测到可用的时间设置工具（timedatectl 或 date）。");
        }

        // 为避免被 chrony 立刻拉回，先让 chrony offline
        if (tool.hasChronyc && chronyResponsive()) {
            runIgnore("chronyc", "-a", "offline");
        }

        String formatted = ldt.format(DT);
        applySystemTime(formatted, tool);
        tryHwclockSync();

        System.out.println("已将系统时间设为: " + formatted);
        System.out.println("提示：restore 将把时间对到当前网络时间。");
    }

    private static void shiftByOffset(String offsetStr, boolean forceNegative) throws Exception {
        ensureRoot();
        maybeSaveOriginalState();

        Tool tool = detectTool();
        if (tool.preferred == ToolKind.NONE) {
            throw new IllegalStateException("系统未检测到可用的时间设置工具（timedatectl 或 date）。");
        }

        Shift shift = parseShift(offsetStr);
        if (forceNegative) shift.negative = true;

        ZonedDateTime now = ZonedDateTime.now(ZONE);
        ZonedDateTime target = applyShift(now, shift);

        if (tool.hasChronyc && chronyResponsive()) {
            runIgnore("chronyc", "-a", "offline");
        }

        String formatted = target.format(DT);
        applySystemTime(formatted, tool);
        tryHwclockSync();

        System.out.println("当前时间: " + now.format(DT));
        System.out.println("目标时间: " + formatted);
        System.out.println(shift.negative ? "已减少时间。" : "已增加时间。");
        System.out.println("提示：restore 将把时间对到当前网络时间。");
    }

 private static void restore() throws Exception {
	 System.out.println("准备恢复时间");
     ensureRoot();
     Tool tool = detectTool();
     boolean success = false;
     String method = "none";
     Properties props = Files.exists(STATE_FILE) ? loadProps(STATE_FILE) : new Properties();
     boolean ntpOriginally = Boolean.parseBoolean(props.getProperty("ntpOriginally", "false"));

     // 1) [最佳实践] 优先尝试使用 timedatectl 恢复 NTP 服务
     // 如果系统支持 timedatectl 并且NTP原本是开启的，这是最优雅的恢复方式
     if (tool.hasTimedatectl && ntpOriginally) {
         System.out.println("尝试通过 timedatectl set-ntp true 恢复时间同步...");
         try {
             // 先禁用再开启，确保触发一次同步检查
             run("timedatectl", "set-ntp", "false");
             Thread.sleep(500);
             CommandResult r = run("timedatectl", "set-ntp", "true");
             if (r.exitCode == 0) {
                 // 等待几秒钟让系统服务去同步
                 if (checkTimedatectlSync(15)) { // 等待最多15秒
                     success = true;
                     method = "timedatectl set-ntp";
                 } else {
                     System.out.println("timedatectl 已开启NTP，但短时内未确认同步成功。系统将在后台继续尝试。");
                     // 即使未立即同步，开启NTP也是一种“恢复”
                     success = true;
                     method = "timedatectl set-ntp (async)";
                 }
             }
         } catch (Exception e) {
             System.out.println("通过 timedatectl 恢复失败: " + e.getMessage());
         }
     }

     // 2) fallback: chrony path (如果 timedatectl 方式不适用或失败)
     if (!success && tool.hasChronyc) {
         System.out.println("尝试通过 chronyc 手动同步...");
         if (!chronyResponsive()) startChronydIfPossible();
         if (chronyResponsive()) {
             runIgnore("chronyc", "-a", "online");
             runIgnore("chronyc", "-a", "burst", "4/4"); // 触发一次快速同步

             // 等待一段时间让 chrony 获取时间源
             System.out.println("等待 chrony 与服务器通信...");
             Thread.sleep(3000);

             // 直接强制校时
             CommandResult step = run("chronyc", "-a", "makestep");
             // 检查 exitCode 并且在 stdout 或 stderr 中寻找 "200 OK"
             boolean ok = step.exitCode == 0 &&
                          (step.stdout.contains("200 OK") || step.stderr.contains("200 OK"));

             if (ok) {
                 success = true;
                 method = "chronyc makestep";
                 // 如果NTP原本不是开启的，恢复 chrony 的 offline 状态
                 if (!ntpOriginally) {
                     System.out.println("NTP 原本为关闭状态，将 chrony 恢复为 offline。");
                     runIgnore("chronyc", "-a", "offline");
                 }
             } else {
                 System.out.println("chronyc makestep 未确认成功。 stdout: " + step.stdout.trim() + ", stderr: " + step.stderr.trim());
             }
         }
     }

     // 3) fallback: ntpdate
     if (!success && tool.hasNtpdate) {
         System.out.println("尝试通过 ntpdate 同步...");
         List<String> args = new ArrayList<>();
         args.add("ntpdate");
         args.add("-u"); // 使用非特权端口，有助于穿越防火墙
         args.addAll(Arrays.asList(getNtpServers()));
         CommandResult r = run(args.toArray(new String[0]));
         if (r.exitCode == 0) {
             success = true;
             method = "ntpdate";
         } else {
             System.out.println("ntpdate 对时失败，stderr: " + r.stderr.trim());
         }
     }

     // 4) fallback: 内置 SNTP 直接设时
     if (!success) {
         System.out.println("尝试通过内置 SNTP 客户端同步...");
         Instant ntpInstant = querySntpFirstSuccess(serversList());
         if (ntpInstant != null) {
             Tool t2 = detectTool(); // 重新检测工具，以防万一
             if (t2.preferred != ToolKind.NONE) {
                 String formatted = ZonedDateTime.ofInstant(ntpInstant, ZONE).format(DT);
                 applySystemTime(formatted, t2);
                 success = true;
                 method = "SNTP 内置客户端";
             }
         } else {
             System.out.println("内置 SNTP 客户端获取时间失败。");
         }
     }

     if (success) {
         tryHwclockSync();
         String now = ZonedDateTime.now(ZONE).format(DT);
         System.out.println("成功恢复到网络时间: " + now + " (方法: " + method + ")");
     } else {
         System.err.println("所有方法均失败，未能恢复网络时间。请检查网络连接和NTP服务（chronyd/ntpd）配置。");
     }

     // 清理状态文件
     try { Files.deleteIfExists(STATE_FILE); } catch (IOException ignored) {}
 }

 /**
  * 轮询检查 timedatectl 状态是否已同步。
  * @param timeoutSeconds 等待的秒数
  * @return 如果在超时时间内同步成功，返回 true
  */
 private static boolean checkTimedatectlSync(int timeoutSeconds) throws InterruptedException {
     long endTime = System.currentTimeMillis() + timeoutSeconds * 1000L;
     while (System.currentTimeMillis() < endTime) {
         NtpState ntpState = getNtpState();
         if (ntpState.synchronizedNow) {
             System.out.println("timedatectl 确认状态为：NTP 已同步。");
             return true;
         }
         Thread.sleep(1000);
     }
     return false;
 }
 

    // ---- chrony / ntp helpers ----

    private static boolean chronyResponsive() {
        try {
            CommandResult r = run("chronyc", "-n", "tracking");
            return r.exitCode == 0 && r.stdout.toLowerCase(Locale.ROOT).contains("reference");
        } catch (IOException e) {
            return false;
        }
    }

    private static void startChronydIfPossible() {
        if (!hasCommand("systemctl", "--version")) return;
        try {
            CommandResult st = run("systemctl", "is-active", "chronyd");
            if (!st.stdout.trim().equalsIgnoreCase("active")) {
                runIgnore("systemctl", "start", "chronyd");
                Thread.sleep(1000);
            }
        } catch (Exception ignored) {}
    }

    private static boolean chronyWaitSync(int timeoutSec, double thresholdSec) {
        try {
            CommandResult r = run("chronyc", "-a", "waitsync", String.valueOf(timeoutSec), String.valueOf(thresholdSec));
            return r.exitCode == 0 && r.stdout.contains("200 OK");
        } catch (IOException e) {
            return false;
        }
    }

    private static boolean pollChronyTrackingForSync(int seconds) {
        long end = System.currentTimeMillis() + seconds * 1000L;
        while (System.currentTimeMillis() < end) {
            try {
                CommandResult r = run("chronyc", "-n", "tracking");
                String s = r.stdout.toLowerCase(Locale.ROOT);
                boolean leapNormal = s.contains("leap status") && s.contains("normal");
                if (leapNormal) return true;
                Thread.sleep(1000);
            } catch (Exception ignored) {}
        }
        return false;
    }

    private static Instant querySntpFirstSuccess(List<String> servers) {
        for (String s : servers) {
            try {
                Instant t = querySntpOnce(s, 2000);
                if (t != null) return t;
            } catch (Exception ignored) {}
        }
        return null;
    }

    private static Instant querySntpOnce(String server, int timeoutMs) throws Exception {
        byte[] buf = new byte[48];
        buf[0] = 0b00_011_011; // LI=0, VN=3, Mode=3 (client)
        InetAddress addr = InetAddress.getByName(server);
        DatagramSocket socket = new DatagramSocket();
        socket.setSoTimeout(timeoutMs);
        DatagramPacket req = new DatagramPacket(buf, buf.length, addr, 123);
        socket.send(req);

        DatagramPacket resp = new DatagramPacket(new byte[48], 48);
        socket.receive(resp);
        byte[] data = resp.getData();
        if (data.length < 48) return null;

        long seconds = readUnsignedInt(data, 40);
        long fraction = readUnsignedInt(data, 44);
        long ntpTime = ((seconds - 2208988800L) * 1000L) + ((fraction * 1000L) >>> 32);
        socket.close();
        return Instant.ofEpochMilli(ntpTime);
    }

    private static long readUnsignedInt(byte[] b, int off) {
        return ((b[off] & 0xffL) << 24) | ((b[off + 1] & 0xffL) << 16) | ((b[off + 2] & 0xffL) << 8) | (b[off + 3] & 0xffL);
    }

    private static List<String> serversList() {
        return Arrays.asList(getNtpServers());
    }

    private static String[] getNtpServers() {
        String fromProp = System.getProperty("ntp.servers", "").trim();
        String fromEnv = System.getenv("NTP_SERVERS");
        String raw = !fromProp.isEmpty() ? fromProp : (fromEnv != null ? fromEnv.trim() : "");
        List<String> out = new ArrayList<>();
        if (!raw.isEmpty()) {
            String[] parts = raw.split("[,\\s]+");
            for (String s : parts) if (!s.isEmpty()) out.add(s);
        }
        if (out.isEmpty()) {
            out.addAll(Arrays.asList("pool.ntp.org", "time.cloudflare.com", "ntp.aliyun.com", "ntp.tencent.com"));
        }
        return out.toArray(new String[0]);
    }

    // ---- system time helpers ----

    private static void applySystemTime(String formattedLocalTime, Tool tool) throws Exception {
        if (tool.preferred == ToolKind.TIMEDATECTL) {
            CommandResult r = run("timedatectl", "set-time", formattedLocalTime);
            if (r.exitCode != 0) throw new RuntimeException("timedatectl 设置失败: " + r.stderr.trim());
        } else if (tool.preferred == ToolKind.DATE) {
            CommandResult r = run("date", "-s", formattedLocalTime);
            if (r.exitCode != 0) throw new RuntimeException("date -s 设置失败: " + r.stderr.trim());
        } else {
            throw new IllegalStateException("无可用工具设置系统时间。");
        }
    }

    private static void tryHwclockSync() {
        try {
            CommandResult r = run("hwclock", "--systohc");
            if (r.exitCode == 0) System.out.println("已同步系统时间至硬件时钟（hwclock --systohc）。");
        } catch (IOException ignored) {}
    }

    private static NtpState getNtpState() {
        try {
            CommandResult r = run("timedatectl", "show", "-p", "NTP", "-p", "NTPSynchronized");
            boolean enabled = false;
            boolean synced = false;
            for (String line : r.stdout.split("\n")) {
                String ln = line.trim();
                if (ln.startsWith("NTP=")) {
                    String v = ln.substring(4).trim();
                    enabled = v.equalsIgnoreCase("yes") || v.equalsIgnoreCase("true");
                } else if (ln.startsWith("NTPSynchronized=")) {
                    String v = ln.substring("NTPSynchronized=".length()).trim();
                    synced = v.equalsIgnoreCase("yes") || v.equalsIgnoreCase("true");
                }
            }
            return new NtpState(enabled, synced);
        } catch (Exception e) {
            return new NtpState(false, false);
        }
    }

    private static Tool detectTool() {
        boolean hasTimedatectl = hasCommand("timedatectl", "status");
        boolean hasDate = hasCommand("date", "--version") || hasCommand("date", "-v");
        boolean hasChronyc = hasCommand("chronyc", "-n");
        boolean hasNtpdate = hasCommand("ntpdate", "-u");
        ToolKind preferred = ToolKind.NONE;
        if (hasTimedatectl) preferred = ToolKind.TIMEDATECTL;
        else if (hasDate) preferred = ToolKind.DATE;
        return new Tool(hasTimedatectl, hasDate, hasChronyc, hasNtpdate, preferred);
    }

 private static boolean hasCommand(String cmd, String argForCheck) {
     // 检查参数有效性，防止 ProcessBuilder 抛出 NullPointerException
     if (cmd == null || cmd.isEmpty()) {
         return false;
     }

     try {
         ProcessBuilder pb = (argForCheck == null || argForCheck.isEmpty())
                            ? new ProcessBuilder(cmd)
                            : new ProcessBuilder(cmd, argForCheck);

         Process p = pb.redirectErrorStream(true).start();

         // **关键修复**：必须启动一个独立的线程来消耗掉进程的输出流。
         // 否则，如果输出内容填满缓冲区，进程会阻塞，导致下面的 waitFor() 卡死。
         Thread streamGobbler = new Thread(() -> {
             try (java.io.InputStream in = p.getInputStream()) {
                 // 创建一个缓冲区来读取数据
                 byte[] buffer = new byte[1024];
                 // 持续读取流中的数据，直到流结束。我们不需要处理这些数据，
                 // 目的仅仅是"清空"管道，防止子进程被阻塞。
                 while (in.read(buffer) != -1) {
                     // Do nothing, just consume the stream
                 }
             } catch (IOException ignored) {
                 // 忽略读取时可能发生的异常
             }
         });
         streamGobbler.setDaemon(true); // 设置为守护线程，这样主程序退出时它也会退出
         streamGobbler.start();

         // 等待命令执行完毕
         boolean exited = p.waitFor(5, java.util.concurrent.TimeUnit.SECONDS); // 最多等待5秒，防止无限等待

         // 如果进程在5秒内没有执行完，也认为命令有问题或系统有问题
         if (!exited) {
             p.destroyForcibly(); // 强制销毁进程
             return false;
         }

         // 等待我们的消耗线程也执行完毕，确保所有输出都被处理
         streamGobbler.join(1000); // 最多再等1秒

         // 只要进程能正常退出（exit code 为 0），就认为命令存在且可用
         // 对于某些检查命令，非0退出码也可能意味着命令存在但执行失败，
         // 但对于 "has command" 的目的，能成功启动并结束就足够了。
         // 这里的判断可以根据需要放宽，但对于 status/version 这类检查，0通常是正确的。
         return p.exitValue() == 0;

     } catch (IOException e) {
         // 如果 ProcessBuilder 启动时就找不到命令，会抛出 IOException (e.g., "error=2, No such file or directory")
         // 这是最明确的“命令不存在”的信号。
         return false;
     } catch (InterruptedException e) {
         // 如果当前线程在等待时被中断
         Thread.currentThread().interrupt(); // 重新设置中断状态
         return false;
     }
 }

    private static void ensureRoot() throws NeedsRootException {
        try {
            CommandResult r = run("id", "-u");
            if (r.exitCode == 0 && r.stdout.trim().equals("0")) return;
            throw new NeedsRootException("当前用户不是 root。id -u=" + r.stdout.trim());
        } catch (IOException e) {
            throw new NeedsRootException("无法确认权限（id 命令不可用）。");
        }
    }

    private static void maybeSaveOriginalState() throws Exception {
        if (Files.exists(STATE_FILE)) return;

        Tool tool = detectTool();
        String now = ZonedDateTime.now(ZONE).format(DT);
        String toolName = (tool.preferred == ToolKind.TIMEDATECTL ? "timedatectl" :
                (tool.preferred == ToolKind.DATE ? "date" : "none"));
        boolean ntpEnabled = false;
        if (tool.hasTimedatectl) ntpEnabled = getNtpState().enabled;

        Properties p = new Properties();
        p.setProperty("savedAt", now);
        p.setProperty("toolWhenSaved", toolName);
        p.setProperty("ntpOriginally", Boolean.toString(ntpEnabled));

        try {
            if (!Files.exists(STATE_DIR)) Files.createDirectories(STATE_DIR);
            try (OutputStream out = Files.newOutputStream(STATE_FILE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                p.store(out, "CentosTimeShift saved state");
            }
            try {
                Set<PosixFilePermission> perms = EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE);
                Files.setPosixFilePermissions(STATE_FILE, perms);
            } catch (Exception ignored) {}
            System.out.println("已保存原始 NTP 状态于: " + STATE_FILE);
        } catch (IOException e) {
            System.err.println("警告：无法保存状态（不影响时间调整）： " + e.getMessage());
        }
    }

    private static Properties loadProps(Path path) throws IOException {
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(path)) {
            p.load(in);
        }
        return p;
    }

    private static class NtpState {
        final boolean enabled;
        final boolean synchronizedNow;
        NtpState(boolean e, boolean s) { this.enabled = e; this.synchronizedNow = s; }
    }

    private enum ToolKind { TIMEDATECTL, DATE, NONE }

    private static class Tool {
        final boolean hasTimedatectl;
        final boolean hasDate;
        final boolean hasChronyc;
        final boolean hasNtpdate;
        final ToolKind preferred;
        Tool(boolean t, boolean d, boolean c, boolean n, ToolKind p) {
            this.hasTimedatectl = t;
            this.hasDate = d;
            this.hasChronyc = c;
            this.hasNtpdate = n;
            this.preferred = p;
        }
    }

    private static class CommandResult {
        int exitCode;
        String stdout;
        String stderr;
    }

    private static CommandResult run(String... cmd) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(cmd);
        Process p = pb.start();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();

        Thread t1 = pipe(p.getInputStream(), out);
        Thread t2 = pipe(p.getErrorStream(), err);

        try {
            int code = p.waitFor();
            t1.join();
            t2.join();
            CommandResult r = new CommandResult();
            r.exitCode = code;
            r.stdout = out.toString(StandardCharsets.UTF_8.name());
            r.stderr = err.toString(StandardCharsets.UTF_8.name());
            return r;
        } catch (InterruptedException e) {
            p.destroyForcibly();
            Thread.currentThread().interrupt();
            throw new IOException("命令中断: " + String.join(" ", cmd));
        }
    }

    private static void runIgnore(String... cmd) {
        try { run(cmd); } catch (IOException ignored) {}
    }

    private static Thread pipe(InputStream in, OutputStream out) {
        Thread t = new Thread(() -> {
            byte[] buf = new byte[4096];
            int n;
            try {
                while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
            } catch (IOException ignored) {}
        });
        t.setDaemon(true);
        t.start();
        return t;
    }

    // ---- Shift parsing ----

    private static class Shift {
        boolean negative;
        int months, weeks, days, hours, minutes, seconds;
    }

    private static Shift parseShift(String s) {
        if (s == null || s.trim().isEmpty())
            throw new IllegalArgumentException("偏移字符串为空。示例：+2h30m 或 -1d 或 1w2d3h");
        s = s.trim();

        Shift result = new Shift();
        if (s.startsWith("+")) { result.negative = false; s = s.substring(1); }
        else if (s.startsWith("-")) { result.negative = true; s = s.substring(1); }

        s = s.replaceAll("\\s+", "");
        Pattern token = Pattern.compile("(\\d+)(mo|mon|month|months|月|w|week|weeks|周|d|day|days|天|h|hour|hours|小时|m|min|minute|minutes|分|分钟|s|sec|second|seconds|秒)", Pattern.CASE_INSENSITIVE);
        Matcher m = token.matcher(s);
        int lastEnd = 0, matchedCount = 0;
        while (m.find()) {
            if (m.start() != lastEnd) throw new IllegalArgumentException("无法解析偏移片段: " + s.substring(lastEnd, m.start()));
            int val = Integer.parseInt(m.group(1));
            String unit = m.group(2).toLowerCase(Locale.ROOT);
            switch (unit) {
                case "mo": case "mon": case "month": case "months": case "月": result.months += val; break;
                case "w": case "week": case "weeks": case "周": result.weeks += val; break;
                case "d": case "day": case "days": case "天": result.days += val; break;
                case "h": case "hour": case "hours": case "小时": result.hours += val; break;
                case "m": case "min": case "minute": case "minutes": case "分": case "分钟": result.minutes += val; break;
                case "s": case "sec": case "second": case "seconds": case "秒": result.seconds += val; break;
                default: throw new IllegalArgumentException("未知单位: " + unit);
            }
            lastEnd = m.end();
            matchedCount++;
        }
        if (matchedCount == 0 || lastEnd != s.length())
            throw new IllegalArgumentException("偏移格式不正确。示例：+1w2d3h 或 -90m 或 2mo");
        return result;
    }

    private static ZonedDateTime applyShift(ZonedDateTime base, Shift shift) {
        int sign = shift.negative ? -1 : 1;
        ZonedDateTime t = base;
        if (shift.months != 0) t = t.plusMonths(sign * shift.months);
        if (shift.weeks != 0) t = t.plusWeeks(sign * shift.weeks);
        if (shift.days != 0) t = t.plusDays(sign * shift.days);
        if (shift.hours != 0) t = t.plusHours(sign * shift.hours);
        if (shift.minutes != 0) t = t.plusMinutes(sign * shift.minutes);
        if (shift.seconds != 0) t = t.plusSeconds(sign * shift.seconds);
        return t;
    }

    // ---- misc ----

    private static String joinArgs(String[] arr) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) sb.append(' ');
            sb.append(arr[i]);
        }
        return sb.toString();
    }

    private static class NeedsRootException extends Exception {
        NeedsRootException(String msg) { super(msg); }
    }
}