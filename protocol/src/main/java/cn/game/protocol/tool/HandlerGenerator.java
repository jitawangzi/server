package cn.game.protocol.tool;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import cn.game.protocol.protobuf.PbProtocol;

/**    
 * 根据消息id，生成对应Handler类的代码包括导入、初始化、方法体等
 * 只是通过解析文本的方式来在指定位置插入代码
 * 2024年8月23日 下午6:37:18
 * @author SYQ
 */
public class HandlerGenerator {

	public static void main(String[] args) throws Exception {
		List<String> initFile = initFile("cn.game.games.net.game.module.develop.pet", "Pet", "0x19");

		List<String> requestMessages = new ArrayList<>();
		requestMessages.add("PetRefineRequest_19000007");
		requestMessages.add("PetCompositeRequest_19000001");

		updateFile("Pet", initFile, requestMessages);
	}

	private static List<String> initFile(String pkg, String module, String message) throws IOException {

		List<String> content = new ArrayList<>();
		content.add("package " + pkg + ";\n");
		content.add("\n");
		content.add("import org.springframework.stereotype.Component;\n");
		content.add("\n");
		content.add("import cn.game.core.net.client.NetClient;\n");
		content.add("import cn.game.core.net.socket.handler.BaseHandler;\n");
		content.add("import cn.game.games.cache.entity.Player;\n");
		content.add("import cn.game.games.net.game.manager.PlayerManager;\n");
		content.add("import cn.game.protocol.generated.enume.InitialUI;\n");
		content.add("import cn.game.protocol.manual.ErrorMsgEnum;\n");
		content.add("import cn.game.protocol.protobuf.PbProtocol;\n");
		content.add("\n");
		
		content.add("@Component\n");
		content.add("public class " + module + "Handler extends BaseHandler {\n");
		content.add("\n");
		content.add("\t@Override\n");
		content.add("\tprotected int getModule() {\n");
		content.add("\t\treturn " + message + ";\n");
		content.add("\t}\n");
		content.add("\n");
		content.add("\t@Override\n");
		content.add("\tprotected void inititialize() {\n");
//		content.add("\n");
		content.add("\t}\n");

		content.add("\n");
		content.add("}");

		Writer write = new FileWriter(new File("d:/handler.java"));
		for (String string : content) {
			write.write(string);
		}
		write.close();

		return content;
	}

	private static void updateFile(String module, List<String> contentList, List<String> requestMessages) throws IOException {
		for (String line : contentList) {
			line = line.trim();
			if (line.startsWith("putInvoker")) {
				int lastIndexOf = line.lastIndexOf("PbProtocol.");
				int indexOf = line.indexOf(",");
				String reqMessage = line.substring(lastIndexOf, indexOf);
				requestMessages.remove(reqMessage);
			}
		}
		if (requestMessages.isEmpty()) {
			return;
		}
		
		boolean inititializeStart = false;
		boolean inititializeEnd = false;
		// 需要新增代码
		for (int i = 0; i < contentList.size(); i++) {
			String line = contentList.get(i);
			line = line.trim();
			if (StringUtils.isEmpty(line)) {
				continue;
			}

			if (inititializeStart && line.indexOf("}") > -1 && !inititializeEnd) {
				inititializeEnd = true;
				continue;
			}
			if (line.indexOf("import cn.game.protocol.protobuf.PbProtocol") > -1) {
				// 导入
				for (String req : requestMessages) {
					contentList.add(i + 1, "import cn.game.protocol.protobuf." + module + "Msg." + req + ";\n");
					contentList.add(i + 2, "import cn.game.protocol.protobuf." + module + "Msg." + getRespMessage(req) + ";\n");
				}
			} else if (line.indexOf("protected void inititialize") > -1) {
				inititializeStart = true;
				// 初始化
				for (String req : requestMessages) {
					contentList.add(i + 1, "\t\tputInvoker(PbProtocol." + req + ", this::" + getReqMethod(req, module) + ");\n");
				}
			} else if (inititializeEnd) {
				// 新增方法
				for (String reqString : requestMessages) {
					List<String> methodBody = genMethodBody(reqString, module, null);
					for (int j = 0; j < methodBody.size(); j++) {
						contentList.add(i + j, methodBody.get(j));
					}
				}
				break;
			}
		}

		Writer write = new FileWriter(new File("d:/handler.java"));
		for (String string : contentList) {
			write.write(string);
		}
		write.close();
	}

	public static String getRespMessage(String reqMessage) {
		int msgId = PbProtocol.getInstance().getMsgId(reqMessage);
		return PbProtocol.getInstance().getMsgName(msgId + 1);
	}

	public static String getReqMethod(String reqMessage, String module) {
		int indexOf = reqMessage.indexOf("_");
		String method = reqMessage.substring(0, indexOf);
		method = method.replace(module, "").replace("Request", "");
		return Character.toLowerCase(method.charAt(0)) + method.substring(1);
	}

	public static List<String> genMethodBody(String reqMessage, String module,String function) {
		List<String> content = new ArrayList<>();
		content.add("\t" + "private void " + getReqMethod(reqMessage, module) + "(NetClient client, Object message) {\n");
		content.add("\t\t" + reqMessage + " req = (" + reqMessage + ") message;\n");
		content.add("\t\t" + getRespMessage(reqMessage) + ".Builder resp = " + getRespMessage(reqMessage) + ".newBuilder();\n");
		content.add("\t\t" + "Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());\n");
		if (function!=null) {
			content.add("\t\t" + "if (!player.isFuncOpen(" + function + ")) {\n");
			content.add("\t\t" + "\tclient.sendProtocol(resp.build(), ErrorMsgEnum.func_not_open.getId());\n");
			content.add("\t\t" + "\treturn;\n");
			content.add("\t\t" + "}\n");
		}
		content.add("\t\t" + "\n");
		content.add("\t\t" + "client.sendProtocol(resp.build());\n");
		content.add("\t" + "}\n");

		return content;
	}
}
