package cn.game.simulation.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import cn.game.util.CSVUtil;
import cn.game.util.Rnd;

public class CSVMessagesReader {

    public static List<CSVMessage> messages = new ArrayList<>();
    /** 组 -> 按顺序排列的消息列表 */
    public static Map<Integer, List<CSVMessage>> groupMessageMap = new java.util.HashMap<>();
    /** 组 -> 组权重（由每条消息的 weight 聚合而来） */
    public static Map<Integer, Integer> groupWeights = new java.util.HashMap<>();

    public static void main(String[] args) {
        String filePath = System.getProperty("user.dir") + "/messages" + ".csv";
        read(filePath);
    }

    /**
     * @param sendingGroup 当前绑定的组（>0 表示已在某组内轮转；<=0 表示需抽组）
     * @param sendingGroupIndex 当前组内正在尝试的索引
     * @return 命中的消息；若未命中返回 null；调用方据此决定是否发送以及如何推进 index
     */
    public static CSVMessage randomGroupMessage(int sendingGroup, int sendingGroupIndex) {
        List<CSVMessage> list;

        // 若已有绑定组，则仅在该组内尝试当前 index，不跨组
        if (sendingGroup > 0) {
            list = groupMessageMap.get(sendingGroup);
            if (list == null || list.isEmpty()) {
                // 当前组无消息，清空绑定，走抽组
            } else {
                return nextMessage(list, sendingGroupIndex);
            }
        }

        // 没有绑定组 或 绑定组无消息：按“组权重”抽一个组
        int group = randomGroupByWeight();
        list = groupMessageMap.get(group);
        if (list == null || list.isEmpty()) {
            return null;
        }
        // 新组从 index=0 开始尝试
        return nextMessage(list, 0);
    }
    private static CSVMessage nextMessage(List<CSVMessage> list, int index) {
        if (list == null || list.isEmpty()) return null;
        if (index < 0 || index >= list.size()) return null;

        CSVMessage csvMessage = list.get(index);
        // 概率语义：0 或空（读入时已处理为 0）= 必发；>0 则按百分比命中
        if (csvMessage.probability == 0 || Rnd.hitPercentage(csvMessage.probability)) {
            return csvMessage;
        }
//        if (csvMessage.probability < 0 ) {
//        	return null; 
//        }
        // 概率百分比没有命中，找下一个消息
        return nextMessage(list, index + 1);
    }

    /**
     * 单纯按权重随机消息（保持原有工具方法）
     */
    public static CSVMessage randomMessage() {
        CSVMessage randomMessage = Rnd.randomElement(messages, r -> r.weight);
        return randomMessage;
    }

    public static List<CSVMessage> getGroupMessages(int group) {
        return groupMessageMap.get(group);
    }

    public static void read(String filePath) {
        String[] headers = new String[] { "序号", "协议名", "协议号", "模块", "功能组", "组顺序", "权重", "描述" };

        List<List<String>> list = CSVUtil.read(filePath, headers);
        for (List<String> csvRecord : list) {

            String seq = csvRecord.get(0);
            if (StringUtils.isEmpty(seq)) {
                continue;
            }
            String protocol = csvRecord.get(1);
            String protocolNumber = csvRecord.get(2);

            CSVMessage message = new CSVMessage();
            message.msgName = protocol;
            message.msgId = Integer.parseInt(protocolNumber.substring(2), 16);
            message.group = StringUtils.isEmpty(csvRecord.get(4)) ? 0 : Integer.parseInt(csvRecord.get(4));
            message.order = StringUtils.isEmpty(csvRecord.get(5)) ? Integer.MAX_VALUE : Integer.parseInt(csvRecord.get(5));
            // 这里的 weight 作为“组权重来源”，将被聚合为组的总权重
            message.weight = StringUtils.isEmpty(csvRecord.get(6)) ? 0 : Integer.parseInt(csvRecord.get(6));
            // 空或0 = 必发；>0 = 按百分比命中
            message.probability = 0; // 默认必发
            if (csvRecord.size() > 7) {
                String probStr = csvRecord.get(7);
                if (!StringUtils.isEmpty(probStr)) {
                    try {
                        message.probability = Integer.parseInt(probStr);
                    } catch (NumberFormatException ignore) {
                        // 保持为 0（必发）
                    }
                }
            }

            messages.add(message);
            groupMessageMap.computeIfAbsent(message.group, k -> new ArrayList<>()).add(message);

            // 聚合组权重：把每条消息的 weight 累加到其所在组
            if (message.weight > 0) {
                groupWeights.merge(message.group, message.weight, Integer::sum);
            } else {
                // 确保组存在于权重表，即便当前消息 weight=0
                groupWeights.putIfAbsent(message.group, groupWeights.getOrDefault(message.group, 0));
            }
        }

        // 组内按 order 排序（缺省 Integer.MAX_VALUE 的排在最后）
        groupMessageMap.forEach((k, v) -> v.sort(new Comparator<CSVMessage>() {
            @Override
            public int compare(CSVMessage o1, CSVMessage o2) {
                return Integer.compare(o1.order, o2.order);
            }
        }));
    }

    /**
     * 按“组权重”抽一个组：
     * - 如果有组的总权重>0，则按权重加权随机
     * - 如果所有组权重总和=0，则在现有的有消息的组中均匀随机
     */
    private static int randomGroupByWeight() {
        if (groupMessageMap == null || groupMessageMap.isEmpty()) {
            return 0;
        }

        long total = 0;
        for (Map.Entry<Integer, Integer> e : groupWeights.entrySet()) {
            int g = e.getKey();
            int gw = e.getValue() == null ? 0 : e.getValue();
            // 仅统计确实有消息的组
            if (gw > 0 && groupMessageMap.containsKey(g) && !groupMessageMap.get(g).isEmpty()) {
                total += gw;
            }
        }

        if (total <= 0) {
            // 所有组权重都为 0：均匀随机一个“有消息”的组
            List<Integer> groups = new ArrayList<>();
            for (Map.Entry<Integer, List<CSVMessage>> e : groupMessageMap.entrySet()) {
                if (e.getValue() != null && !e.getValue().isEmpty()) {
                    groups.add(e.getKey());
                }
            }
            if (groups.isEmpty()) return 0;
            return groups.get(Rnd.nextInt(groups.size()));
        }

        int r = Rnd.nextInt((int) total);
        int acc = 0;
        for (Map.Entry<Integer, Integer> e : groupWeights.entrySet()) {
            int g = e.getKey();
            int gw = e.getValue() == null ? 0 : e.getValue();
            if (gw <= 0) continue;
            List<CSVMessage> lst = groupMessageMap.get(g);
            if (lst == null || lst.isEmpty()) continue;

            acc += gw;
            if (r < acc) {
                return g;
            }
        }

        // 理论不会到达；兜底返回任一有消息的组
        for (Map.Entry<Integer, List<CSVMessage>> e : groupMessageMap.entrySet()) {
            if (e.getValue() != null && !e.getValue().isEmpty()) {
                return e.getKey();
            }
        }
        return 0;
    }

    public static class CSVMessage {
        public int msgId;
        public String msgName;
        public int group;
        public int order;
        public int weight;
        /** 单个协议发送概率，百分数；0 或空=必发 */
        public int probability;

        @Override
        public String toString() {
            return "CSVMessage [msgId=" + msgId + ", msgName=" + msgName + ", group=" + group + ", order=" + order
                    + ", weight=" + weight + ", probability=" + probability + "]";
        }
    }
}