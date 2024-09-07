package cn.game.protocol.tool;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
@Deprecated
public class HandlerGenerator {

	public static void main(String[] args) throws Exception {
		List<String> initFile = initFile("cn.game.games.net.game.module.develop.pet", "Pet", "0x19");

		List<String> requestMessages = new ArrayList<>();
		requestMessages.add("PetRefineRequest_19000007");
		requestMessages.add("PetCompositeRequest_19000001");

		updateFile("d:/handler.java", "Pet", initFile, requestMessages, null);
	}

	public static List<String> initFile(String pkg, String module, String message) throws IOException {

		List<String> content = new ArrayList<>();
		content.add("package " + pkg + ";");
		content.add("");
		content.add("import org.springframework.stereotype.Component;");
		content.add("");
		content.add("import cn.game.core.net.client.NetClient;");
		content.add("import cn.game.core.net.socket.handler.BaseHandler;");
		content.add("import cn.game.games.cache.entity.Player;");
		content.add("import cn.game.games.net.game.manager.PlayerManager;");
		content.add("import cn.game.protocol.generated.enume.InitialUI;");
		content.add("import cn.game.protocol.manual.ErrorMsgEnum;");
		content.add("import cn.game.protocol.protobuf.PbProtocol;");
		content.add("");
		
		content.add("@Component");
		content.add("public class " + module + "Handler extends BaseHandler {");
		content.add("");
		content.add("\t@Override");
		content.add("\tprotected int getModule() {");
		content.add("\t\treturn " + message + ";");
		content.add("\t}");
		content.add("");
		content.add("\t@Override");
		content.add("\tprotected void inititialize() {");
		content.add("\t}\n");

		content.add("");
		content.add("}");

		Writer write = new FileWriter(new File("d:/handler.java"));
		for (String string : content) {
			write.write(string);
		}
		write.close();

		return content;
	}

	public static void updateFile(String handlerPath, String module, List<String> contentList, List<String> requestMessages, String function)
			throws IOException {
		System.err.println("更新前文本行数： " + contentList.size());
		for (String line : contentList) {
			line = line.trim();
			if (line.startsWith("putInvoker")) {
				int lastIndexOf = line.lastIndexOf("PbProtocol.");
				int indexOf = line.indexOf(",");
				String reqMessage = line.substring(lastIndexOf + "PbProtocol.".length(), indexOf);
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
					contentList.add(i + 1, "import cn.game.protocol.protobuf." + module + "Msg." + req + ";");
					contentList.add(i + 2, "import cn.game.protocol.protobuf." + module + "Msg." + getRespMessage(req) + ";");
				}
			} else if (line.indexOf("protected void inititialize") > -1) {
				inititializeStart = true;
				// 初始化
				for (String req : requestMessages) {
					contentList.add(i + 1, "\t\tputInvoker(PbProtocol." + req + ", this::" + getReqMethod(req, module) + ");");
				}
			} else if (inititializeEnd) {
				// 新增方法
				for (String reqString : requestMessages) {
					List<String> methodBody = genMethodBody(reqString, module, function);
					for (int j = 0; j < methodBody.size(); j++) {
						contentList.add(i + j, methodBody.get(j));
					}
				}
				break;
			}
		}
		System.err.println("更新后文本行数： " + contentList.size());
//		Writer write = new FileWriter(new File(handlerPath));
//		for (String string : contentList) {
//			write.write(string);
//		}
//		write.close();
		Path filePath = Paths.get(handlerPath);
		// 创建目录（如果不存在）
		Files.createDirectories(filePath.getParent());
		Files.write(filePath, contentList);
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
		content.add("\t" + "private void " + getReqMethod(reqMessage, module) + "(NetClient client, Object message) {");
		content.add("\t\t" + reqMessage + " req = (" + reqMessage + ") message;");
		content.add("\t\t" + getRespMessage(reqMessage) + ".Builder resp = " + getRespMessage(reqMessage) + ".newBuilder();");
		content.add("\t\t" + "Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());");
		if (function!=null) {
			content.add("\t\t" + "if (!player.isFuncOpen(InitialUI." + function + ")) {");
			content.add("\t\t" + "\tclient.sendProtocol(resp.build(), ErrorMsgEnum.func_not_open.getId());");
			content.add("\t\t" + "\treturn;");
			content.add("\t\t" + "}");
		}
		content.add("");
		content.add("\t\t" + "client.sendProtocol(resp.build());");
		content.add("\t" + "}");
		return content;
	}
}
