package cn.game.protocol.tool;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import cn.game.protocol.tool.MessageObject.MessageField;
import cn.game.protocol.tool.obj.HandlerParam;
import cn.game.util.CSVUtil;
import cn.game.util.ExcelUtil;

/**
 * proto文件修改后执行
 * 2017年5月6日 下午6:58:34
 * @author SYQ
 */
public class PbProtocolGenerator {

	private static VelocityEngine velocityEngine = new VelocityEngine();
	private static Properties initialProp;
	private static Properties velocityProp;
	private static String workspace;
	private static String metafolder;
	/** 一般是开发中的，或者是其他游戏的proto */
	private static Set<String> notParseProtos = new HashSet<String>();
	private static Set<String> notGenRequestMessages = new HashSet<String>();
	private static String notParseRequestPrefix;

	public static void readProtos(String protoPath, String inputTemplate, String outputFile, String chareset) throws Exception {

		// 生成XXXHandler类的参数
		Map<String, HandlerParam> handlerMap = new HashMap<String, HandlerParam>();
		Multimap<String, String> classNameRequestMessageMap = ArrayListMultimap.create();
		
		List<String> outClass = new ArrayList<>();
		List<MessageObject> messages = new ArrayList<>();
		List<String> packages = new ArrayList<>();
		Set<String> prefixs = new HashSet<>();

		String curPackage = null;
		String curOutclass = null;
		File file = new File(protoPath);
		if (!file.isDirectory()) {
			throw new IllegalArgumentException("路径错误:" + protoPath);
		}
		List<String> clientProtoLines = new ArrayList<>();

		File[] listFiles = file.listFiles();
		for (File f : listFiles) {
			if (f.getName().lastIndexOf(".proto") < 0) {
				continue;
			}
			if (f.isDirectory()) {
				continue;
			}
			String out = f.getName().split("\\.")[0].trim();
			curOutclass = out;
			outClass.add(out);

			BufferedReader reader = new BufferedReader(new FileReader(f));

			List<String> lines = new ArrayList<>();
			int index = 0;

			String str = null;
			boolean notUesd = false;
			while ((str = reader.readLine()) != null) {
				String clientStr = str.trim();
				// 把所有的message，单独生成到一个文件里
				if (clientStr.length() > 0 && !clientStr.startsWith("syntax") && !clientStr.startsWith("option")
//						&& !clientStr.startsWith("//") 
						&& !clientStr.startsWith("package") && !clientStr.startsWith("import")) {
					clientProtoLines.add(clientStr);
				}
				if (str.trim().startsWith("//") && str.trim().toLowerCase().contains("@notuseend")) {
					notUesd = false;
				}
				if (str.trim().startsWith("//") && str.trim().toLowerCase().contains("@handlerpackage")) {
					handlerMap.computeIfAbsent(out, k -> new HandlerParam()).setHandlerPackage(str.split(" ")[1].trim());
				}
				if (str.trim().startsWith("//") && str.trim().toLowerCase().contains("@function")) {
					handlerMap.computeIfAbsent(out, k -> new HandlerParam()).setFunction(str.split(" ")[1].trim());
				}
				if (str.trim().startsWith("//") && str.trim().toLowerCase().contains("@messagemodule")) {
					handlerMap.computeIfAbsent(out, k -> new HandlerParam()).setMessageModule(str.split(" ")[1].trim());
				}

				lines.add(str);
				// System.out.println(str);
				if (str.trim().startsWith("//") && str.trim().toLowerCase().contains("@notusestart")) {
					notUesd = true;
//					continue;
				} else if (str.indexOf("java_package") > -1) {
					int begin = str.indexOf("\"");
					int end = str.lastIndexOf("\"");
					String p = str.substring(begin + 1, end).trim();
					curPackage = p;
					packages.add(p);
				} else if (str.indexOf("message") > -1) {
					String[] split = str.split("message");
					String msgName = split[1].replace('{', ' ').trim();
					String[] split2 = msgName.split("_");
					if (split2.length < 2) { // 无消息id的
						index++;
						continue;
					}

					String msgId = "0x" + split2[1];
					MessageObject message = new MessageObject();
					message.setId(msgId);
					message.setShortName(msgName);
					String prefix = curPackage + "." + curOutclass;
					message.setPrefix(prefix);
					prefixs.add(prefix);
					message.setLongName(prefix + "." + msgName);
					String string = lines.get(index - 1).trim();
					if (string != null) {
						if (string.startsWith("/")) {
							String comment = string.replace('/', ' ').replace('*', ' ').trim();
							message.setComment(comment);
						}
					}

					messages.add(message);
					if (message.isRequest()) {
//						if (message.isRequest() || message.isPush()) {
						classNameRequestMessageMap.put(out, message.getShortName());
					}
					if (notParseProtos.contains(out) || notUesd) {
						notGenRequestMessages.add(message.getShortName());
					}
				}
				index++;

			}
			if (notUesd) {
				throw new IllegalArgumentException(f.getName() + " 应该是设置了 @NotUseStart但是没有设置@NotUseEnd");
			}
			reader.close();
		}

		checkId(messages);

		generate(messages, outClass, packages, prefixs, inputTemplate, outputFile, chareset);

		// 生成客户端测试类
		for (MessageObject m : messages) {

			generateRequestTest(m, chareset);
		}

		// 生成前端用的json文件

		Collections.sort(messages);
		// 生成协议列表，压测使用。
		genMessageDescCSV(messages);
		// 生成服务器的Handler类
		updateHandler(handlerMap, classNameRequestMessageMap);

		// 注意把MessageObject 的值修改了
		for (MessageObject messageObject : messages) {

			String id = messageObject.getId();
//			System.err.println(id);
			id = id.replace("0x", "");
			Integer idInt = Integer.valueOf(id, 16);
			messageObject.setId(idInt.toString());
		}

		String jsPath = metafolder + initialProp.getProperty("client.js.dir");
		generateClient(messages, "protocol_js_id.vm", jsPath + File.separator + "ProtosMessageID.ts", chareset);
		generateClient(messages, "protocol_js_name.vm", jsPath + File.separator + "ProtosMessageName.ts", chareset);

		messages = readMessageObject(protoPath);
		// 这个项目暂时不用这个
//		generateClient(messages, "protos.d.ts.vm", jsPath + File.separator + "protos.d.ts", chareset);
//		generateClient(messages, "ProtosEnum.ts.vm", jsPath + File.separator + "ProtosEnum.ts", chareset);

		// 所有proto，生成到一个文件里给客户端使用
		mergeAllProto4Client(clientProtoLines, jsPath);

	}

