package cn.game.protocol.tool;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.github.javaparser.ParserConfiguration.LanguageLevel;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;

import cn.game.protocol.protobuf.PbProtocol;

/**    
 * 基于JavaParser的类文件生成、修改。 
 * 2024年8月26日 上午11:21:44
 * @author SYQ
 */
public class ClassGenerator {

	/** 
	 * 根据xxxMsg.proto,创建处理请求消息的 xxxHandler类
	 * @param handlerPath Handler文件路径
	 * @param pkg Handler所在包名  msg中定义 /@HandlerPackage cn.game.games.net.game.module.develop.pet
	 * @param className xxxHandler
	 * @param moduleCode  模块号， msg中定义 //@MessageModule 19
	 * @throws IOException
	 */
	public static void createHandlerJavaFile(String handlerPath, String pkg, String className, String moduleCode) throws IOException {
		// 首先设置语言级别
		StaticJavaParser.getConfiguration().setLanguageLevel(LanguageLevel.JAVA_17);
		CompilationUnit cu = new CompilationUnit();
		cu.setPackageDeclaration(pkg);

		cu.addImport("org.springframework.stereotype.Component");
		cu.addImport("cn.game.core.net.socket.handler.BaseHandler");

		ClassOrInterfaceDeclaration classDeclaration = cu.addClass(className, Modifier.Keyword.PUBLIC);
		classDeclaration.addExtendedType("BaseHandler");
		classDeclaration.addMarkerAnnotation("Component");
		// 添加getModule方法
		MethodDeclaration methodGetModule = classDeclaration.addMethod("getModule", Modifier.Keyword.PROTECTED);
		methodGetModule.addMarkerAnnotation("Override");
		methodGetModule.setType(com.github.javaparser.ast.type.PrimitiveType.intType());
		methodGetModule.setBody(StaticJavaParser.parseBlock(String.format("{ return %s; }", moduleCode)));
		// 添加inititialize方法
		MethodDeclaration methodInititialize = classDeclaration.addMethod("inititialize", Modifier.Keyword.PROTECTED);
		methodInititialize.addMarkerAnnotation("Override");
		methodInititialize.setType(new com.github.javaparser.ast.type.VoidType());

		// 创建目录（如果不存在）
		Path path = Paths.get(handlerPath);
		Files.createDirectories(path.getParent());
		// 将新创建的类写入文件
		Files.write(path, cu.toString().getBytes());
	}

