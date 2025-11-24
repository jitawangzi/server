package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.protocol.protobuf.ChatMsg.ChatType;
import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;
import cn.game.util.Rnd;

@Component
public class ChatRequest_31000001Test extends ServerTest {
	static String[] poems = new String[] { "床前明月光，疑是地上霜。", "举头望明月，低头思故乡。", "春眠不觉晓，处处闻啼鸟。", "夜来风雨声，花落知多少。", "白日依山尽，黄河入海流。", "欲穷千里目，更上一层楼。",
			"孤帆远影碧空尽，唯见长江天际流。", "两个黄鹂鸣翠柳，一行白鹭上青天。", "忽如一夜春风来，千树万树梨花开。", "无边落木萧萧下，不尽长江滚滚来。", "月落乌啼霜满天，江枫渔火对愁眠。", "海内存知己，天涯若比邻。",
			"劝君更尽一杯酒，西出阳关无故人。", "醉卧沙场君莫笑，古来征战几人回？", "会当凌绝顶，一览众山小。", "东风不与周郎便，铜雀春深锁二乔。", "人生自古谁无死？留取丹心照汗青。", "生当作人杰，死亦为鬼雄。", "千山鸟飞绝，万径人踪灭。",
			"大漠孤烟直，长河落日圆。", "落霞与孤鹜齐飞，秋水共长天一色。", "采菊东篱下，悠然见南山。", "明月松间照，清泉石上流。", "山重水复疑无路，柳暗花明又一村。", "黑云压城城欲摧，甲光向日金鳞开。", "不畏浮云遮望眼，自缘身在最高层。",
			"欲把西湖比西子，淡妆浓抹总相宜。", "会挽雕弓如满月，西北望，射天狼。", "但愿人长久，千里共婵娟。", "身无彩凤双飞翼，心有灵犀一点通。", "衣带渐宽终不悔，为伊消得人憔悴。", "十年生死两茫茫，不思量，自难忘。",
			"莫愁前路无知己，天下谁人不识君。", "长风破浪会有时，直挂云帆济沧海。", "会稽山阴寻戴处，水面初平云脚低。", "故人西辞黄鹤楼，烟花三月下扬州。", "春江潮水连海平，海上明月共潮生。", "桃花潭水深千尺，不及汪伦送我情。",
			"夜阑卧听风吹雨，铁马冰河入梦来。", "竹外桃花三两枝，春江水暖鸭先知。", "接天莲叶无穷碧，映日荷花别样红。", "日照香炉生紫烟，遥看瀑布挂前川。", "天生我材必有用，千金散尽还复来。", "大江东去，浪淘尽，千古风流人物。",
			"无人问津舟自横，古渡平沙秋草生。", "溪云初起日沉阁，山雨欲来风满楼。", "蒹葭苍苍，白露为霜。", "燕山雪花大如席，片片吹落轩辕台。", "昨夜西风凋碧树，独上高楼，望尽天涯路。" };

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.ChatMsg.ChatRequest_31000001.Builder builder = cn.game.protocol.protobuf.ChatMsg.ChatRequest_31000001
				.newBuilder();

		builder.setChatType(ChatType.WORLD_CHAT);
//		builder.setContent("习近平");
		builder.setContent("你好");
//		builder.setTargetPlayerId(251220055 + "");

		return builder.build();
	}

	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.ChatMsg.ChatRequest_31000001.Builder builder = cn.game.protocol.protobuf.ChatMsg.ChatRequest_31000001
				.newBuilder();

		builder.setChatType(ChatType.WORLD_CHAT);
		String serverName = client.getPlayerAllInfo().getPlayer().getServerName();
		builder.setContent("来自 " + serverName + " 的消息： " + poems[Rnd.nextInt(poems.length)]);
//		builder.setTargetPlayerId(251220055 + "");

		return builder.build();
	}

	public static void main(String args[]) throws Exception {
		ChatRequest_31000001Test instance = new ChatRequest_31000001Test();
		instance.start();
	}

}