	private static void mergeAllProto4Client(List<String> clientProtoLines, String jsPath) throws IOException {
		File clientAllProto = new File(jsPath + File.separator + "all.proto");
		BufferedWriter writer = new BufferedWriter(new FileWriter(clientAllProto));

		// option optimize_for = LITE_RUNTIME;
		writer.write("syntax = \"proto3\";" + "\n");
		writer.write("package Protos;" + "\n");
		writer.write("\n");

		for (String string : clientProtoLines) {

			writer.write(string + "\n");
		}
		writer.close();
	}

	private static void genMessageDesc(List<MessageObject> messages) throws FileNotFoundException {
		String filePath = System.getProperty("user.dir") + "/messages" + ".xlsx";
		List<List<Object>> messagesList = new ArrayList<>();
		List<Object> headList = new ArrayList<>();
		headList.add("序号");
		headList.add("模块");
		headList.add("协议");
		headList.add("协议号");
		headList.add("权重");
		headList.add("描述");
		messagesList.add(headList);

		int i = 1;
		for (MessageObject obj : messages) {
			if (isNotRequestMessage(obj) || obj.getShortName().startsWith("Test")) {
				continue;
			}
			List<Object> list = new ArrayList<>();
			list.add(i++);
			list.add("模块");
			list.add(obj.getShortName());
			list.add(obj.getId());
			list.add(1);
			list.add(obj.getComment());
			messagesList.add(list);
		}

		ExcelUtil.writeDataAutoWidth(filePath, "协议描述", messagesList);
	}

