package cn.game.util.ai;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ParserConfiguration.LanguageLevel;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.javadoc.Javadoc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AiContextExporter {

    private static final String ANNOTATION_NAME = "AiExport";
    
    // 全局输出流，默认为控制台
    private static PrintStream out = System.out;

    public static void main(String[] args) throws IOException {
        // 1. 获取 Workspace (项目根目录)
        // 逻辑：优先读取 Java 系统属性 (-Dworkspace=xxx)，如果为空则读取系统环境变量 (SET workspace=xxx)
        String workspace = System.getProperty("workspace", System.getenv("workspace"));

        if (workspace == null || workspace.trim().isEmpty()) {
            System.err.println("[ERROR] 'workspace' environment variable is not set!");
            System.err.println("Please set it via system env or java property -Dworkspace=...");
            System.exit(1);
        }

        // 2. 解析参数 (分离模块名和输出文件路径)
        List<String> modules = new ArrayList<>();
        String outputFilePath = null;

        for (int i = 0; i < args.length; i++) {
            if ("-o".equals(args[i])) {
                if (i + 1 < args.length) {
                    outputFilePath = args[i + 1];
                    i++; // 跳过下一个参数
                } else {
                    System.err.println("[WARN] Flag '-o' provided but no file path specified. Ignoring.");
                }
            } else {
                modules.add(args[i]);
            }
        }

        if (modules.isEmpty()) {
            System.err.println("Usage: java AiContextExporter <module1> <module2> ... [-o output_path]");
            return;
        }

        // 3. 设置输出流
        if (outputFilePath != null) {
            File outFile = new File(outputFilePath);
            // 确保父目录存在
            if (outFile.getParentFile() != null) {
                outFile.getParentFile().mkdirs();
            }
            out = new PrintStream(new FileOutputStream(outFile));
            System.out.println("// Output redirected to: " + outputFilePath);
        }

        // 打印头信息 (这也将写入文件)
        out.println("// ==========================================");
        out.println("// AI Context Export");
        out.println("// Workspace: " + workspace);
        out.println("// Modules: " + String.join(", ", modules));
        out.println("// ==========================================\n");

        // 4. 开始扫描
        for (String moduleName : modules) {
            Path moduleSrcPath = Paths.get(workspace, moduleName, "src", "main", "java");

            if (!Files.exists(moduleSrcPath)) {
                // 兼容逻辑：尝试直接在 workspace 下找 (如果 moduleName 本身包含路径)
                moduleSrcPath = Paths.get(workspace, moduleName);
                if (!Files.exists(moduleSrcPath)) {
                    System.err.println("[WARN] Path not found: " + moduleSrcPath);
                    continue;
                }
            }
            
            // 可以在控制台打印进度日志，而不写入文件（使用 System.err 或 System.out 根据情况）
            // 注意：因为 out 可能被重定向到了文件，所以进度信息建议用 System.err 打印到屏幕
            System.err.println(">>> Scanning: " + moduleName);
            
            scanDirectory(moduleSrcPath);
        }

        // 5. 资源清理
        if (out != System.out) {
            out.close();
            System.err.println(">>> Done. Context saved.");
        }
    }

    private static void scanDirectory(Path startPath) throws IOException {
        try (Stream<Path> walk = Files.walk(startPath)) {
            List<File> javaFiles = walk.filter(p -> p.toString().endsWith(".java"))
                    .map(Path::toFile)
                    .collect(Collectors.toList());

            for (File file : javaFiles) {
                processFile(file);
            }
        }
    }

    private static void processFile(File file) {
        try {
    		// 首先设置语言级别
    		StaticJavaParser.getConfiguration().setLanguageLevel(LanguageLevel.JAVA_17);
            CompilationUnit cu = StaticJavaParser.parse(file);
            cu.findAll(ClassOrInterfaceDeclaration.class).forEach(clazz -> {
                boolean isClassAnnotated = clazz.isAnnotationPresent(ANNOTATION_NAME);
                boolean hasMethodAnnotated = clazz.getMethods().stream()
                        .anyMatch(m -> m.isAnnotationPresent(ANNOTATION_NAME));

                if (!isClassAnnotated && !hasMethodAnnotated) {
                    return;
                }

                StringBuilder classOutput = new StringBuilder();
                classOutput.append(String.format("// --- Class: %s ---\n", clazz.getNameAsString()));
                
                if (clazz.getJavadoc().isPresent()) {
                    classOutput.append(formatJavadoc(clazz.getJavadoc().get())).append("\n");
                }
                classOutput.append(String.format("public class %s {\n", clazz.getNameAsString()));

                boolean hasContent = false;
                for (MethodDeclaration method : clazz.getMethods()) {
                    if (!method.isPublic()) continue;

                    boolean shouldExport = isClassAnnotated || method.isAnnotationPresent(ANNOTATION_NAME);
                    if (shouldExport) {
                        hasContent = true;
                        method.getJavadoc().ifPresent(doc -> classOutput.append(formatJavadoc(doc)).append("\n"));
                        String signature = method.getDeclarationAsString(true, true, true);
                        signature = signature.replace("@" + ANNOTATION_NAME, "").trim();
                        classOutput.append("    ").append(signature).append(";\n\n");
                    }
                }
                classOutput.append("}\n");

                if (hasContent) {
                    // 核心修改：使用全局的 out 变量输出
                    out.println(classOutput.toString());
                }
            });
        } catch (Exception e) {
        	System.err.println("[ERROR] Failed to process file: " + file.getAbsolutePath());
			e.printStackTrace();
        }
    }

    private static String formatJavadoc(Javadoc javadoc) {
        String doc = javadoc.toText();
        return "    /**\n" + 
               doc.lines().map(line -> "     * " + line).collect(Collectors.joining("\n")) + 
               "\n     */";
    }
}