	/** 
	 * 在处理请求消息的 xxxHandler类存在时，增加请求消息的处理代码
	 * @param handlerPath
	 * @param className
	 * @param module
	 * @param requestMessages xxxMsg.proto中定义的所有请求消息名
	 * @param function  功能开启的枚举名，msg中定义  //@Function SoulPets
	 * @throws Exception
	 */
	public static void updateHandlerJavaFile(String handlerPath, String className, String module, List<String> requestMessages,
			String function) throws Exception {
		// 首先设置语言级别
		StaticJavaParser.getConfiguration().setLanguageLevel(LanguageLevel.JAVA_17);
		Path filePath = Paths.get(handlerPath);
		// 在解析代码时保存原始格式信息
		CompilationUnit cu = StaticJavaParser.parse(filePath);
		LexicalPreservingPrinter.setup(cu);

		ClassOrInterfaceDeclaration classDeclaration = cu.getClassByName(className).orElseThrow(() -> new RuntimeException("Class not found in file"));

		// 在inititialize方法中，根据putInvoker，找到存在的请求消息名并排除。
		MethodDeclaration inititializeMethod = classDeclaration.getMethodsByName("inititialize").get(0);
		BlockStmt blockStmt = inititializeMethod.getBody().get();
		List<Node> childNodes = blockStmt.getChildNodes();
		for (Node node : childNodes) {
			String nodeString = node.toString();
			if (nodeString.contains("putInvoker")) {
				int lastIndexOf = nodeString.lastIndexOf("PbProtocol.");
				int indexOf = nodeString.indexOf(",");
				String reqMessage = nodeString.substring(lastIndexOf + "PbProtocol.".length(), indexOf);
				requestMessages.remove(reqMessage);
			}
		}
		if (requestMessages.isEmpty()) {
			return;
		}

		boolean hasMap = false;
		boolean hasList = false;
		for (String reqMessage : requestMessages) {
			String respMessage = getRespMessage(reqMessage);
			checkImport("cn.game.protocol.protobuf." + module + "Msg", reqMessage, cu);
			checkImport("cn.game.protocol.protobuf." + module + "Msg", respMessage, cu);
			// 增加putInvoker
			blockStmt.addStatement(String.format("putInvoker(PbProtocol.%s, this::%s);", reqMessage, getReqMethod(reqMessage, module)));

			// 新增处理消息的方法
			MethodDeclaration messageMethod = classDeclaration.addMethod(getReqMethod(reqMessage, module), com.github.javaparser.ast.Modifier.Keyword.PRIVATE);
			messageMethod.setType(new com.github.javaparser.ast.type.VoidType());
			messageMethod.addParameter("NetClient", "client");
			messageMethod.addParameter("Object", "message");
			LambdaExpr lambda = new LambdaExpr();
			BlockStmt blockStmtMessage = new BlockStmt(); 
			blockStmtMessage.addStatement(reqMessage + " req = (" + reqMessage + ") message;");

			Class<?> respMessageClass = Class.forName("cn.game.protocol.protobuf." + module + "Msg$" + respMessage);
			Class<?> reqMessageClass = Class.forName("cn.game.protocol.protobuf." + module + "Msg$" + reqMessage);
			Message reqMessageInstance = (Message) reqMessageClass.getDeclaredMethod("getDefaultInstance").invoke(null);
			Message respMessageInstance = (Message) respMessageClass.getDeclaredMethod("getDefaultInstance").invoke(null);
			Descriptors.Descriptor reqDescriptor = reqMessageInstance.getDescriptorForType();
			Descriptors.Descriptor respDescriptor = respMessageInstance.getDescriptorForType();
			// 生成请求消息的 数据 get方法。
			if (!reqDescriptor.getFields().isEmpty()) {
				for (FieldDescriptor field : reqDescriptor.getFields()) {
					String fieldName = field.getName();
					if (field.isMapField()) {
						hasMap = true;
					} else if (field.isRepeated()) {
						hasList = true;
					}
					String fieldGetCode;
					if (field.isRepeated() || field.isMapField()) {
						fieldGetCode = generateRepeatedOrMapFieldCode(fieldName, field);
					} else {
						fieldGetCode = generateSingleFieldCode(fieldName, field);
					}
					blockStmtMessage.addStatement(fieldGetCode);
				}
			}
			blockStmtMessage.addStatement(respMessage + " defaultInstance = " + respMessage + ".getDefaultInstance();");
			String respInsatnce;
			if (!respDescriptor.getFields().isEmpty()) {
				respInsatnce = "resp.build()";
			} else {
				respInsatnce = "defaultInstance";
			}
			blockStmtMessage.addStatement("Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());");
//			if (function != null) {
//				blockStmt2
//						.addStatement("if (!player.isFuncOpen(InitialUI." + function + ")) {")
//						.addStatement("client.sendProtocol(resp.build(), ErrorMsgEnum.func_not_open.getId());")
//						.addStatement("return;")
//						.addStatement("}");
//			}
			if (function != null) {
				// 生成功能开启检查代码
				// 创建 if 条件
				Expression condition = new MethodCallExpr(new NameExpr("player"), "isFuncOpen",
						new NodeList<>(new FieldAccessExpr(new NameExpr("InitialUI"), function)));

				// 创建 if 语句体
				BlockStmt ifBody = new BlockStmt()
						.addStatement(new MethodCallExpr(new NameExpr("client"), "sendProtocol",
								new NodeList<>(new NameExpr("defaultInstance"),
										new MethodCallExpr(new FieldAccessExpr(new NameExpr("ErrorMsgEnum"), "func_not_open"), "getId"))))
						.addStatement(new ReturnStmt());

				// 创建完整的 if 语句
				IfStmt ifStmt = new IfStmt(new UnaryExpr(condition, UnaryExpr.Operator.LOGICAL_COMPLEMENT), ifBody, null);
				// 将 if 语句添加到主语句块
				blockStmtMessage.addStatement(ifStmt);
			}
			
			// 逻辑代码。。。

			if (!respDescriptor.getFields().isEmpty()) {
				blockStmtMessage.addStatement(respMessage + ".Builder resp = " + respMessage + ".newBuilder();\n");
			}
//			blockStmtMessage.addStatement(new EmptyStmt());
			blockStmtMessage.addStatement(String.format("client.sendProtocol(%s);", respInsatnce));
			lambda.setBody(blockStmtMessage);
			messageMethod.setBody(lambda.getBody().asBlockStmt());
			
			// 导入方法需要的类
			checkImport("cn.game.core.net.client.NetClient", cu);
			checkImport("cn.game.games.cache.entity.Player", cu);
			checkImport("cn.game.games.net.game.manager.PlayerManager", cu);
			checkImport("cn.game.protocol.generated.enume.InitialUI", cu);
			checkImport("cn.game.protocol.manual.ErrorMsgEnum", cu);
			checkImport("cn.game.protocol.protobuf.PbProtocol", cu);
			if (hasList) {
				checkImport("java.util.List", cu);
			}
			if (hasMap) {
				checkImport("java.util.Map", cu);
			}
		}

		// 使用 LexicalPreservingPrinter 输出修改后的代码
		String modifiedCode = LexicalPreservingPrinter.print(cu);

		// 将更新后的类写回文件
		Files.write(filePath, cu.toString().getBytes());
	}