	private static void genMessageDescCSV(List<MessageObject> messages) throws FileNotFoundException {
		String[] headers = new String[] { "序号", "协议名", "协议号", "模块", "功能组", "组顺序", "权重", "描述" };
		String filePath = System.getProperty("user.dir") + "/../simulationclient/messages.csv";
		Set<String> protoNameSet = new HashSet<String>();
		List<List<String>> oldDataList = new ArrayList<>();;
		if (new File(filePath).exists()) {
			oldDataList = CSVUtil.read(filePath, headers);
			for (List<String> list : oldDataList) {
				protoNameSet.add(list.get(1));
			}
		}
		List<List<String>> dataList = new ArrayList<>();
		// 老数据保留
		dataList.addAll(oldDataList);

		int i = 1;
		if (!oldDataList.isEmpty()) {
			for (List<String> list : dataList) {
				int seq = Integer.parseInt(list.get(0));
				if (seq > i) {
					i = seq;
				}
			}
			i++;
		}
		for (MessageObject obj : messages) {
			if (isNotRequestMessage(obj) || obj.getShortName().startsWith("Test")) {
				continue;
			}
			List<String> list = new ArrayList<>();
			if (protoNameSet.contains(obj.getShortName())) {
				continue;
			}

			list.add(i++ + "");
			list.add(obj.getShortName());
			list.add(obj.getId());
			list.add("");
			list.add("");
			list.add("");
			list.add("");
			list.add(obj.getComment());

			dataList.add(list);
		}

		CSVUtil.write(dataList, filePath, headers);

	}

	/** 
	 * 检查消息id是否合法， 包括是否有重复的id，和请求id +1 = 返回id
	 * @param messages
	 * @throws IllegalAccessError
	 */
	private static void checkId(List<MessageObject> messages) throws IllegalAccessError {
		Set<String> idSet = new HashSet<String>();

		Set<Integer> requestIdSet = new HashSet<Integer>();
		Set<Integer> responseAndPushIdSet = new HashSet<Integer>();

		for (MessageObject message : messages) {
			if (!idSet.add(message.getId())) {
				throw new IllegalAccessError("消息id重复： " + message.getId());
			}
			int id = Integer.parseInt(message.getId().replace("0x", ""), 16);

			if (message.isRequest()) {
				requestIdSet.add(id);
			} else {
				responseAndPushIdSet.add(id);
			}
		}
		for (Integer id : requestIdSet) {
			if (!responseAndPushIdSet.contains(id + 1)) {
				throw new IllegalAccessError("请求消息id没有对应的返回id： " + Integer.toHexString(id));
			}
		}
	}

	/**
	 * 读取所有的message，和enum
	 * @param protoPath
	 * @return
	 * @throws Exception
	 */
	public static List<MessageObject> readMessageObject(String protoPath) throws Exception {
		List<MessageObject> list = new ArrayList<>();
		File file = new File(protoPath);
		if (!file.isDirectory()) {
			throw new IllegalArgumentException("路径错误:" + protoPath);
		}
		File[] listFiles = file.listFiles();
		for (File f : listFiles) {
			if (f.getName().lastIndexOf(".proto") < 0) {
				continue;
			}
			if (f.isDirectory()) {
				continue;
			}

			MessageObject messageObject = new MessageObject();
			BufferedReader reader = new BufferedReader(new FileReader(f));

			String str = null;
			boolean readMessageOrEnum = true;
			boolean readingMessage = true;
			while ((str = reader.readLine()) != null) {
				str = str.trim();
				if ("".equals(str)) {
					continue;
				}
				if (readMessageOrEnum) {
					if (!str.startsWith("message") && !str.startsWith("enum")) {
						continue;
					}
					str = str.replace("{", "");
					String[] split = str.split("\\W+");
					messageObject.setShortName(split[1]);
					if (str.startsWith("enum")) {
						messageObject.setEnum(true);
						readingMessage = false;
					} else {
						readingMessage = true;

					}
					readMessageOrEnum = false;
				} else {
					if (str.startsWith("/") || str.startsWith("*") || str.startsWith("{")) {
						continue;
					}
					if (str.startsWith("}")) {
						readMessageOrEnum = true;
						list.add(messageObject);
						messageObject = new MessageObject();
						continue;
					}
					String[] fields = str.split("=");
					MessageField messageField = new MessageField();
					String[] sb = fields[1].trim().split(";");
					messageField.setIndex(sb[0].trim());
					if (sb.length > 1) {
						String[] sb2 = sb[1].trim().split("//");
						if (sb2.length > 1) {
							messageField.setDesc(sb2[1]);
						}
					}
					String field = fields[0].trim();
					String[] ss = field.split("\\W+");
					messageField.setName(ss[ss.length - 1]);
					if (readingMessage) {
						messageField.setType(ss[ss.length - 2]);
						if (ss.length == 3 && ss[0].equalsIgnoreCase("repeated")) {
							messageField.setArray(true);
						}
					} else {
						messageField.setType("");
					}
					messageObject.addField(messageField);
				}
			}

			reader.close();
		}

		return list;

	}

