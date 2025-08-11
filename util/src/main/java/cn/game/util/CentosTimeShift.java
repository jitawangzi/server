package cn.game.util;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermission;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CentosTimeShift
 * - 支持通过相对偏移（+/- 或无符号默认增加）或绝对时间设置系统时间
 * - 支持单位：秒/分/时/天/周/月（m=分钟，mo=月份）
 * - 自动保存原始时间与 NTP 状态，可恢复
 * - 优先使用 timedatectl，回退到 date -s，尝试同步硬件时钟 hwclock --systohc
 *
 * 用法示例：
 *   sudo java CentosTimeShift 1d                默认增加1天
 *   sudo java CentosTimeShift 2h30m             默认增加2小时30分
 *   sudo java CentosTimeShift +2h30m            显式增加
 *   sudo java CentosTimeShift -1d               显式减少
 *   sudo java CentosTimeShift add 1w2d3h        增加1周2天3小时
 *   sudo java CentosTimeShift add 1h 30m        支持空格分隔
 *   sudo java CentosTimeShift +1h 30m           支持空格分隔
 *   sudo java CentosTimeShift set "2025-01-01 00:00:00"
 *   sudo java CentosTimeShift restore
 *   sudo java CentosTimeShift status
 *   java  CentosTimeShift help
 */
public class CentosTimeShift {

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

            // 直接使用 +offset 或 -offset（允许空格分隔片段）
            if (first.startsWith("+") || first.startsWith("-")) {
                String offset = joinArgs(args);
                shiftByOffset(offset, false);
                return;
            }

