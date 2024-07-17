package cn.game.protocol.tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * 将protobuf的.proto文件转成java代码(可以通过插件代替)
 * 2017年4月24日 下午6:00:51
 * @author SYQ
 */
public class Proto2Java {

	public static void gen(String protoPath, String outputPath) {

		if (protoPath == null || outputPath == null) {
			System.err.println("需要输入2个参数，proto文件所在目录，和java文件输出目录");
			return;
		}
		String protoDirectory = protoPath;
		String outDirectory = outputPath;
		Runtime runtime = Runtime.getRuntime();
		String cmd = null;
		File file = new File(protoDirectory);
		if (!file.isDirectory()) {
			System.err.println("proto 文件所在目录路径错误,不是目录，或者目录不存在 :" + protoDirectory);
			System.exit(-1);
		}

		File[] listFiles = file.listFiles();
		for (File f : listFiles) {
			if (f.isDirectory()) {
				continue;
			}
			if (f.getName().lastIndexOf(".proto") < 0) {
				continue;
			}
			cmd = "protoc -I " + protoDirectory + " --java_out " + outDirectory + " " + f.getPath()
					+ " --experimental_allow_proto3_optional";
			System.out.println(cmd);
			try {
				Process exec = runtime.exec(cmd);
				InputStream errorStream = exec.getErrorStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream));
				String str = null;
				while ((str = reader.readLine()) != null) {
					System.err.println(str);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		System.out.println("proto文件解析完毕");

	}

	public static void main(String[] args) {
		if (args == null || args.length < 2) {
			System.err.println("需要输入2个参数，proto文件所在目录，和java文件输出目录");
			return;
		}
		gen(args[0], args[1]);
	}

}
