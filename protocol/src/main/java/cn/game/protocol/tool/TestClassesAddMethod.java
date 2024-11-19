package cn.game.protocol.tool;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;

/**    
 * 增加一个 getMessagePressure 方法到所有的类中
 * 2024年11月19日 11:36:12
 * @author SYQ
 */
public class TestClassesAddMethod {
	public static void main(String[] args) {
		String directoryPath = "C:\\work_all\\work\\server\\simulationclient\\src\\main\\java\\cn\\game\\simulation\\test\\gen"; // 替换为你的目录路径

		try {
			try (Stream<Path> paths = Files.walk(Paths.get(directoryPath))) {
				paths.filter(Files::isRegularFile)
						.filter(path -> path.toString().endsWith(".java"))
						.forEach(TestClassesAddMethod::processFile);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void processFile(Path path) {
		try {
			// 解析Java文件
			CompilationUnit cu = StaticJavaParser.parse(path.toFile());
			// 设置词法保持
			LexicalPreservingPrinter.setup(cu);
			// 获取类声明
			cu.findAll(ClassOrInterfaceDeclaration.class).forEach(classDecl -> {
				// 查找getMessage方法
				Optional<MethodDeclaration> getMessageMethodOpt = classDecl.getMethodsByName("getMessage").stream().findFirst();

				if (getMessageMethodOpt.isPresent()) {
					MethodDeclaration getMessageMethod = getMessageMethodOpt.get();

					// 如果getMessagePressure方法不存在，则添加
					if (classDecl.getMethodsByName("getMessagePressure").isEmpty()) {
						// 创建新方法
						// clone方式似乎不兼容 LexicalPreservingPrinter
						// LexicalPreservingPrinter是保持原始代码的格式，处理克隆节点的修改有些问题
//						MethodDeclaration newMethod = getMessageMethod.clone();
//						newMethod.setName("getMessagePressure");

						// 替代方案：
						// 1、创建新方法的字符串
//						String methodSource = getMessageMethod.toString().replace("getMessage", "getMessagePressure");
//						MethodDeclaration newMethod = StaticJavaParser.parseMethodDeclaration(methodSource);
						// 2 、直接创建新方法
						MethodDeclaration newMethod = new MethodDeclaration().setName("getMessagePressure")
								.setType(getMessageMethod.getType().clone())
								.setModifiers(getMessageMethod.getModifiers())
								.setBody(getMessageMethod.getBody().get().clone());
						// 复制参数
						getMessageMethod.getParameters().forEach(param -> newMethod.addParameter(param.clone()));
						getMessageMethod.getAnnotations().forEach(anno -> newMethod.addAnnotation(anno.clone()));

						List<MethodDeclaration> methods = classDecl.getMethods();
						int getMessageIndex = methods.indexOf(getMessageMethod);
						if (getMessageIndex != -1) {
							classDecl.getMembers().remove(newMethod); // 确保没有重复添加
							classDecl.getMembers().add(getMessageIndex + 1, newMethod);
						}
						// 保存修改后的文件，使用LexicalPreservingPrinter
						try (FileWriter writer = new FileWriter(path.toFile())) {
							writer.write(LexicalPreservingPrinter.print(cu));
						} catch (IOException e) {
							System.err.println("保存文件时出错: " + path);
							e.printStackTrace();
						}
					}
				}
			});
		} catch (Exception e) {
			System.err.println("处理文件时出错: " + path);
			e.printStackTrace();
		}
	}
}