            // 无符号偏移：默认增加（支持空格分隔）
            String candidate = joinArgs(args);
            try {
                // 尝试解析，无异常则当作偏移处理
                parseShift(candidate);
                shiftByOffset(candidate, false);
                return;
            } catch (IllegalArgumentException ignored) {
                // 不是合法偏移，继续 fallthrough
            }

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
        System.out.println("  sudo java CentosTimeShift 1d              默认增加1天");
        System.out.println("  sudo java CentosTimeShift 2h30m           默认增加2小时30分");
        System.out.println("  sudo java CentosTimeShift +2h30m          显式增加");
        System.out.println("  sudo java CentosTimeShift -1d             显式减少");
        System.out.println("  sudo java CentosTimeShift add 1w2d3h      增加1周2天3小时");
        System.out.println("  sudo java CentosTimeShift add 1h 30m      支持空格分隔偏移");
        System.out.println("  sudo java CentosTimeShift +1h 30m         支持空格分隔偏移");
        System.out.println("  sudo java CentosTimeShift set \"2025-01-01 00:00:00\"  设为绝对时间");
        System.out.println("  sudo java CentosTimeShift restore         恢复到修改前时间并恢复NTP（如适用）");
        System.out.println("  sudo java CentosTimeShift status          显示工具、NTP状态和已保存状态");
        System.out.println();
        System.out.println("支持的偏移单位（可叠加，顺序随意）：");
        System.out.println("  秒: s, sec, second, seconds, 秒");
        System.out.println("  分: m, min, minute, minutes, 分, 分钟");
        System.out.println("  时: h, hour, hours, 小时");
        System.out.println("  天: d, day, days, 天");
        System.out.println("  周: w, week, weeks, 周");
        System.out.println("  月: mo, mon, month, months, 月   （注意：m=分钟，mo=月份）");
        System.out.println();
        System.out.println("示例：1w2d3h 或 -2d 或 add 90m 或 2mo");
    }

    // ---- Core operations ----

    private static void status() throws Exception {
        Tool tool = detectTool();
        System.out.println("工具检测：");
        System.out.println("  timedatectl 可用: " + tool.hasTimedatectl);
        System.out.println("  date 可用       : " + tool.hasDate);
        System.out.println("  将使用           : " + (tool.preferred == ToolKind.TIMEDATECTL ? "timedatectl" :
                                          tool.preferred == ToolKind.DATE ? "date -s" : "无（无法修改）"));

        String now = ZonedDateTime.now(ZONE).format(DT);
        System.out.println("当前系统时间（本地时区）: " + now);

        if (tool.hasTimedatectl) {
            NtpState ns = getNtpState();
            System.out.println("NTP 启用: " + ns.enabled + ", 已同步: " + ns.synchronizedNow);
        } else {
            System.out.println("NTP 状态: timedatectl 不可用，无法查询。");
        }

        if (Files.exists(STATE_FILE)) {
            Properties p = loadProps(STATE_FILE);
            System.out.println("已保存的原始时间状态：");
            System.out.println("  originalTime   : " + p.getProperty("originalTime", "未知"));
            System.out.println("  savedAt        : " + p.getProperty("savedAt", "未知"));
            System.out.println("  ntpOriginally  : " + p.getProperty("ntpOriginally", "未知"));
            System.out.println("  toolWhenSaved  : " + p.getProperty("toolWhenSaved", "未知"));
        } else {
            System.out.println("未发现已保存的原始时间状态。");
        }
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

        Boolean ntpOriginally = null;
        if (tool.hasTimedatectl) {
            NtpState ns = getNtpState();
            ntpOriginally = ns.enabled;
            if (ns.enabled) {
                setNtp(false);
            }
        }

        String formatted = ldt.format(DT);
        applySystemTime(formatted, tool);

        tryHwclockSync();

        System.out.println("已将系统时间设为: " + formatted);

        if (ntpOriginally != null && ntpOriginally) {
            System.out.println("提示：恢复时将重新开启 NTP。若需立即开启：sudo timedatectl set-ntp true");
        }
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

        Boolean ntpOriginally = null;
        if (tool.hasTimedatectl) {
            NtpState ns = getNtpState();
            ntpOriginally = ns.enabled;
            if (ns.enabled) {
                setNtp(false);
            }
        }

        String formatted = target.format(DT);
        applySystemTime(formatted, tool);
        tryHwclockSync();

        System.out.println("当前时间: " + now.format(DT));
        System.out.println("目标时间: " + formatted);
        if (shift.negative) {
            System.out.println("已减少时间。");
        } else {
            System.out.println("已增加时间。");
        }
        if (ntpOriginally != null && ntpOriginally) {
            System.out.println("提示：恢复时将重新开启 NTP。若需立即开启：sudo timedatectl set-ntp true");
        }
    }

    private static void restore() throws Exception {
        ensureRoot();
        Tool tool = detectTool();

        if (!Files.exists(STATE_FILE)) {
            System.out.println("未发现保存的原始状态。将尝试仅重新开启 NTP（如可用）。");
            if (tool.hasTimedatectl) {
                setNtp(true);
                System.out.println("已尝试开启 NTP，同步可能需要数秒至数十秒。");
            } else {
                System.out.println("timedatectl 不可用，无法自动开启 NTP。");
            }
            return;
        }

        Properties p = loadProps(STATE_FILE);
        String originalTime = p.getProperty("originalTime", null);
        boolean ntpOriginally = Boolean.parseBoolean(p.getProperty("ntpOriginally", "false"));

        if (originalTime == null) {
            System.out.println("状态文件不完整。仅尝试开启 NTP。");
            if (tool.hasTimedatectl) setNtp(true);
            return;
        }

        if (tool.preferred == ToolKind.NONE) {
            throw new IllegalStateException("系统未检测到可用的时间设置工具（timedatectl 或 date）。");
        }

        applySystemTime(originalTime, tool);
        tryHwclockSync();
        System.out.println("已恢复系统时间为原始时间: " + originalTime);

        if (tool.hasTimedatectl && ntpOriginally) {
            setNtp(true);
            System.out.println("已恢复 NTP 启用状态。");
        }

        try {
            Files.deleteIfExists(STATE_FILE);
        } catch (IOException ignored) {}
    }

    // ---- Helpers: parsing, applying time, NTP, tool detection ----

    private static void ensureRoot() throws NeedsRootException {
        try {
            CommandResult r = run(new String[]{"id", "-u"});
            if (r.exitCode == 0 && r.stdout.trim().equals("0")) {
                return;
            }
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
        if (tool.hasTimedatectl) {
            ntpEnabled = getNtpState().enabled;
        }

        Properties p = new Properties();
        p.setProperty("originalTime", now);
        p.setProperty("savedAt", now);
        p.setProperty("toolWhenSaved", toolName);
        p.setProperty("ntpOriginally", Boolean.toString(ntpEnabled));

        try {
            if (!Files.exists(STATE_DIR)) {
                Files.createDirectories(STATE_DIR);
            }
            try (OutputStream out = Files.newOutputStream(STATE_FILE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                p.store(out, "CentosTimeShift saved state");
            }
            try {
                Set<PosixFilePermission> perms = EnumSet.of(
                        PosixFilePermission.OWNER_READ,
                        PosixFilePermission.OWNER_WRITE
                );
                Files.setPosixFilePermissions(STATE_FILE, perms);
            } catch (Exception ignored) {}
            System.out.println("已保存原始时间状态于: " + STATE_FILE);
        } catch (IOException e) {
            System.err.println("警告：无法保存原始时间状态（不影响时间调整）： " + e.getMessage());
        }
    }

    private static void applySystemTime(String formattedLocalTime, Tool tool) throws Exception {
        if (tool.preferred == ToolKind.TIMEDATECTL) {
            CommandResult r = run(new String[]{"timedatectl", "set-time", formattedLocalTime});
            if (r.exitCode != 0) {
                throw new RuntimeException("timedatectl 设置失败: " + r.stderr.trim());
            }
        } else if (tool.preferred == ToolKind.DATE) {
            CommandResult r = run(new String[]{"date", "-s", formattedLocalTime});
            if (r.exitCode != 0) {
                throw new RuntimeException("date -s 设置失败: " + r.stderr.trim());
            }
        } else {
            throw new IllegalStateException("无可用工具设置系统时间。");
        }
    }

    private static void tryHwclockSync() {
        try {
            CommandResult r = run(new String[]{"hwclock", "--systohc"});
            if (r.exitCode == 0) {
                System.out.println("已同步系统时间至硬件时钟（hwclock --systohc）。");
            }
        } catch (IOException ignored) {}
    }

    private static void setNtp(boolean enable) throws Exception {
        Tool tool = detectTool();
        if (!tool.hasTimedatectl) {
            System.out.println("timedatectl 不可用，无法" + (enable ? "开启" : "关闭") + " NTP。");
            return;
        }
        CommandResult r = run(new String[]{"timedatectl", "set-ntp", enable ? "true" : "false"});
        if (r.exitCode != 0) {
            System.out.println("警告：timedatectl set-ntp 失败：" + r.stderr.trim());
        } else {
            System.out.println((enable ? "已开启" : "已关闭") + " NTP。");
        }
    }

    private static NtpState getNtpState() {
        try {
            CommandResult r = run(new String[]{"timedatectl", "show", "-p", "NTP", "-p", "NTPSynchronized"});
            boolean enabled = false;
            boolean synced = false;
            for (String line : r.stdout.split("\n")) {
                line = line.trim();
                if (line.startsWith("NTP=")) {
                    enabled = "yes".equalsIgnoreCase(line.substring(4)) || "true".equalsIgnoreCase(line.substring(4));
                } else if (line.startsWith("NTPSynchronized=")) {
                    synced = "yes".equalsIgnoreCase(line.substring("NTPSynchronized=".length())) ||
                             "true".equalsIgnoreCase(line.substring("NTPSynchronized=".length()));
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
        ToolKind preferred = ToolKind.NONE;
        if (hasTimedatectl) preferred = ToolKind.TIMEDATECTL;
        else if (hasDate) preferred = ToolKind.DATE;
        return new Tool(hasTimedatectl, hasDate, preferred);
    }

    private static boolean hasCommand(String cmd, String argForCheck) {
        try {
            Process p = new ProcessBuilder(cmd, argForCheck).redirectErrorStream(true).start();
            p.waitFor();
            return true;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    private static Properties loadProps(Path path) throws IOException {
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(path)) {
            p.load(in);
        }
        return p;
    }

    // ---- Shift parsing and application ----

    private static class Shift {
        boolean negative;
        int months;
        int weeks;
        int days;
        int hours;
        int minutes;
        int seconds;
    }

    private static Shift parseShift(String s) {
        if (s == null || s.trim().isEmpty()) {
            throw new IllegalArgumentException("偏移字符串为空。示例：+2h30m 或 -1d 或 1w2d3h");
        }
        s = s.trim();

        Shift result = new Shift();
        if (s.startsWith("+")) {
            result.negative = false;
            s = s.substring(1);
        } else if (s.startsWith("-")) {
            result.negative = true;
            s = s.substring(1);
        }

        s = s.replaceAll("\\s+", "");

        Pattern token = Pattern.compile("(\\d+)(mo|mon|month|months|月|w|week|weeks|周|d|day|days|天|h|hour|hours|小时|m|min|minute|minutes|分|分钟|s|sec|second|seconds|秒)", Pattern.CASE_INSENSITIVE);
        Matcher m = token.matcher(s);
        int lastEnd = 0;
        int matchedCount = 0;
        while (m.find()) {
            if (m.start() != lastEnd) {
                throw new IllegalArgumentException("无法解析偏移片段: " + s.substring(lastEnd, m.start()));
            }
            String numStr = m.group(1);
            String unit = m.group(2).toLowerCase(Locale.ROOT);
            int val = Integer.parseInt(numStr);

            switch (unit) {
                case "mo":
                case "mon":
                case "month":
                case "months":
                case "月":
                    result.months += val;
                    break;
                case "w":
                case "week":
                case "weeks":
                case "周":
                    result.weeks += val;
                    break;
                case "d":
                case "day":
                case "days":
                case "天":
                    result.days += val;
                    break;
                case "h":
                case "hour":
                case "hours":
                case "小时":
                    result.hours += val;
                    break;
                case "m":
                case "min":
                case "minute":
                case "minutes":
                case "分":
                case "分钟":
                    result.minutes += val;
                    break;
                case "s":
                case "sec":
                case "second":
                case "seconds":
                case "秒":
                    result.seconds += val;
                    break;
                default:
                    throw new IllegalArgumentException("未知单位: " + unit);
            }

            lastEnd = m.end();
            matchedCount++;
        }
        if (matchedCount == 0 || lastEnd != s.length()) {
            throw new IllegalArgumentException("偏移格式不正确。示例：+1w2d3h 或 -90m 或 2mo");
        }
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

    // ---- Low-level command runner ----

    private static class CommandResult {
        int exitCode;
        String stdout;
        String stderr;
    }

    private static CommandResult run(String[] cmd) throws IOException {
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

    private static Thread pipe(InputStream in, OutputStream out) {
        Thread t = new Thread(() -> {
            byte[] buf = new byte[4096];
            int n;
            try {
                while ((n = in.read(buf)) != -1) {
                    out.write(buf, 0, n);
                }
            } catch (IOException ignored) {}
        });
        t.setDaemon(true);
        t.start();
        return t;
    }

    // ---- Types ----

    private enum ToolKind { TIMEDATECTL, DATE, NONE }

    private static class Tool {
        final boolean hasTimedatectl;
        final boolean hasDate;
        final ToolKind preferred;
        Tool(boolean t, boolean d, ToolKind p) {
            this.hasTimedatectl = t;
            this.hasDate = d;
            this.preferred = p;
        }
    }

    private static class NtpState {
        final boolean enabled;
        final boolean synchronizedNow;
        NtpState(boolean e, boolean s) {
            this.enabled = e;
            this.synchronizedNow = s;
        }
    }

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