	public static void generate(List<MessageObject> messages, List<String> outClass, List<String> packages, Set<String> prefixs, String inputTemplate,
			String outputFile, String chareset) throws Exception {

		VelocityContext context = new VelocityContext();
		context.put("messages", messages);
		context.put("outClass", outClass);
		context.put("packages", packages);
		context.put("prefixs", prefixs);

		Template template = null;
		try {
			template = velocityEngine.getTemplate(inputTemplate);
		} catch (ResourceNotFoundException rnfe) {
			rnfe.printStackTrace();
			System.out.println("error : cannot find template " + inputTemplate);
			System.exit(1);
		} catch (ParseErrorException pee) {
			pee.printStackTrace();
			System.out.println("Syntax error in template " + inputTemplate + ":" + pee);
			System.exit(1);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Syntax error in template " + inputTemplate + ":" + e);
			System.exit(1);
		}

		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), chareset));

		if (template != null) {
			template.merge(context, writer);
		}

		writer.flush();
		writer.close();
	}

	public static void generateClient(List<MessageObject> messages, String inputTemplate, String outputFile, String chareset) throws Exception {

		VelocityContext context = new VelocityContext();
		Template template = null;

		try {
			context.put("messages", messages);

			template = velocityEngine.getTemplate(inputTemplate);
		} catch (ResourceNotFoundException rnfe) {
			rnfe.printStackTrace();
			System.out.println("error : cannot find template " + inputTemplate);
			System.exit(1);
		} catch (ParseErrorException pee) {
			pee.printStackTrace();
			System.out.println("Syntax error in template " + inputTemplate + ":" + pee);
			System.exit(1);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Syntax error in template " + inputTemplate + ":" + e);
			System.exit(1);
		}

		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), chareset));

		if (template != null) {
			template.merge(context, writer);
		}

		writer.flush();
		writer.close();
	}

	public static void generateRequestTest(MessageObject message, String charset) throws Exception {
		String outPath = workspace + initialProp.getProperty("client.test.dir");

		String inputTemplate = "client_test.vm";

		if (isNotRequestMessage(message)) {
			return;
		}

		File file = new File(outPath + File.separator + message.getShortName() + "Test.java");
		if (file.exists()) {
			return;
		}

		VelocityContext context = new VelocityContext();
		context.put("m", message);

		Template template = null;
		try {
			template = velocityEngine.getTemplate(inputTemplate);
		} catch (ResourceNotFoundException rnfe) {
			rnfe.printStackTrace();
			System.out.println("error : cannot find template " + inputTemplate);
			System.exit(1);
		} catch (ParseErrorException pee) {
			pee.printStackTrace();
			System.out.println("Syntax error in template " + inputTemplate + ":" + pee);
			System.exit(1);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Syntax error in template " + inputTemplate + ":" + e);
			System.exit(1);
		}

		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), charset));

		if (template != null) {
			template.merge(context, writer);
		}

		writer.flush();
		writer.close();
	}

	private static void updateHandler(Map<String, HandlerParam> handlerMap, Multimap<String, String> classNameRequestMessageMap) throws Exception {
		Set<Entry<String, HandlerParam>> entrySet = handlerMap.entrySet();
		for (Entry<String, HandlerParam> entry : entrySet) {
			String k = entry.getKey();
			HandlerParam v = entry.getValue();
			String handlerPackage = v.getHandlerPackage();
			String function = v.getFunction();
			String messageModule = v.getMessageModule();
			String className = k;
			List<String> messages = (List<String>) classNameRequestMessageMap.get(className);
			String module = className.replace("Msg", "");
			String handlerPath = workspace + "/game/src/main/java/" + handlerPackage.replace(".", "/") + "/" + module + "Handler.java";
			File file = new File(handlerPath);
			if (!file.exists()) {
				ClassGenerator.createHandlerJavaFile(handlerPath, handlerPackage, module + "Handler", "0x" + messageModule);
			}
			ClassGenerator.updateHandlerJavaFile(handlerPath, module + "Handler", module, messages, function);
		}
	}

	public static void main(String[] args) throws Exception {

		// 可以加vm参数改变workspace 和 metafolder 路径。
		workspace = System.getProperty("workspace", System.getenv("workspace"));
		metafolder = System.getProperty("metafolder", System.getenv("metafolder"));
		if (workspace == null) {
			throw new IllegalArgumentException("需要设置 workspace");
		}
		if (metafolder == null) {
			throw new IllegalArgumentException("需要设置 metafolder");
		}
		initialProp = new Properties();
		InputStream inpurtStream = PbProtocolGenerator.class.getClassLoader().getResourceAsStream("proto_gen.properties");
		initialProp.load(inpurtStream);

		velocityProp = new Properties();
		inpurtStream = PbProtocolGenerator.class.getClassLoader().getResourceAsStream("velocity.properties");
//			inpurtStream.
		velocityProp.load(inpurtStream);
//			velocityEngine.init("./config/velocity.properties");
		velocityEngine.init(velocityProp);

		String inputTemplate = "protocol_pb_impl.vm";

		String protoPath = workspace + initialProp.getProperty("protos.dir");
		String javaSrc = workspace + initialProp.getProperty("java.src.dir");
		String output = workspace + initialProp.getProperty("PbProtocol.dir");

		String toJava = initialProp.getProperty("proto.to.java");
		boolean protoToJava = Boolean.parseBoolean(toJava);

		String charset = initialProp.getProperty("charset");

		String notParse = initialProp.getProperty("not.parse.protos");
		if (!StringUtils.isEmpty(notParse)) {
			String[] split = notParse.split(",");
			for (String string : split) {
				notParseProtos.add(string);
			}
		}
		notParseRequestPrefix = initialProp.getProperty("not.parse.request.prefix");

//		List<MessageObject> messages = readMessageObject(protoPath, inputTemplate, output + "/PbProtocol.java", charset);
//		
//		String jsPath = initialProp.getProperty("client.js.dir");
//
//		generateClient(messages, "protos.d.ts.vm", jsPath + File.separator + "protos.d.ts", "utf-8");
//		generateClient(messages, "ProtosEnum.ts.vm", jsPath + File.separator + "ProtosEnum.ts", "utf-8");

		if (protoToJava) {
			Proto2Java.main(new String[] { protoPath, javaSrc });
		}

		readProtos(protoPath, inputTemplate, output + "/PbProtocol.java", charset);

		System.out.println("PbProtocol.java gen complete !");



		// 手写枚举，生成excel，给客户端用。
		EnumToExcel.main(args);
	}

	public static boolean isNotRequestMessage(MessageObject message) {
		return !message.isRequest() || (notParseRequestPrefix != null && message.getShortName().startsWith(notParseRequestPrefix))
				|| notGenRequestMessages.contains(message.getShortName());
	}
}