	private static void checkImport(String importPackageOrClass, String importClass, CompilationUnit cu) {
		boolean isImported = cu
				.getImports()
				.stream()
				.anyMatch(importDecl -> importDecl.getNameAsString().equals(importPackageOrClass + "." + importClass)
						|| importDecl.getNameAsString().equals(importPackageOrClass + ".*"));

		if (!isImported) {
			// 如果没有导入，添加导入
			cu.addImport(importPackageOrClass + "." + importClass);
		}
	}

	private static void checkImport(String packageAndClass, CompilationUnit cu) {
		int lastIndexOf = packageAndClass.lastIndexOf(".");
		String packageName = packageAndClass.substring(0, lastIndexOf);
		String className = packageAndClass.substring(lastIndexOf + 1);
		checkImport(packageName, className, cu);
	}

	/** 
	 * 根据请求消息名，获取响应消息名
	 * @param reqMessage
	 * @return
	 */
	public static String getRespMessage(String reqMessage) {
		int msgId = PbProtocol.getInstance().getMsgId(reqMessage);
		return PbProtocol.getInstance().getMsgName(msgId + 1);
	}

	/** 
	 * 根据请求消息名，生成处理消息的方法名
	 * @param reqMessage
	 * @param module
	 * @return
	 */
	public static String getReqMethod(String reqMessage, String module) {
		int indexOf = reqMessage.indexOf("_");
		String method = reqMessage.substring(0, indexOf);
		method = method.replace(module, "").replace("Request", "");
		return Character.toLowerCase(method.charAt(0)) + method.substring(1);
	}

