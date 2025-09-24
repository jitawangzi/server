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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jna.*;

/**
 * - 当环境禁止 exec 外部命令（spawn helper 失败）时，自动回退为“内核直调 clock_settime”，不再依赖 timedatectl/date。
 * - 需要 root 或 CAP_SYS_TIME。若缺少权限会返回 EPERM。
 */
public class LinuxTimeShift {
private static final Logger LOGGER = LoggerFactory.getLogger(LinuxTimeShift.class);
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
        System.out.println("  sudo java cn.game.util.SystemTimeShift 1d");
        System.out.println("  sudo java cn.game.util.SystemTimeShift 2h30m");
        System.out.println("  sudo java cn.game.util.SystemTimeShift +2h30m");
        System.out.println("  sudo java cn.game.util.SystemTimeShift -1d");
        System.out.println("  sudo java cn.game.util.SystemTimeShift add 1w2d3h");
        System.out.println("  sudo java cn.game.util.SystemTimeShift set \"2025-01-01 00:00:00\"");
        System.out.println("  sudo java cn.game.util.SystemTimeShift set 23:59[:ss]    仅设置当天时间，日期不变");
        System.out.println("  也支持不带前导零的日期：set \"2022-1-1 23:59:50\"");
        System.out.println("  sudo java cn.game.util.SystemTimeShift restore   恢复到当前网络时间（chrony > ntpdate > SNTP）");
        System.out.println("  sudo java cn.game.util.SystemTimeShift status");
        System.out.println("单位：s 秒, m 分钟, h 小时, d 天, w 周, mo 月（m=分钟，mo=月份）");
        System.out.println("环境变量/属性：NTP_SERVERS 或 -Dntp.servers=\"server1 server2\"");
        System.out.println("默认NTP：pool.ntp.org time.cloudflare.com ntp.aliyun.com ntp.tencent.com");
    }

    // ---- 预估功能：仅计算，不改系统时间 ----

    /**
     * 预估“若按给定参数执行”后的目标时间（不修改系统时间）。
     * 支持与 main 相同的参数形态：set / add / sub / +offset / -offset / 无符号偏移。
     * 不支持：restore/status/help（会抛出 IllegalArgumentException）。
     *
     * @param inputArgs 与命令行相同的参数数组
     * @return 预估结果，包含 now、target、是否跨天等
     */
    public static PreviewResult previewPlannedTime(String[] inputArgs) {
        if (inputArgs == null || inputArgs.length == 0) {
            throw new IllegalArgumentException("缺少参数用于预估。");
        }
        String first = inputArgs[0].trim();
        String joinedAll = joinArgs(inputArgs);
        ZonedDateTime now = ZonedDateTime.now(ZONE);
        ZonedDateTime target;
        String mode;

        if ("help".equalsIgnoreCase(first) || "--help".equalsIgnoreCase(first) || "-h".equalsIgnoreCase(first)
                || "status".equalsIgnoreCase(first) || "restore".equalsIgnoreCase(first)) {
//            throw new IllegalArgumentException("预估不支持命令：" + first);
        	LOGGER.error("预估不支持命令：" + first);
        	return null; 
        }

        if ("set".equalsIgnoreCase(first)) {
            if (inputArgs.length < 2) {
                throw new IllegalArgumentException("set 预估缺少时间参数，例如：set \"2025-01-01 00:00:00\"");
            }
            String targetStr = joinArgs(Arrays.copyOfRange(inputArgs, 1, inputArgs.length)).trim().replace('T', ' ');
            LocalDateTime ldt = parseLocalDateTimeFlexible(targetStr);
            target = ZonedDateTime.of(ldt, ZONE);
            mode = "set";
        } else if ("add".equalsIgnoreCase(first) || "sub".equalsIgnoreCase(first)) {
            if (inputArgs.length < 2) {
                throw new IllegalArgumentException(first + " 预估缺少偏移参数，例如：add 1h30m 或 sub 2d");
            }
            String offset = joinArgs(Arrays.copyOfRange(inputArgs, 1, inputArgs.length));
            Shift shift = parseShift(offset);
            if ("sub".equalsIgnoreCase(first)) shift.negative = true;
            target = applyShift(now, shift);
            mode = "shift";
        } else if (first.startsWith("+") || first.startsWith("-")) {
            String offset = joinArgs(inputArgs);
            Shift shift = parseShift(offset);
            target = applyShift(now, shift);
            mode = "shift";
        } else {
            // 尝试按“无符号偏移（默认增加）”解析
            String candidate = joinArgs(inputArgs);
            Shift shift = parseShift(candidate); // 无符号 → parseShift 返回 negative=false
            target = applyShift(now, shift);
            mode = "shift";
        }

        LocalDate dNow = now.toLocalDate();
        LocalDate dTarget = target.toLocalDate();
        boolean crossedDay = !dNow.equals(dTarget);
        long dayDelta = ChronoUnit.DAYS.between(dNow, dTarget);

        return new PreviewResult(now, target, crossedDay, dayDelta, mode, joinedAll);
    }

    /**
     * 预估结果载体。
     */
    public static final class PreviewResult {
        public final ZonedDateTime now;
        public final ZonedDateTime target;
        public final boolean crossedDay;
        public final long dayDelta;
        public final String mode; // "set" 或 "shift"
        public final String normalizedInput;

        public PreviewResult(ZonedDateTime now, ZonedDateTime target, boolean crossedDay, long dayDelta, String mode, String normalizedInput) {
            this.now = now;
            this.target = target;
            this.crossedDay = crossedDay;
            this.dayDelta = dayDelta;
            this.mode = mode;
            this.normalizedInput = normalizedInput;
        }

        @Override
        public String toString() {
            return "PreviewResult{" +
                    "now=" + now.format(DT) +
                    ", target=" + target.format(DT) +
                    ", crossedDay=" + crossedDay +
                    ", dayDelta=" + dayDelta +
                    ", mode='" + mode + '\'' +
                    ", input='" + normalizedInput + '\'' +
                    '}';
        }
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
        LocalDateTime ldt = parseLocalDateTimeFlexible(normalized);
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

        // 1) 尝试 timedatectl set-ntp（仅在可用时，且不因异常中断）
        if (tool.hasTimedatectl && ntpOriginally) {
            System.out.println("尝试通过 timedatectl set-ntp true 恢复时间同步...");
            try {
                run("timedatectl", "set-ntp", "false");
                Thread.sleep(500);
                CommandResult r = run("timedatectl", "set-ntp", "true");
                if (r.exitCode == 0) {
                    if (checkTimedatectlSync(15)) {
                        success = true;
                        method = "timedatectl set-ntp";
                    } else {
                        System.out.println("timedatectl 已开启NTP，但短时内未确认同步成功。系统将在后台继续尝试。");
                        success = true;
                        method = "timedatectl set-ntp (async)";
                    }
                }
            } catch (Exception e) {
                System.out.println("通过 timedatectl 恢复失败: " + e.getMessage());
            }
        }

        // 2) 尝试 chronyc（仅在可通信时，否则跳过；异常不终止）
        if (!success && tool.hasChronyc) {
            System.out.println("尝试通过 chronyc 手动同步...");
            try {
                if (!chronyResponsive()) startChronydIfPossible();
                if (chronyResponsive()) {
                    runIgnore("chronyc", "-a", "online");
                    runIgnore("chronyc", "-a", "burst", "4/4");
                    System.out.println("等待 chrony 与服务器通信...");
                    Thread.sleep(3000);

                    CommandResult step = run("chronyc", "-a", "makestep");
                    boolean ok = step.exitCode == 0 &&
                            (step.stdout.contains("200 OK") || step.stderr.contains("200 OK"));

                    if (ok) {
                        success = true;
                        method = "chronyc makestep";
                        if (!ntpOriginally) {
                            System.out.println("NTP 原本为关闭状态，将 chrony 恢复为 offline。");
                            runIgnore("chronyc", "-a", "offline");
                        }
                    } else {
                        System.out.println("chronyc makestep 未确认成功。 stdout: " + step.stdout.trim() + ", stderr: " + step.stderr.trim());
                    }
                }
            } catch (Exception e) {
                System.out.println("chronyc 尝试失败: " + e.getMessage());
            }
        }

        // 3) 尝试 ntpdate（异常不终止）
        if (!success && tool.hasNtpdate) {
            System.out.println("尝试通过 ntpdate 同步...");
            try {
                List<String> args = new ArrayList<>();
                args.add("ntpdate");
                args.add("-u");
                args.addAll(Arrays.asList(getNtpServers()));
                CommandResult r = run(args.toArray(new String[0]));
                if (r.exitCode == 0) {
                    success = true;
                    method = "ntpdate";
                } else {
                    System.out.println("ntpdate 对时失败，stderr: " + r.stderr.trim());
                }
            } catch (Exception e) {
                System.out.println("ntpdate 执行失败: " + e.getMessage());
            }
        }

        // 4) 内置 SNTP + clock_settime（不依赖任何外部命令）
        if (!success) {
            System.out.println("尝试通过内置 SNTP 客户端同步...");
            Instant ntpInstant = querySntpFirstSuccess(serversList());
            if (ntpInstant != null) {
                // 直接使用 clock_settime，不再依赖 date/timedatectl
                ZonedDateTime z = ZonedDateTime.ofInstant(ntpInstant, ZONE);
                try {
                    setSystemTimeViaClockSettime(z);
                    success = true;
                    method = "SNTP 内置客户端 + clock_settime";
                } catch (Exception e) {
                    System.out.println("clock_settime 设置失败: " + e.getMessage());
                }
            } else {
                System.out.println("内置 SNTP 客户端获取时间失败。");
            }
        }

        if (success) {
            tryHwclockSync(); // 如果 hwclock 也不能 exec，则 silently ignore
            String now = ZonedDateTime.now(ZONE).format(DT);
            System.out.println("成功恢复到网络时间: " + now + " (方法: " + method + ")");
            try { Files.deleteIfExists(STATE_FILE); } catch (IOException ignored) {}
        } else {
            System.err.println("所有方法均失败，未能恢复网络时间。请检查网络连接与防火墙，或为容器授予网络访问。");
        }
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
        if (!hasCommandExecutable("systemctl")) return;
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
                Thread.sleep(1_000);
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
        if (tool.preferred == ToolKind.DATE) {
            try {
                CommandResult r = run("date", "-s", formattedLocalTime);
                if (r.exitCode == 0) return;
                if (tool.hasTimedatectl) {
                    try {
                        CommandResult r2 = run("timedatectl", "set-time", formattedLocalTime);
                        if (r2.exitCode == 0) return;
                        throw new RuntimeException("timedatectl 设置失败: " + r2.stderr.trim());
                    } catch (Exception e2) {
                        LocalDateTime ldt = LocalDateTime.parse(formattedLocalTime, DT);
                        ZonedDateTime z = ZonedDateTime.of(ldt, ZONE);
                        setSystemTimeViaClockSettime(z);
                        return;
                    }
                }
                LocalDateTime ldt = LocalDateTime.parse(formattedLocalTime, DT);
                ZonedDateTime z = ZonedDateTime.of(ldt, ZONE);
                setSystemTimeViaClockSettime(z);
                return;
            } catch (Exception e) {
                try {
                    if (tool.hasTimedatectl) {
                        CommandResult r2 = run("timedatectl", "set-time", formattedLocalTime);
                        if (r2.exitCode == 0) return;
                    }
                } catch (Exception ignored) {}
                LocalDateTime ldt = LocalDateTime.parse(formattedLocalTime, DT);
                ZonedDateTime z = ZonedDateTime.of(ldt, ZONE);
                setSystemTimeViaClockSettime(z);
                return;
            }
        } else if (tool.preferred == ToolKind.TIMEDATECTL) {
            try {
                CommandResult r = run("timedatectl", "set-time", formattedLocalTime);
                if (r.exitCode == 0) return;
                if (tool.hasDate) {
                    try {
                        CommandResult r2 = run("date", "-s", formattedLocalTime);
                        if (r2.exitCode == 0) return;
                        throw new RuntimeException("date -s 设置失败: " + r2.stderr.trim());
                    } catch (Exception e2) {
                        LocalDateTime ldt = LocalDateTime.parse(formattedLocalTime, DT);
                        ZonedDateTime z = ZonedDateTime.of(ldt, ZONE);
                        setSystemTimeViaClockSettime(z);
                        return;
                    }
                }
                LocalDateTime ldt = LocalDateTime.parse(formattedLocalTime, DT);
                ZonedDateTime z = ZonedDateTime.of(ldt, ZONE);
                setSystemTimeViaClockSettime(z);
                return;
            } catch (Exception e) {
                try {
                    if (tool.hasDate) {
                        CommandResult r2 = run("date", "-s", formattedLocalTime);
                        if (r2.exitCode == 0) return;
                    }
                } catch (Exception ignored) {}
                LocalDateTime ldt = LocalDateTime.parse(formattedLocalTime, DT);
                ZonedDateTime z = ZonedDateTime.of(ldt, ZONE);
                setSystemTimeViaClockSettime(z);
                return;
            }
        } else {
            LocalDateTime ldt = LocalDateTime.parse(formattedLocalTime, DT);
            ZonedDateTime z = ZonedDateTime.of(ldt, ZONE);
            setSystemTimeViaClockSettime(z);
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
        boolean hasTimedatectl = hasCommandExecutable("timedatectl");
        boolean hasDate = hasCommandExecutable("date");
        boolean hasChronyc = hasCommandExecutable("chronyc");
        boolean hasNtpdate = hasCommandExecutable("ntpdate");
        ToolKind preferred = ToolKind.NONE;
        if (hasDate) preferred = ToolKind.DATE;
        else if (hasTimedatectl) preferred = ToolKind.TIMEDATECTL;
        return new Tool(hasTimedatectl, hasDate, hasChronyc, hasNtpdate, preferred);
    }

    private static boolean hasCommandExecutable(String cmd) {
        if (cmd == null || cmd.isEmpty()) return false;

        Path p0 = Paths.get(cmd);
        if (p0.isAbsolute()) {
            return Files.isExecutable(p0);
        }

        String[] common = {"/usr/bin", "/bin", "/usr/sbin", "/sbin", "/usr/local/bin", "/usr/local/sbin"};
        for (String base : common) {
            Path p = Paths.get(base, cmd);
            if (Files.isExecutable(p)) return true;
        }

        String path = System.getenv("PATH");
        if (path != null) {
            for (String dir : path.split(":")) {
                if (dir == null || dir.isEmpty()) continue;
                Path p = Paths.get(dir, cmd);
                if (Files.isExecutable(p)) return true;
            }
        }
        return false;
    }

    private static boolean hasCommand(String cmd, String argForCheck) {
        return hasCommandExecutable(cmd);
    }

    private static void ensureRoot() throws NeedsRootException {
        try {
            String status = Files.readString(Paths.get("/proc/self/status"));
            for (String line : status.split("\n")) {
                if (line.startsWith("Uid:")) {
                    String[] parts = line.replace('\t', ' ').trim().split("\\s+");
                    if (parts.length >= 2 && "0".equals(parts[1])) {
                        return;
                    }
                    break;
                }
            }
        } catch (IOException ignore) {}

        try {
            CommandResult r = run("/usr/bin/id", "-u");
            if (r.exitCode == 0 && r.stdout.trim().equals("0")) return;
        } catch (IOException ignore) {}

        try {
            CommandResult r = run("id", "-u");
            if (r.exitCode == 0 && r.stdout.trim().equals("0")) return;
        } catch (IOException ignore) {}

        String user = System.getProperty("user.name", "");
        if ("root".equals(user)) return;

        String path = System.getenv("PATH");
        throw new NeedsRootException("未能确认 root 身份。已检查 /proc/self/status、/usr/bin/id 和 id。PATH=" + path);
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

    // ---------- 进程执行增强：候选绝对路径 + 回退 ----------
    private static String[] resolveExecCandidates(String cmd) {
        List<String> cands = new ArrayList<>();
        Path p0 = Paths.get(cmd);
        if (p0.isAbsolute()) {
            cands.add(cmd);
        } else {
            String[] common = {"/usr/bin", "/bin", "/usr/sbin", "/sbin", "/usr/local/bin", "/usr/local/sbin"};
            for (String base : common) {
                Path p = Paths.get(base, cmd);
                if (Files.isExecutable(p)) {
                    cands.add(p.toString());
                }
            }
            cands.add(cmd);
        }
        return cands.toArray(new String[0]);
    }

    private static CommandResult runExec(String cmd, String... args) throws IOException {
        String[] cands = resolveExecCandidates(cmd);
        List<String> errors = new ArrayList<>();
        IOException last = null;

        for (String exe : cands) {
            try {
                List<String> cmdline = new ArrayList<>();
                cmdline.add(exe);
                cmdline.addAll(Arrays.asList(args));
                ProcessBuilder pb = new ProcessBuilder(cmdline);
                Process p = pb.start();

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                ByteArrayOutputStream err = new ByteArrayOutputStream();
                Thread t1 = pipe(p.getInputStream(), out);
                Thread t2 = pipe(p.getErrorStream(), err);
                int code = p.waitFor();
                t1.join();
                t2.join();

                CommandResult r = new CommandResult();
                r.exitCode = code;
                r.stdout = out.toString(StandardCharsets.UTF_8.name());
                r.stderr = err.toString(StandardCharsets.UTF_8.name());
                return r;
            } catch (IOException ioe) {
                last = ioe;
                errors.add(exe + ": " + ioe.getMessage());
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                IOException ioe = new IOException("命令中断: " + exe);
                last = ioe;
                errors.add(exe + ": " + ioe.getMessage());
            }
        }
        String msg = "所有可执行路径均启动失败: " + String.join(" | ", errors);
        if (last != null) {
            throw new IOException(msg, last);
        }
        throw new IOException(msg);
    }

    private static CommandResult run(String... cmd) throws IOException {
        if (cmd == null || cmd.length == 0) throw new IOException("空命令");
        String bin = cmd[0];
        String[] args = Arrays.copyOfRange(cmd, 1, cmd.length);
        return runExec(bin, args);
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
    // ---------- 进程执行增强结束 ----------

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

    private static LocalDateTime parseLocalDateTimeFlexible(String dateTime) {
        String s = dateTime == null ? "" : dateTime.trim().replace('T', ' ');
        if (s.isEmpty()) {
            throw new IllegalArgumentException("时间字符串为空");
        }

        // 统一空白、分隔符
        s = s.replaceAll("\\s+", " ");          // 折叠多空格
        String s2 = s.replace('/', '-').replace('.', '-'); // 兼容 2022/1/1 或 2022.1.1

        // 1) 仅时间：H:m 或 H:m:s（日期保持当天）
        try {
            LocalTime lt = LocalTime.parse(s2, DateTimeFormatter.ofPattern("H:m[:s]"));
            return LocalDateTime.of(ZonedDateTime.now(ZONE).toLocalDate(), lt);
        } catch (Exception ignore) {
            // 不是仅时间格式，继续尝试完整日期时间
        }

        // 2) 宽松的日期时间：允许不带前导零（yyyy-M-d H:m 或 H:m:s）
        try {
            return LocalDateTime.parse(s2, DateTimeFormatter.ofPattern("yyyy-M-d H:m[:s]"));
        } catch (Exception ignore) {
            // 3) 回退到你原先的严格格式（保留兼容）
            try {
                return LocalDateTime.parse(s2, DT); // yyyy-MM-dd HH:mm:ss
            } catch (Exception e2) {
                // 4) 再次回退：严格但秒可选
                try {
                    return LocalDateTime.parse(s2, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm[:ss]"));
                } catch (Exception e3) {
                    throw new IllegalArgumentException(
                        "无法解析时间。支持格式示例：\n" +
                        "  - yyyy-M-d H:m[:s]（如 2022-1-1 23:59:50）\n" +
                        "  - yyyy-MM-dd HH:mm[:ss]\n" +
                        "  - HH:mm[:ss]（仅设置当天时间，日期保持不变）\n" +
                        "例如：set \"2022-1-1 23:59:50\" 或 set 23:59:30"
                    );
                }
            }
        }
    }
    private static class NeedsRootException extends Exception {
        NeedsRootException(String msg) { super(msg); }
    }

    // ---------- 纯 Java 内核直调：clock_settime ----------
    private interface CLib extends Library {
        CLib INSTANCE = Native.load("c", CLib.class);
        // struct timespec { time_t tv_sec; long tv_nsec; }
        public static class Timespec extends Structure {
            public NativeLong tv_sec; // time_t
            public NativeLong tv_nsec;
            @Override
            protected List<String> getFieldOrder() {
                return Arrays.asList("tv_sec", "tv_nsec");
            }
        }
        int clock_settime(int clk_id, Timespec tp);
    }

    private static final int CLOCK_REALTIME = 0;

    private static void setSystemTimeViaClockSettime(ZonedDateTime targetLocal) throws Exception {
        // 说明：targetLocal 是“本地时区”的目标时间。clock_settime 接收的是 UTC epoch 秒。
        Instant instant = targetLocal.toInstant();
        long sec = instant.getEpochSecond();
        long nsec = instant.getNano();

        CLib.Timespec ts = new CLib.Timespec();
        ts.tv_sec = new NativeLong(sec);
        ts.tv_nsec = new NativeLong(nsec);

        int rc = CLib.INSTANCE.clock_settime(CLOCK_REALTIME, ts);
        if (rc != 0) {
            int errno = Native.getLastError();
            String msg;
            switch (errno) {
                case 1: msg = "EPERM（缺少 root/CAP_SYS_TIME）"; break;
                case 22: msg = "EINVAL（参数错误）"; break;
                default: msg = "errno=" + errno;
            }
            throw new RuntimeException("clock_settime 失败: " + msg);
        }
    }
    // ---------- 纯 Java 内核直调结束 ----------
}