	private static String generateRepeatedOrMapFieldCode(String fieldName, Descriptors.FieldDescriptor fieldDescriptor) {
		Descriptors.FieldDescriptor.Type fieldType = fieldDescriptor.getType();
		String capitalizedFieldName = capitalize(fieldName);

		if (fieldDescriptor.isMapField()) {
			Descriptors.FieldDescriptor keyDescriptor = fieldDescriptor.getMessageType().findFieldByName("key");
			Descriptors.FieldDescriptor valueDescriptor = fieldDescriptor.getMessageType().findFieldByName("value");
			String keyType = getJavaType(keyDescriptor);
			String valueType = getJavaType(valueDescriptor);

			return "Map<" + keyType + ", " + valueType + "> " + fieldName + "Map = req.get" + capitalizedFieldName + "Map();";
		} else if (fieldDescriptor.isRepeated()) {
			switch (fieldType) {
			case INT32:
			case UINT32:
			case SINT32:
			case FIXED32:
			case SFIXED32:
				return "List<Integer> " + fieldName + "List = req.get" + capitalizedFieldName + "List();";
			case INT64:
			case UINT64:
			case SINT64:
			case FIXED64:
			case SFIXED64:
				return "List<Long> " + fieldName + "List = req.get" + capitalizedFieldName + "List();";
			case FLOAT:
				return "List<Float> " + fieldName + "List = req.get" + capitalizedFieldName + "List();";
			case DOUBLE:
				return "List<Double> " + fieldName + "List = req.get" + capitalizedFieldName + "List();";
			case BOOL:
				return "List<Boolean> " + fieldName + "List = req.get" + capitalizedFieldName + "List();";
			case STRING:
				return "List<String> " + fieldName + "List = req.get" + capitalizedFieldName + "List();";
			case BYTES:
				return "List<ByteString> " + fieldName + "List = req.get" + capitalizedFieldName + "List();";
			case ENUM:
				String enumType = fieldDescriptor.getEnumType().getName();
				return "List<" + enumType + "> " + fieldName + "List = req.get" + capitalizedFieldName + "List();";
			case MESSAGE:
				String messageType = fieldDescriptor.getMessageType().getName();
				return "List<" + messageType + "> " + fieldName + "List = req.get" + capitalizedFieldName + "List();";
			default:
				throw new IllegalArgumentException("Unsupported repeated field type: " + fieldType);
			}
		} else {
			throw new IllegalArgumentException("Field is neither repeated nor a map type.");
		}
	}

	private static String getJavaType(Descriptors.FieldDescriptor descriptor) {
		switch (descriptor.getType()) {
		case INT32:
		case UINT32:
		case SINT32:
		case FIXED32:
		case SFIXED32:
			return "Integer";
		case INT64:
		case UINT64:
		case SINT64:
		case FIXED64:
		case SFIXED64:
			return "Long";
		case FLOAT:
			return "Float";
		case DOUBLE:
			return "Double";
		case BOOL:
			return "Boolean";
		case STRING:
			return "String";
		case BYTES:
			return "ByteString";
		case ENUM:
			return descriptor.getEnumType().getName();
		case MESSAGE:
			return descriptor.getMessageType().getName();
		default:
			throw new IllegalArgumentException("Unsupported map field type: " + descriptor.getType());
		}
	}

	private static String generateSingleFieldCode(String fieldName, Descriptors.FieldDescriptor fieldDescriptor) {
		switch (fieldDescriptor.getType()) {
		case INT32:
		case UINT32:
		case SINT32:
		case FIXED32:
		case SFIXED32:
			return "int " + fieldName + " = req.get" + capitalize(fieldName) + "();";
		case INT64:
		case UINT64:
		case SINT64:
		case FIXED64:
		case SFIXED64:
			return "long " + fieldName + " = req.get" + capitalize(fieldName) + "();";
		case FLOAT:
			return "float " + fieldName + " = req.get" + capitalize(fieldName) + "();";
		case DOUBLE:
			return "double " + fieldName + " = req.get" + capitalize(fieldName) + "();";
		case BOOL:
			return "boolean " + fieldName + " = req.get" + capitalize(fieldName) + "();";
		case STRING:
			return "String " + fieldName + " = req.get" + capitalize(fieldName) + "();";
		case BYTES:
			return "ByteString " + fieldName + " = req.get" + capitalize(fieldName) + "();";
		case ENUM:
			return fieldDescriptor.getEnumType().getName() + " " + fieldName + " = req.get" + capitalize(fieldName) + "();";
		case MESSAGE:
			return fieldDescriptor.getMessageType().getName() + " " + fieldName + " = req.get" + capitalize(fieldName) + "();";
		default:
			return "// Unsupported field type: " + fieldDescriptor.getType().name();
		}
	}

	private static String capitalize(String name) {
		if (name == null || name.isEmpty()) {
			return name;
		}
		return Character.toUpperCase(name.charAt(0)) + name.substring(1);
	}
}